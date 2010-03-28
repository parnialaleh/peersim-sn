package example.cyclon;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import example.sn.newscast.NewscastED;
import example.sn.newscast.NodeEntry;


import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class Cyclon implements Linkable, EDProtocol, CDProtocol
{
	private static final String PAR_CACHE = "cache";
	private static final String PAR_L = "l";
	private static final String PAR_TRANSPORT = "transport";

	private final int size;
	private final int l;
	private final int tid;

	private List<CyclonEntry> cache = null;

	public Cyclon(String n)
	{
		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
		
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
			return cache.get(cache.size()-1).n;
		} catch (Exception e){
			return null;
		}
	}
	
	private List<CyclonEntry> selectNeighbors(int l)
	{
		int dim = Math.min(l, cache.size()-1);
		List<CyclonEntry> list = new ArrayList<CyclonEntry>(l);

		for (int i = 0; i < dim; i++){
			CyclonEntry ce = cache.remove(CommonState.r.nextInt(cache.size()-1));
			list.add(ce);
		}
		
		return list;
	}
	
	/*private List<CyclonEntry> discardEntries(Node n, List<CyclonEntry> list)
	{
		List<CyclonEntry> newList = new ArrayList<CyclonEntry>();
		for (CyclonEntry ce : list)
			if (!ce.n.equals(n) && contains(ce.n))
				newList.add(ce);
		
		return newList;
	}*/
	
	private int indexOf(CyclonEntry ce)
	{
		for (int i = 0; i < cache.size(); i++)
			if (cache.get(i).n.equals(ce.n))
				return i;

		return -1;
	}
	
	private void insertItems(List<CyclonEntry> list)
	{
		int pos = 0;
		
		for (CyclonEntry ce : list)
			if ((pos = indexOf(ce)) < 0)
				cache.add(new CyclonEntry(ce.n, ce.age));
			else{
				CyclonEntry lce = cache.get(pos);
				if (lce.age > ce.age)
					cache.set(pos, new CyclonEntry(ce.n, ce.age));
			}
	}
	
	private void insertReceivedItems(List<CyclonEntry> list, List<CyclonEntry> sList)
	{
		//Add received items to the cache
		insertItems(list);

		if (cache.size() >= size)
			return;
		
		//I've received less data that one I've sent, so
		//reuse some sent entries
		int cacheSize = cache.size();
		int sListSize = sList.size();
		for (int i = 0; i < Math.min(size-cacheSize, sListSize); i++){
			int pos = CommonState.r.nextInt(sList.size());
			CyclonEntry ce = sList.remove(pos);
			cache.add(ce);
		}
	}
	//-------------------------------------------------------------------


	public Object clone()
	{
		Cyclon cyclon = null;
		try { cyclon = (Cyclon) super.clone(); }
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
			nodesToSend = selectNeighbors(message.list.size()+1);
			
			CyclonMessage msg = new CyclonMessage(node, nodesToSend, false, message.list);
			Transport tr = (Transport) node.getProtocol(tid);
			tr.send(node, message.node, msg, pid);
		}
		else
			nodesToSend = message.receivedList;
		
		// 5. Discard entries pointing to P, and entries that are already in P’s cache.
		//XXX List<CyclonEntry> list = discardEntries(node, message.list);
		
		// 6. Update P’s cache to include all remaining entries, by firstly using empty
		//    cache slots (if any), and secondly replacing entries among the ones originally
		//    sent to Q.
		insertReceivedItems(message.list, nodesToSend);
	}

	public void nextCycle(Node node, int protocolID)
	{
		// 1. Increase by one the age of all neighbors.
		increaseAgeAndSort();

		// 2. Select neighbor Q with the highest age among all neighbors...
		Node q = selectNeighbor();
		if (q == null){
			System.err.println("No Peer");
			return;
		}
		//    and l − 1 other random neighbors.
		List<CyclonEntry> nodesToSend = selectNeighbors(l-1);
		
		// 3. Replace Q’s entry with a new entry of age 0 and with P’s address.
		cache.set(cache.size()-1, new CyclonEntry(node, 0));
		
		// 4. Send the updated subset to peer Q.
		CyclonMessage message = new CyclonMessage(node, nodesToSend, true, null);
		Transport tr = (Transport) node.getProtocol(tid);
		tr.send(node, q, message, protocolID);
	}

}
