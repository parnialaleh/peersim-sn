package example.sn.cyclon;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import example.sn.linkable.LinkableSN;
import example.sn.newscast.NodeEntry;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class CyclonSN extends LinkableSN implements EDProtocol, CDProtocol
{
	private static final String PAR_CACHE = "cache";
	private static final String PAR_L = "l";
	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_IDLE = "idle";

	private final int size;
	private final int l;
	private final int tid;
	private final int idle;

	private List<CyclonEntry> cache = null;

	public CyclonSN(String n)
	{
		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
		this.idle = Configuration.getPid(n + "." + PAR_IDLE);

		cache = new ArrayList<CyclonEntry>(size);
	}

	//-------------------------------------------------------------------
	private void increaseAgeAndSort()
	{
		for (CyclonEntry ce : cache)
			ce.increase();

		Collections.sort(cache, new CyclonEntry());
	}

	private Node selectNeighbor()
	{
		try{
			return cache.remove(cache.size()-1).n;
		} catch (Exception e){
			return null;
		}
	}

	private List<CyclonEntry> initList(Node rnode)
	{
		Linkable idleProtocol = (Linkable)rnode.getProtocol(idle);

		List<CyclonEntry> list = new ArrayList<CyclonEntry>();
		for (CyclonEntry ce : cache)
			if (idleProtocol.contains(ce.n))
				list.add(ce);

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
	private List<CyclonEntry> selectNeighbors(int l, Node rnode)
	{
		int dim = Math.min(l, cache.size()-1);
		List<CyclonEntry> list = initList(rnode);

		while (list.size() > dim)
			list.remove(CommonState.r.nextInt(list.size()));

		/*List<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < cache.size()-1; i++)
			tmp.add(i);

		for (int i = 0; i < dim; i++){
			CyclonEntry ce = cache.get(tmp.remove(CommonState.r.nextInt(tmp.size())));
			list.add(ce);
		}*/

		return list;
	}

	private List<CyclonEntry> discardEntries(Node n, List<CyclonEntry> list)
	{
		List<CyclonEntry> newList = new ArrayList<CyclonEntry>();
		for (CyclonEntry ce : list){
			if (!ce.n.equals(n) && !contains(ce.n))
				newList.add(ce);
		}

		return newList;
	}

	private int indexOf(CyclonEntry ce)
	{
		for (int i = 0; i < cache.size(); i++)
			if (cache.get(i).n.equals(ce.n))
				return i;

		return -1;
	}

	private void insertReceivedItems(List<CyclonEntry> list, List<CyclonEntry> sentList)
	{
		for (CyclonEntry ce : list){
			if (cache.size() < size)
				cache.add(new CyclonEntry(ce.n, ce.age));
			else{
				int index = 0;
				while (index < sentList.size() && indexOf(sentList.get(index)) < 0)
					index++;
				if (index >= sentList.size())
					return;
				cache.set(indexOf(sentList.remove(index)), new CyclonEntry(ce.n, ce.age));
			}
		}
		/*//Add received items to the cache
		insertItems(list);

		if (cache.size()-1 >= size)
			return;

		//I've received less data that one I've sent, so
		//reuse some sent entries
		int cacheSize = cache.size();
		int sListSize = sList.size();
		for (int i = 0; i < Math.min(size-cacheSize-1, sListSize-1); i++){
			int pos = CommonState.r.nextInt(sList.size());
			CyclonEntry ce = sList.remove(pos);
			cache.add(ce);
		}*/
	}
	//-------------------------------------------------------------------


	public Object clone()
	{
		CyclonSN cyclon = null;
		try { cyclon = (CyclonSN) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		cyclon.cache = new ArrayList<CyclonEntry>();

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

	public void pack() {
		// TODO Auto-generated method stub

	}

	public void onKill() {}

	public void processEvent(Node node, int pid, Object event)
	{
		CyclonMessage message = (CyclonMessage) event;

		List<CyclonEntry> nodesToSend = null;
		if (message.isResuest){
			nodesToSend = selectNeighbors(message.list.size(), message.node);

			CyclonMessage msg = new CyclonMessage(node, nodesToSend, false, message.list);
			Transport tr = (Transport) node.getProtocol(tid);
			tr.send(node, message.node, msg, pid);
		}
		else
			nodesToSend = message.sentList;

		// 5. Discard entries pointing to P, and entries that are already in P’s cache.
		List<CyclonEntry> list = discardEntries(node, message.list);

		// 6. Update P’s cache to include all remaining entries, by firstly using empty
		//    cache slots (if any), and secondly replacing entries among the ones originally
		//    sent to Q.
		insertReceivedItems(list, nodesToSend);

		// 1. Increase by one the age of all neighbors.
		increaseAgeAndSort();
	}

	public void nextCycle(Node node, int protocolID)
	{
		// 1. Increase by one the age of all neighbors.
		//increaseAgeAndSort();

		// 2. Select neighbor Q with the highest age among all neighbors...
		Node q = selectNeighbor();
		if (q == null){
			System.err.println("No Peer");
			return;
		}
		//    and l − 1 other random neighbors.
		List<CyclonEntry> nodesToSend = selectNeighbors(l-1, q);

		// 3. Replace Q’s entry with a new entry of age 0 and with P’s address.
		nodesToSend.add(0, new CyclonEntry(node, 0));

		// 4. Send the updated subset to peer Q.
		CyclonMessage message = new CyclonMessage(node, nodesToSend, true, null);
		Transport tr = (Transport) node.getProtocol(tid);
		tr.send(node, q, message, protocolID);
	}

	@Override
	public boolean containsAsFriend(Node lnode, Node n) {
		LinkableSN linkable = (LinkableSN)lnode.getProtocol(idle);
		return linkable.containsAsFriend(lnode, n);
	}

	@Override
	public Node getFriendPeer(Node lnode, Node n)
	{
		LinkableSN linkable = (LinkableSN)lnode.getProtocol(idle);
		return linkable.getFriendPeer(lnode, n);
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
}
