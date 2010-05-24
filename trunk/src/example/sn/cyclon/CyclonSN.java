package example.sn.cyclon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.cyclon.CyclonEntry;
import example.cyclon.CyclonMessage;
import example.newscast.NodeEntry;
import example.sn.linkable.LinkableSN;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class CyclonSN extends LinkableSN implements EDProtocol, CDProtocol
{
	private static final String PAR_CACHE = "cache";
	private static final String PAR_L = "l";
	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_IDLE = "idle";
	private static final String PAR_STEP = "stepSize";
	private static final String PAR_INIT = "init";
	private static final String PAR_MAX_EMPTY_CYCLES = "emptyCycles";
	private static final long TIMEOUT = 5000;

	private int size;
	private final int l;
	private final int tid;
	private final int idle;
	private final int step;
	private int lastStep = 0;
	private int inDegree = 0;
	private int noPeerCount = 0;
	private final int maxEmptyCycles;
	
	private int NODEID = -1;

	private List<CyclonEntry> cache = null;
	private final NodeInitializer[] inits;

	public CyclonSN(String n)
	{
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
		this.idle = Configuration.getPid(n + "." + PAR_IDLE);
		this.step = Configuration.getInt(n + "." + PAR_STEP);
		this.maxEmptyCycles = Configuration.getInt(n + "." + PAR_MAX_EMPTY_CYCLES, 0);

		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		cache = new ArrayList<CyclonEntry>(size);
		
		this.noPeerCount = 0;
		
		Object[] tmp = Configuration.getInstanceArray(n + "." + PAR_INIT);
		inits = new NodeInitializer[tmp.length];
		for (int i = 0; i < tmp.length; ++i)
		{
			inits[i] = (NodeInitializer) tmp[i];
		}
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
		Linkable lNodeIdleProtocol = (Linkable)lnode.getProtocol(idle);

		if (lNodeIdleProtocol.contains(entry.n) && !entry.removed)
			return true;

		for (int i = 0; i < lNodeIdleProtocol.degree(); i++)
			if (!entry.removed && ((Linkable)lNodeIdleProtocol.getNeighbor(i).getProtocol(idle)).contains(entry.n))
				return true;

		return false;
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
		for (CyclonEntry ce : list)
			if (!ce.n.equals(n) && (index = indexOf(ce.n)) < 0)
				newList.add(ce);
			//Duplicate, take the newest one
			else if (index >= 0 && !cache.get(index).selectedAsReceiver){
				cache.get(index).age = Math.max(ce.age, cache.get(index).age);
				cache.get(index).reuseNode();
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

		if (list.isEmpty()){
			//System.err.println("Empty List " + inDegree + " " + rnode.getID() + " " + lnode.getID());
			if (selectedAtRequest)
				cache.remove(indexOf(rnode));
			//if (selectedAtRequest)
			//	cache.get(indexOf(rnode)).reuseNode();
			return;
		}

		if (selectedAtRequest){
			try{
				cache.set(indexOf(rnode) , new CyclonEntry(list.remove(0)));
			} catch (Exception e){
				System.err.println(CommonState.getNode().getID() + " " + rnode.getID());
				e.printStackTrace();
			}
		}

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

		for (CyclonEntry ce : cache)
			if (ce.nodeSended != null && ce.nodeSended.equals(rnode) && ce.selectedAtRequest == selectedAtRequest)
				ce.reuseNode();

	}
	//-------------------------------------------------------------------


	public Object clone()
	{
		CyclonSN cyclon = null;
		try { cyclon = (CyclonSN) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		cyclon.cache = new ArrayList<CyclonEntry>();
		cyclon.lastStep = 0;
		cyclon.inDegree = 0;
		
		this.noPeerCount = 0;

		return cyclon;
	}

	public boolean addNeighbor(Node neighbour)
	{
		if (contains(neighbour))
			return false;

		if (cache.size() >= size)
			return false;

		CyclonEntry ce = new CyclonEntry(neighbour, 0);
		cache.add(ce);

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
		CyclonMessage message = (CyclonMessage) event;

		List<CyclonEntry> nodesToSend = null;
		if (message.isRequest){
			nodesToSend = selectNeighbors(message.list.size(), node, message.sender, false);

			CyclonMessage msg = new CyclonMessage(node, nodesToSend, false);
			Transport tr = (Transport) node.getProtocol(tid);
			tr.send(node, message.sender, msg, pid);

			if (node.getID() == NODEID){
				System.err.println("------------------ SEND ---------------------------");
				System.err.println(node.getID() + "<-" + message.sender.getID() + " " + cache.size() + " " + nodesToSend.size());
				for (CyclonEntry ce1 : nodesToSend){
					if (ce1.removed)
						System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
				}
				System.err.println("\n---------------------------------------------------");
			}
		}

		if (node.getID() == NODEID){
			System.err.println("------------------ PRDISCARD ----------------------");
			System.err.println(message.isRequest + " " + node.getID() + " " + message.sender.getID() + " " + cache.size() + " " + message.list.size() + " " + size);
			for (CyclonEntry ce1 : cache){
				if (ce1.removed)
					System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
			}
			System.err.println("\n---------------------------------------------------");
		}
		
		// 5. Discard entries pointing to P, and entries that are already in P’s cache.
		List<CyclonEntry> list = discardEntries(node, message.list);
		
		if (node.getID() == NODEID){
			System.err.println("------------------ AFDISCARD ----------------------");
			System.err.println(message.isRequest + " " + node.getID() + " " + message.sender.getID() + " " + cache.size() + " " + list.size() + " " + size);
			for (CyclonEntry ce1 : cache){
				if (ce1.removed)
					System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
			}
			System.err.println("\n---------------------------------------------------");
		}

		// 6. Update P’s cache to include all remaining entries, by firstly using empty
		//    cache slots (if any), and secondly replacing entries among the ones originally
		//    sent to Q.
		insertReceivedItems(list, message.sender, !message.isRequest, node);

		if (node.getID() == NODEID){
			System.err.println("------------------ END ----------------------------");
			System.err.println(node.getID() + " " + message.sender.getID() + " " + cache.size());
			for (CyclonEntry ce1 : cache){
				if (ce1.removed)
					System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
			}
			System.err.println("\n---------------------------------------------------");
		}

		// 1. Increase by one the age of all neighbors.
		increaseAgeAndSort();
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
		/*for (Node n : set)
			if (((LinkableSN)n.getProtocol(idle)).contains(node))
				inDegree++;*/
		inDegree = set.size();
	}

	public void nextCycle(Node node, int protocolID)
	{
		if (inDegree == 0)
			calculateInDegree(node);
		
		if (maxEmptyCycles > 0 && noPeerCount > maxEmptyCycles){
			for (int k = 0; k < inits.length; ++k) {
				inits[k].initialize(node);
			}
			noPeerCount = 0;
		}

		// 1. Increase by one the age of all neighbors.
		//increaseAgeAndSort();

		// 2. Select neighbor Q with the highest age among all neighbors...
		CyclonEntry ce = selectNeighbor();
		if (ce == null){
			ce = selectNeighborAlsoSended();
			if (ce == null){
				noPeerCount++;
				System.err.println("No Peer " + degree() + " " + ((LinkableSN)node.getProtocol(idle)).degree() + " " + node.getID());
				return;
			}
		}
		
		ce.removeNode(ce.n, true, true);
		
		if (node.getID() == NODEID)
			System.err.println(node.getID() + "->" + ce.n.getID());

		List<CyclonEntry> nodesToSend = null;

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
		Transport tr = (Transport) node.getProtocol(tid);
		tr.send(node, ce.n, message, protocolID);
	}

	@Override
	public boolean containsAsFriend(Node lnode, Node n) {
		LinkableSN linkable = (LinkableSN)lnode.getProtocol(idle);
		return linkable.containsAsFriend(lnode, n);
	}

	@Override
	public Node getFriendPeer(Node lnode, Node n)
	{		
		//LinkableSN lLinkable = (LinkableSN)lnode.getProtocol(idle);
		LinkableSN rLinkable = (LinkableSN)n.getProtocol(idle);
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

		for (CyclonEntry ce : cache)
			//lnode is a friend of n of ce.n is a friend of n
			if (rLinkable.containsAsFriend(n, lnode) || ((LinkableSN)ce.n.getProtocol(idle)).containsAsFriend(lnode, n))
				set.add(ce.n);

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
}
