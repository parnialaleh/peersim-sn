package example.newscast;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class Newscast implements EDProtocol, CDProtocol, Linkable
{

	/**
	 * Initial cache size.
	 * @config
	 */
	private static final String PAR_CACHE = "cache";

	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_PERIOD = "period";

	/** Temp array for merging. Its size is the same as the cache size. */
	private static NodeEntry[] tn;

	/** Neighbors currently in the cache */
	private NodeEntry[] cache;

	private final String name;

	protected class CommonData
	{
		/** Transport protocol identifier */
		public int tid;

		/** Length of a cycle in the protocol */
		public int period;

	}

	private CommonData c = null;

	public Newscast(String n)
	{
		final int cachesize = Configuration.getInt(n + "." + PAR_CACHE);

		if (Newscast.tn == null || Newscast.tn.length < cachesize) {
			Newscast.tn = new NodeEntry[cachesize];
		}
		cache = new NodeEntry[cachesize];

		c = new CommonData();

		name = n;

		// Read other parameters
		c.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
		c.period = Configuration.getInt(n + "." + PAR_PERIOD);
	}

	public Object clone()
	{
		Newscast sn = null;
		try { sn = (Newscast) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		sn.cache = new NodeEntry[cache.length];

		return sn;
	}

	private Node getPeer()
	{
		final int d = degree();
		if (d == 0)
			return null;
		int index = CommonState.r.nextInt(d);
		Node result = cache[index].n;
		
		return result;

		/*if (result.isUp())
			return result;

		// proceed towards older entries
		for (int i = index + 1; i < d; ++i)
			if (cache[i].n.isUp())
				return cache[i].n;

		// proceed towards younger entries
		for (int i = index - 1; i >= 0; --i)
			if (cache[i].n.isUp())
				return cache[i].n;

		// no accessible peer
		return null;*/
	}

	private void merge(Node thisNode, NodeEntry peerFriends[], Node peerNode)
	{
		int i1 = 0;
		int i2 = 0;
		boolean first;
		boolean lastTieWinner = CommonState.r.nextBoolean();
		int i = 1;

		final int d1 = degree();
		int d2 = 0;
		
		for (int j = 0; j < peerFriends.length; j++)
			if (peerFriends[j] == null){
				d2 = j;
				break;
			}

		// merging two arrays
		while (i < cache.length && i1 < d1 && i2 < d2) {
			if (cache[i1].ts == peerFriends[i2].ts)
				lastTieWinner = first = !lastTieWinner;
			else 
				first = cache[i1].ts > peerFriends[i2].ts;
			

			if (first) {
				if (cache[i1] != peerNode && !Newscast.contains(i, cache[i1].n)) {
					Newscast.tn[i] = (NodeEntry)cache[i1].clone();
					i++;
				}
				i1++;
			} else {
				if (peerFriends[i2] != thisNode
						&& !Newscast.contains(i, peerFriends[i2].n)) {
					Newscast.tn[i] = (NodeEntry)peerFriends[i2].clone();
					i++;
				}
				i2++;
			}
		}

		if (i < cache.length) {
			for (; i1 < d1 && i < cache.length; ++i1) {
				if (cache[i1].n != peerNode && !Newscast.contains(i, cache[i1].n)) {
					Newscast.tn[i] = (NodeEntry)cache[i1].clone();
					i++;
				}
			}

			for (; i2 < d2 && i < cache.length; ++i2) {
				if (peerFriends[i2].n != thisNode
						&& !Newscast.contains(i, peerFriends[i2].n)) {
					Newscast.tn[i] = (NodeEntry)peerFriends[i2].clone();
					i++;
				}
			}
		}

		// if the two arrays were not enough to fill the buffer
		// fill in the rest with nulls
		if (i < cache.length) {
			for (; i < cache.length; ++i) {
				Newscast.tn[i] = null;
			}
		}
	}

	public void processEvent(Node lnode, int thisPid, Object event)
	{
		NewscastMessage message = (NewscastMessage)event;
		if (message.isRequest()){
			Transport tr = (Transport) lnode.getProtocol(c.tid);
			NewscastMessage messsage = new NewscastMessage(lnode, false, cache);
			tr.send(lnode, message.getSender(), messsage, thisPid);
		}

		merge(lnode, message.getCache(), message.getSender());

		//if (contains(message.getSender())){
			Newscast.tn[0] = new NodeEntry();
			Newscast.tn[0].ts = CommonState.getIntTime();
			Newscast.tn[0].n = message.getSender();
			cache = new NodeEntry[Newscast.tn.length];
			System.arraycopy(Newscast.tn, 0, cache, 0, cache.length);
		//}
		//else{
		//	cache = new NodeEntry[Newscast.tn.length - 1];
		//	System.arraycopy(Newscast.tn, 1, cache, 0, cache.length);
		//}
	}

	public boolean addNeighbor(Node node) 
	{
		int i;
		for (i = 0; i < cache.length && cache[i] != null; i++) {
			if (cache[i] == node)
				return false;
		}

		if (i == cache.length) {
			NodeEntry[] temp = new NodeEntry[3 * cache.length / 2];
			System.arraycopy(cache, 0, temp, 0, cache.length);
			cache = temp;
		}

		if (i > 0 && cache[i - 1].ts < CommonState.getIntTime()) {
			// we need to insert to the first position
			for (int j = cache.length - 2; j >= 0; --j) {
				cache[j + 1] = cache[j];
			}
			i = 0;
		}

		cache[i] = new NodeEntry();
		cache[i].n = node;
		cache[i].ts = CommonState.getIntTime();
		return true;
	}

	public boolean contains(Node node)
	{
		for (int i = 0; i < cache.length && cache[i] != null; i++) {
			if (cache[i].n.getID() == node.getID())
				return true;
		}
		return false;
	}
	
	private static boolean contains(int size, Node peer)
	{
		for (int i = 0; i < size; i++) {
			if (Newscast.tn[i] == peer)
				return true;
		}
		return false;
	}

	public int degree()
	{
		int len = cache.length - 1;
		while (len >= 0 && cache[len] == null)
			len--;
		return len + 1;
	}

	public Node getNeighbor(int i)
	{
		return cache[i].n;
	}

	public void pack() {}

	public void onKill()
	{
		cache = null;
	}

	public String toString()
	{
		if( cache == null ) return "DEAD!";

		StringBuffer sb = new StringBuffer();
		sb.append(name + " ");

		for (int i = 0; i < degree(); ++i) {
			sb.append(" (" + cache[i].n.getIndex() + "," + cache[i].ts + ")");
		}
		return sb.toString();
	}

	public void nextCycle(Node lnode, int protocolID)
	{
		Node peerNode = getPeer();
		if (peerNode == null) {
			System.err.println("Newscast: no accessible peer " + lnode.getID() + " " + this);
			return;
		}
		Transport tr = (Transport) lnode.getProtocol(c.tid);
		NewscastMessage messsage = new NewscastMessage(lnode, true, cache);
		tr.send(lnode, peerNode, messsage, protocolID);
	}
}

