package example.sn.cyclon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.cyclon.CyclonEntry;
import example.cyclon.CyclonMessage;
import example.newscast.NodeEntry;
import example.sn.linkable.LinkableSN;
import example.sn.node.SNNode;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.graph.Graph;
import peersim.graph.GraphAlgorithms;
import peersim.graph.NeighbourListGraph;
import peersim.transport.Transport;
import peersim.util.IncrementalStats;

public class CyclonSN extends LinkableSN implements EDProtocol, CDProtocol
{
	private static final String PAR_CACHE = "cache";
	private static final String PAR_L = "l";
	private static final String PAR_PERIOD = "period";
	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_IDLE = "idle";
	private static final String PAR_STEP = "stepSize";
	private static final String PAR_INIT = "init";
	private static final String PAR_MAX_EMPTY_CYCLES = "emptyCycles";
	private static final long TIMEOUT = 2000;

	private class CommonData
	{
		public int tid;
		public int period;
	}

	private class Point implements Comparator<Point>
	{
		Node n;
		int index;

		public Point(Node n, int index)
		{
			this.n = n;
			this.index = index;
		}

		public int compare(Point p1, Point p2)
		{
			long diff = ((SNNode)p1.n).getRealID() - ((SNNode)p2.n).getRealID();
			if (diff > 0)
				return 1;
			else if (diff < 0)
				return -1;
			
			return 0;	
		}
	}

	private int size;
	private final int l;
	private final int idle;
	private final int step;
	private int inDegree = 0;
	private int noPeerCount = 0;
	private final int maxEmptyCycles;
	private final CommonData c;

	private int NODEID = -1147;

	private List<CyclonEntry> cache = null;
	private final NodeInitializer[] inits;

	private static GraphAlgorithms ga = new GraphAlgorithms();
	private Comparator<Point> comparator = new Point(null, 0);
	private static Graph graph = null;
	private static List<Point> indexes = null;
	private static int[][] d = null;
	
	private IncrementalStats is = null;
	private int totalReceived = 0;
	
	private final String name;
	private int lastStep = 0;
	
	public CyclonSN(String n)
	{
		c = new CommonData();
		c.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
		c.period = Configuration.getInt(n + "." + PAR_PERIOD);

		this.l = Configuration.getInt(n + "." + PAR_L);
		this.idle = Configuration.getPid(n + "." + PAR_IDLE);
		this.step = Configuration.getInt(n + "." + PAR_STEP);
		this.maxEmptyCycles = Configuration.getInt(n + "." + PAR_MAX_EMPTY_CYCLES, 0);

		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		cache = new ArrayList<CyclonEntry>(size);

		this.name = n;
		this.noPeerCount = 0;
		this.totalReceived = 0;

		Object[] tmp = Configuration.getInstanceArray(n + "." + PAR_INIT);
		inits = new NodeInitializer[tmp.length];
		for (int i = 0; i < tmp.length; ++i)
		{
			inits[i] = (NodeInitializer) tmp[i];
		}
		
		int r = CommonState.r.nextInt(c.period);
		EDSimulator.add(r, new Integer(r), CommonState.getNode(), CommonState.getPid());
	}

	//-------------------------------------------------------------------
	private void increaseAgeAndSort()
	{
		//for (CyclonEntry ce : cache)
		//	ce.increase();

		Collections.sort(cache, new CyclonEntry());
	}

	private CyclonEntry selectNeighbor()
	{
		try{			
			int i = cache.size()-1;

			if (cache.get(i).removed && (CommonState.getTime() - cache.get(i).timeRemoved) >= TIMEOUT){
				cache.remove(i);
				i--;
			}

			while (cache.get(i).removed)// && cache.get(i).selectedAsReceiver)
				i--;

			return cache.get(i);
		} catch (Exception e){
			return null;
		}
	}

	private CyclonEntry selectNeighborAlsoSended()
	{
		try{			
			int i = cache.size()-1;

			if (cache.get(i).removed && (CommonState.getTime() - cache.get(i).timeRemoved) >= TIMEOUT){
				cache.remove(i);
				i--;
			}

			while (cache.get(i).removed && cache.get(i).selectedAsReceiver)
				i--;

			return cache.get(i);
		} catch (Exception e){
			return null;
		}
	}

	private boolean isInterestingNode(CyclonEntry entry, Node lnode, Node rnode, boolean isRNodeFriend)
	{
		//ga.dist(graph, Collections.binarySearch(indexes, new Point(entry.n, 0), comparator));
		//distance between entry and rnode <= 2 -> TRUE else FALSE
		int i1 = Collections.binarySearch(indexes, new Point(rnode, 0), comparator);
		int i2 = Collections.binarySearch(indexes, new Point(entry.n, 0), comparator);
		
		return CyclonSN.d[i1][i2] <= 2 && CyclonSN.d[i1][i2] >= 0;
	}

	/**
	 * Return a list with all the friends of rnode I know up in the network
	 * 
	 * @param rnode
	 * @return
	 */
	private List<CyclonEntry> initList(Node lnode, Node rnode)
	{
		Linkable lNodeIdleProtocol = (Linkable)lnode.getProtocol(idle);
		boolean isRNodeFriend = lNodeIdleProtocol.contains(rnode);

		//Linkable rNodeIdleProtocol = (Linkable)rnode.getProtocol(idle);

		List<CyclonEntry> list = new ArrayList<CyclonEntry>();
		for (CyclonEntry ce : cache){
			if (ce.removed && (CommonState.getTime() - ce.timeRemoved) >= TIMEOUT)
				ce.reuseNode();
			//ce.n is a friend of rNode or rNode is a friend of mine and ce.n is my friend
			//if ((rNodeIdleProtocol.contains(ce.n) || (isRNodeFriend && lNodeIdleProtocol.contains(ce.n))) && !ce.removed)
			if (isInterestingNode(ce, lnode, rnode, isRNodeFriend))
				list.add(ce);
		}

		return list;
	}

	/**
	 * Send to rnode the list of nodes I know that are also
	 * its friends
	 * 
	 * @param l
	 * @param rnode
	 * @return
	 */
	private List<CyclonEntry> selectNeighbors(int l, Node lnode, Node rnode, boolean selectedAtRequest)
	{
		int dim = Math.min(l, cache.size());
		List<CyclonEntry> list = initList(lnode, rnode);

		while (list.size() > dim)
			list.remove(CommonState.r.nextInt(list.size()));

		for (CyclonEntry ce : list)
			ce.removeNode(rnode, selectedAtRequest, false);

		return list;
	}

	private List<CyclonEntry> discardEntries(Node n, List<CyclonEntry> list)
	{
		int index = 0;
		List<CyclonEntry> newList = new ArrayList<CyclonEntry>();
		for (CyclonEntry ce : list){
			index = indexOf(ce.n);
			if (!ce.n.equals(n) && index < 0)
				newList.add(ce);
			//Duplicate, take the newest one
			else if (index >= 0 && !cache.get(index).selectedAsReceiver){
				cache.get(index).age = Math.max(ce.age, cache.get(index).age);
				cache.get(index).reuseNode();
			}
		}

		return newList;
	}

	private int getFirstDeleted(Node rnode, boolean selectedAtRequest)
	{
		for (int i = cache.size()-1 ; i >= 0; i--)
			if (cache.get(i).removed && cache.get(i).nodeSended.equals(rnode) && cache.get(i).selectedAtRequest == selectedAtRequest)
				return i;

		return -1;
	}

	private int indexOf(Node rnode)
	{
		for (int i = cache.size()-1; i >= 0; i--)
			if (cache.get(i).n.equals(rnode))
				return i;

		return -1;
	}

	private void insertReceivedItems(List<CyclonEntry> list, Node rnode, boolean selectedAtRequest, Node lnode)
	{
		//		System.err.print(CommonState.getNode().getID() + " -> " + rnode.getID() + " ");
		//		for (CyclonEntry ce1 : cache){
		//			if (ce1.removed)
		//				System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
		//		}
		//		System.err.println();

		/*if (list.isEmpty()){
			//System.err.println("Empty List " + inDegree + " " + rnode.getID() + " " + lnode.getID());
			if (selectedAtRequest)
				cache.remove(indexOf(rnode));
			//if (selectedAtRequest)
			//	cache.get(indexOf(rnode)).reuseNode();
			return;
		}*/	

		/*if (selectedAtRequest){
			try{
				cache.set(indexOf(rnode) , new CyclonEntry(list.remove(0)));
			} catch (Exception e){
				System.err.println(CommonState.getNode().getID() + " " + rnode.getID());
				e.printStackTrace();
			}
		}*/

		for (CyclonEntry ce : list){
			// firstly using empty cache slots
			if (cache.size() < size)
				cache.add(new CyclonEntry(ce.n, ce.age));
			// secondly replacing entries among the ones sent to rnode
			else{
				int index = getFirstDeleted(rnode, selectedAtRequest);
				//				if (index < 0){
				//					System.err.println("PROBLEM " + CommonState.getNode().getID() + " " + cache.size() + " " + rnode.getID());
				//					return;
				//				}
				if (index >= 0)
					cache.set(index, new CyclonEntry(ce.n, ce.age));
			}
		}

		for (CyclonEntry ce : cache){
			if (ce.nodeSended != null && ce.nodeSended.equals(rnode) && ce.selectedAtRequest == selectedAtRequest)
				ce.reuseNode();
			if (ce.removed && (CommonState.getTime() - ce.timeRemoved) >= TIMEOUT)
				cache.remove(ce);
		}

	}
	//-------------------------------------------------------------------


	public Object clone()
	{
		CyclonSN cyclon = null;
		try { cyclon = (CyclonSN) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		cyclon.cache = new ArrayList<CyclonEntry>();
		//cyclon.lastStep = 0;
		cyclon.inDegree = 0;
		cyclon.is = null;
		cyclon.noPeerCount = 0;
		cyclon.totalReceived = 0;
		
		int r = CommonState.r.nextInt(c.period);
		EDSimulator.add(r, new Integer(r), CommonState.getNode(), CommonState.getPid());

		return cyclon;
	}

	public boolean addNeighbor(Node neighbour)
	{
		if (contains(neighbour))
			return false;

		if (cache.size() >= size)
			return false;

		CyclonEntry ce = new CyclonEntry(neighbour, CommonState.getTime());
		cache.add(ce);

		increaseAgeAndSort();

		return true;
	}

	public boolean contains(Node neighbour)
	{
		for (CyclonEntry ne : cache)
			if (ne.n.equals(neighbour))
				return true;
		return false;
	}

	public int degree()
	{
		return cache.size();
	}

	public Node getNeighbor(int i)
	{
		return cache.get(i).n;
	}

	public void pack() {}

	public void onKill() {}

	public void processEvent(Node node, int pid, Object event)
	{
		if (event instanceof Integer){
			if (inDegree == 0)
				calculateInDegree(node);
			//double time = (double)inDegree/(double)step;
			//if (time < 1)
			//	time = 1;
			//System.out.println((long)((double)c.period * time) + " " + inDegree + " " + step);
			//EDSimulator.add((long)((double)c.period * time), (Integer) event, node, pid);

			activeThread(node, pid);
		}
		else
			passiveThread(node, pid, event);
	}

	private Graph grap hInit(Node node)
	{
		/*NeighbourListGraph g = new NeighbourListGraph(true);
		Node n = null;pidGossip

		LinkableSN linkable = (LinkableSN)node.getProtocol(idle);
		Set<Node> set = new HashSet<Node>();
		for (int i = 0; i < linkable.degree(); i++){
			set.add(linkable.getNeighbor(i));

			LinkableSN rLinkable = (LinkableSN)linkable.getNeighbor(i).getProtocol(idle);
			for (int j = 0; j < rLinkable.degree(); j++)
				set.add(rLinkable.getNeighbor(j));
		}
		set.add(node);

		indexes = new ArrayList<Point>();
		for (Node nd: set.toArray(new Node[0]))
			indexes.add(new Point(nd, g.addNode(nd)));

		Collections.sort(indexes, comparator);

		for (int i = 0; i < indexes.size(); i++){
			n = indexes.get(i).n;
			linkable = (LinkableSN)n.getProtocol(idle);
			for (int j = 0; j < linkable.degree(); j++){
				int index = Collections.binarySearch(indexes, new Point(linkable.getNeighbor(j), 0), comparator);
				if (index >= 0)
					g.setEdge(indexes.get(i).index, index);
			}
		}

		return g;*/

		NeighbourListGraph g = new NeighbourListGraph(false);
		Node n = null;
		LinkableSN l = null;

		indexes = new ArrayList<Point>();
		for (int i = 0; i < Network.size(); i++){
			n = Network.get(i);
			indexes.add(new Point(n, g.addNode(n))); 
		}

		Collections.sort(indexes, comparator);

		for (int i = 0; i < indexes.size(); i++){
			n = indexes.get(i).n;
			l = (LinkableSN)n.getProtocol(idle);
			for (int j = 0; j < l.degree(); j++)
				g.setEdge(indexes.get(i).index, Collections.binarySearch(indexes, new Point(l.getNeighbor(j), 0), comparator)); //indexOf(l.getNeighbor(j), indexes));
		}

		return g;
	}

	private void calculateInDegree(Node node)
	{
		LinkableSN linkable = (LinkableSN)node.getProtocol(idle);
		Set<Node> set = new HashSet<Node>();
		for (int i = 0; i < linkable.degree(); i++){
			LinkableSN rLinkable = (LinkableSN)linkable.getNeighbor(i).getProtocol(idle);
			set.add(linkable.getNeighbor(i));
			for (int j = 0; j < rLinkable.degree(); j++)
				set.add(rLinkable.getNeighbor(j));
		}

		inDegree = set.size();

		if (CyclonSN.graph == null){
			CyclonSN.graph = graphInit(node);

			CyclonSN.d = new int[Network.size()][];
			for (int i = 0; i < CyclonSN.graph.size(); i++){
				CyclonSN.ga.dist(CyclonSN.graph, i);					
				CyclonSN.d[i] = new int[CyclonSN.ga.d.length];
				System.arraycopy(CyclonSN.ga.d, 0, CyclonSN.d[i], 0, CyclonSN.ga.d.length);
			}
		}
	}

	public void nextCycle(Node node, int protocolID)
	{
		if (is != null){
			is.add(totalReceived);
			//System.out.println(" " + CommonState.getTime() + " " + name + " node: " + node.getID() + " indegree: " + inDegree + " receivedCycle: " + totalReceived + " is: " + is);
		}
		else
			is = new IncrementalStats();
		noPeerCount++;
		totalReceived = 0;
	}

	@Override
	public boolean containsAsFriend(Node lnode, Node n) {
		LinkableSN linkable = (LinkableSN)lnode.getProtocol(idle);
		return linkable.containsAsFriend(lnode, n);
	}

	@Override
	public Node getFriendPeer(Node lnode, Node n)
	{		
		LinkableSN lLinkable = (LinkableSN)lnode.getProtocol(idle);
		//LinkableSN rLinkable = (LinkableSN)n.getProtocol(idle);
		Set<Node> set = new HashSet<Node>();

		/*//n is my friend
		if (lLinkable.containsAsFriend(lnode, n)){	
			//search in cache all the friends I know to be up
			for (CyclonEntry ce : cache)
				if (rLinkable.contains(n))
					list.add(ce.n);
		}
		else{
			for (CyclonEntry ce : cache){
				rLinkable = (LinkableSN)ce.n.getProtocol(idle);
				if (rLinkable.contains(n))
					list.add(ce.n);
			}
		}*/

		/*for (CyclonEntry ce : cache)
			//lnode is a friend of n of ce.n is a friend of n
			if (lLinkable.containsAsFriend(lnode, n) || ((LinkableSN)ce.n.getProtocol(idle)).containsAsFriend(lnode, n))
				set.add(ce.n);*/

		if (lLinkable.containsAsFriend(lnode, n))
			set.add(lnode);

		for (int i = 0; i < cache.size(); i++){
			LinkableSN rLinkable = (LinkableSN)cache.get(i).n.getProtocol(idle);
			if (rLinkable.containsAsFriend(lnode, n))
				set.add(cache.get(i).n);
			for (int j = 0; j < rLinkable.degree(); j++)
				if (((LinkableSN)rLinkable.getNeighbor(j).getProtocol(idle)).containsAsFriend(lnode, n))
					set.add(rLinkable.getNeighbor(j));
		}

		if (set.size() > 0)
			return set.toArray(new Node[0])[CommonState.r.nextInt(set.size())];

		return null;
	}

	@Override
	public NodeEntry[] getFriends(Node lnode, Node n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node[] getNodes(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getPeer(Node node)
	{
		try{
			return cache.get(CommonState.r.nextInt(cache.size())).n;
		} catch (Exception e){
			return null;
		}
	}

	@Override
	public void clearCache()
	{
		this.cache.clear();
	}

	private void passiveThread(Node node, int pid, Object event)
	{
		CyclonMessage message = (CyclonMessage) event;

		List<CyclonEntry> nodesToSend = null;
		if (message.isRequest){
			totalReceived++;
			nodesToSend = selectNeighbors(message.list.size(), node, message.sender, false);

			CyclonMessage msg = new CyclonMessage(node, nodesToSend, false);
			Transport tr = (Transport) node.getProtocol(c.tid);
			tr.send(node, message.sender, msg, pid);

			if (node.getID() == NODEID){
				System.err.println("------------------ SEND ---------------------------");
				System.err.println(message.sender.getID() + "->" + node.getID() + " cacheSize: " + cache.size() + " sendSize: " + nodesToSend.size());
				for (CyclonEntry ce1 : nodesToSend)
					System.err.print("[" + ce1.n.getID() + "," + ce1.nodeSended.getID() + "," + ce1.removed + "] ");
				System.err.println("\n---------------------------------------------------");
			}
		}

		if (node.getID() == NODEID){
			System.err.println("------------------ PRDISCARD ----------------------");
			System.err.println("Node: " + node.getID() + " Sender: " + message.sender.getID() + " cacheSize: " + cache.size() + " listSize: " + message.list.size() + " request: " + message.isRequest);
			for (CyclonEntry ce1 : cache){
				if (ce1.removed)
					System.err.print("[" + ce1.n.getID() + "," + ce1.nodeSended.getID() + "," + ce1.removed + "] ");
			}
			System.err.println("\n---------------------------------------------------");
		}

		// 5. Discard entries pointing to P, and entries that are already in P’s cache.
		List<CyclonEntry> list = discardEntries(node, message.list);

		if (node.getID() == NODEID){
			System.err.println("------------------ AFDISCARD ----------------------");
			System.err.println("Node: " + node.getID() + " Sender: " + message.sender.getID() + " cacheSize: " + cache.size() + " listSize: " + message.list.size() + " request: " + message.isRequest);
			for (CyclonEntry ce1 : cache){
				if (ce1.removed)
					System.err.print("[" + ce1.n.getID() + "," + ce1.nodeSended.getID() + "," + ce1.removed + "] ");
			}
			System.err.println("\n---------------------------------------------------");
		}

		// 6. Update P’s cache to include all remaining entries, by firstly using empty
		//    cache slots (if any), and secondly replacing entries among the ones originally
		//    sent to Q.
		insertReceivedItems(list, message.sender, !message.isRequest, node);

		if (node.getID() == NODEID){
			System.err.println("------------------ END ----------------------------");
			System.err.println("Node: " + node.getID() + " Sender: " + message.sender.getID() + " cacheSize: " + cache.size() + " listSize: " + message.list.size() + " request: " + message.isRequest);
			for (CyclonEntry ce1 : cache){
				if (ce1.removed)
					System.err.print("[" + ce1.n.getID() + "," + ce1.nodeSended.getID() + "," + ce1.removed + "] ");
			}
			System.err.println("\n---------------------------------------------------");
		}

		// 1. Increase by one the age of all neighbors.
		increaseAgeAndSort();
	}

	private void activeThread(Node node, int protocolID)
	{
		if (maxEmptyCycles > 0 && noPeerCount > maxEmptyCycles){
			for (int k = 0; k < inits.length; ++k) {
				System.out.println("REINIT_CACHE " + CommonState.getTime() + " " + ((SNNode)node).getRealID() + " " + inDegree);
				inits[k].initialize(node);
			}
			noPeerCount = 0;
		}

		// 2. Select neighbor Q with the highest age among all neighbors...
		CyclonEntry ce = selectNeighbor();
		if (ce == null){
			ce = selectNeighborAlsoSended();
			if (ce == null){
				//System.err.println("No Peer " + degree() + " " + ((LinkableSN)node.getProtocol(idle)).degree() + " " + node.getID());
				return;
			}
		}
		noPeerCount = 0;

		ce.removeNode(ce.n, true, true);
		cache.remove(indexOf(ce.n));

		if (node.getID() == NODEID)
			System.err.println(node.getID() + "->" + ce.n.getID());

		List<CyclonEntry> nodesToSend = null;
		//and l − 1 other random neighbors.
		nodesToSend = selectNeighbors(l-1, node, ce.n, true);
		// 3. Replace Q’s entry with a new entry of age 0 and with P’s address.
		nodesToSend.add(0, new CyclonEntry(node, CommonState.getTime()));

		int stepSize = (int)Math.ceil((double)inDegree/(double)step);
		lastStep = (lastStep+1)%stepSize;
		if (lastStep == 0){
			//and l − 1 other random neighbors.
			nodesToSend = selectNeighbors(l-1, node, ce.n, true);
			// 3. Replace Q’s entry with a new entry of age 0 and with P’s address.
			nodesToSend.add(0, new CyclonEntry(node, CommonState.getTime()));
		}
		else
			nodesToSend = selectNeighbors(l, node, ce.n, true);

		// 4. Send the updated subset to peer Q.
		CyclonMessage message = new CyclonMessage(node, nodesToSend, true);
		Transport tr = (Transport) node.getProtocol(c.tid);
		tr.send(node, ce.n, message, protocolID);
	}

}
