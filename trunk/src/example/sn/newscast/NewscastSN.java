package example.sn.newscast;

import java.util.ArrayList;
import java.util.List;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class NewscastSN implements EDProtocol, CDProtocol, LinkableSN
{

	/**
	 * Initial cache size.
	 * @config
	 */
	private static final String PAR_CACHE = "cache";

	/**
	 * Communicate also with the friends of my friends.<br>
	 * If missing this field is true.
	 * @config
	 */
	private static final String PAR_COMMUNICATE = "ff_communication";

	/**
	 * Pid of the idle_protocol used to store the network state at time 0.<br>
	 * @config
	 */
	private static final String PAR_IDLE_PROTOCOL = "idle_protocol";

	private final boolean ff_communication;
	private final int idle_protocol;

	/** Temp array for merging. Its size is the same as the cache size. */
	private static NodeEntry[] tn;

	/** Neighbors currently in the cache */
	private NodeEntry[] cache;

	public NewscastSN(String n)
	{
		final int cachesize = Configuration.getInt(n + "." + PAR_CACHE);
		ff_communication = Configuration.getBoolean(n + "." + PAR_COMMUNICATE, true);
		
		idle_protocol = Configuration.getPid(n + "." + PAR_IDLE_PROTOCOL);
		if (NewscastSN.tn == null || NewscastSN.tn.length < cachesize) {
			NewscastSN.tn = new NodeEntry[cachesize];
		}
		cache = new NodeEntry[cachesize];
	}

	public Object clone()
	{
		NewscastSN sn = null;
		try { sn = (NewscastSN) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		sn.cache = new NodeEntry[cache.length];

		return sn;
	}
	
	private Node getPeer(boolean ff_communication)
	{
		final int d = degree();
		if (d == 0)
			return null;
		int index = CommonState.r.nextInt(d);
		Node result = cache[index].n;

		//		System.out.println(result.getID() + " " + cache[index].type);
		//if ((result.isUp()) && ((cache[index].type == FRIEND) || ff_communication))
		if ((result.isUp()) && (cache[index].type == FRIEND))
			return result;

		// proceed towards older entries
		for (int i = index + 1; i < d; ++i){
			//			System.out.println(cache[i].n.getID() + " " + cache[i].n.isUp() + " " + cache[i].type);
//			if (cache[i].n.isUp() && ((cache[i].type == FRIEND) || ff_communication))
			if((cache[i].n.isUp()) && (cache[i].type == FRIEND))
				return cache[i].n;
		}

		// proceed towards younger entries
		for (int i = index - 1; i >= 0; --i)
			if ((cache[i].n.isUp()) && (cache[i].type == FRIEND))
				return cache[i].n;
		
		if (ff_communication){
			if ((result.isUp()) && (cache[index].type == FRIEND_FRIEND))
				return result;

			for (int i = index + 1; i < d; ++i){
				if((cache[i].n.isUp()) && (cache[i].type == FRIEND_FRIEND))
					return cache[i].n;
			}
			
			for (int i = index - 1; i >= 0; --i)
				if ((cache[i].n.isUp()) && (cache[i].type == FRIEND_FRIEND))
					return cache[i].n;
		}

		// no accessible peer
		return null;
	}

	public Node getPeer()
	{
		return getPeer(ff_communication);
	}
	
	public Node getFriendPeer()
	{
		return getPeer(false);
	}

	private Node getPeer(Node node)
	{
		Linkable idle = (Linkable)node.getProtocol(idle_protocol);

		final int d = idle.degree();
		final int d1 = degree();

		if (CommonState.r.nextInt(d + d1) < d1){
			Node n = getPeer();
			if (n != null)
				return n;
		}

		//============== Node from Idle Protocol =====================
		if (d == 0)
			return null;

		int index = CommonState.r.nextInt(d);
		Node result = idle.getNeighbor(index);

		if (result.isUp())
			return result;

		for (int i = index + 1; i < d; ++i)
			if (idle.getNeighbor(i).isUp())
				return cache[i].n;

		for (int i = index - 1; i >= 0; --i)
			if (idle.getNeighbor(i).isUp())
				return cache[i].n;

		return null;
	}

	private void merge(Node thisNode, NewscastSN peer, Node peerNode)
	{
		int i1 = 0;
		int i2 = 0;
		boolean first;
		boolean lastTieWinner = CommonState.r.nextBoolean();
		int i = 1;

		NodeEntry peerFriends[] = peer.getFriends(thisNode);

		//merge only with the peer friends that I already know
		//HashSet<NodeEntry> tmp = new HashSet<NodeEntry>(Arrays.asList(peerFriends));

		//int k = 0;
//		for (k=cache.length-1; cache[k] == null && k > 0; k--);
//		NodeEntry lcache[] = new NodeEntry[++k];
//		System.arraycopy(cache, 0, lcache, 0, k);
//		tmp.addAll(Arrays.asList(lcache));
		
		int finalSize = cache.length;
		for (NodeEntry ne : peerFriends)
			if (!contains(ne.n))
				finalSize++;

		if (finalSize >= cache.length) {
			NodeEntry[] temp = new NodeEntry[finalSize];
			System.arraycopy(cache, 0, temp, 0, cache.length);
			cache = temp;
		}
//		if (finalSize >= peer.cache.length) {
//			NodeEntry[] temp = new NodeEntry[finalSize];
//			System.arraycopy(peer.cache, 0, temp, 0, peer.cache.length);
//			peer.cache = temp;
//		}
		

		final int d1 = degree();
		final int d2 = peerFriends.length;

		tn = new NodeEntry[cache.length];

		// merging two arrays
		while (i < cache.length && i1 < d1 && i2 < d2) {
			if (cache[i1].ts == peerFriends[i2].ts) {
				lastTieWinner = first = !lastTieWinner;
			} else {
				first = cache[i1].ts > peerFriends[i2].ts;
			}

			if (first) {
				if (cache[i1].n.getID() != peerNode.getID() && !NewscastSN.contains(1, i, cache[i1].n, tn)) {
					tn[i] = (NodeEntry)cache[i1].clone();
					i++;
				}
				i1++;
			} else {
				if (peerFriends[i2].n.getID() != thisNode.getID()
						&& !NewscastSN.contains(1, i, peerFriends[i2].n, tn)) {
					tn[i] = (NodeEntry)peerFriends[i2].clone();
					//if current node has peer.cache[i2].n in its cache then it is a friend otherwise a friend of a friend
					//tn[i].type = NewscastED.containsAsFriend(cache.length, peerFriends[i2].n, cache)? FRIEND : FRIEND_FRIEND;
					i++;
				}
				i2++;
			}
		}

		// if one of the original arrays got fully copied into
		// tn and there is still place, fill the rest with the other
		// array
		if (i < cache.length) {
			// only one of the for cycles will be entered

			for (; i1 < d1 && i < cache.length; ++i1) {
				if (cache[i1].n.getID() != peerNode.getID() && !NewscastSN.contains(1, i, cache[i1].n, tn)) {
					tn[i] = (NodeEntry)cache[i1].clone();
					i++;
				}
			}

			for (; i2 < d2 && i < cache.length; ++i2) {
				if (peerFriends[i2].n.getID() != thisNode.getID()
						&& !NewscastSN.contains(1, i, peerFriends[i2].n, tn)) {
					tn[i] = (NodeEntry)peerFriends[i2].clone();
//					tn[i].type = NewscastED.containsAsFriend(cache.length, peerFriends[i2].n, cache)? FRIEND : FRIEND_FRIEND;
					i++;
				}
			}
		}

		// if the two arrays were not enough to fill the buffer
		// fill in the rest with nulls
		if (i < cache.length) {
			for (; i < cache.length; ++i) {
				tn[i] = null;
			}
		}
	}

	private static boolean contains(int start, int size, Node peer, NodeEntry[] list)
	{
		for (int i = start; i < size; i++) {
			if (list[i].n.getID() == peer.getID()){
				return true;
			}
		}
		return false;
	}

	public NodeEntry[] getFriends(Node n)
	{
		List<NodeEntry> friends = new ArrayList<NodeEntry>();

		for (int i = 0; i < cache.length && cache[i] != null; i++){
			NodeEntry ne = cache[i];
			if (ne.type == FRIEND)
				friends.add(ne);
		}

		return friends.toArray(new NodeEntry[0]);
	}

	public void processEvent(Node node, int pid, Object event) {
//		nextCycle(node, pid);		
	}

	public void nextCycle(Node node, int protocolID) 
	{
		Node peerNode = getPeer(node);
		if (peerNode == null) {
			System.err.println("Newscast: no accessible peer " + node.getID() + " " + this);
			return;
		}

		NewscastSN peer = (NewscastSN) (peerNode.getProtocol(protocolID));
		int type = containsAsFriend(peerNode)? FRIEND : FRIEND_FRIEND;
		
//		if (node.getID() == 180){
//			System.out.println(node.getID() + "X" + peerNode.getID());
//			System.out.println(this);
//			System.out.println(peer);
//		}

		merge(node, peer, peerNode);
		NodeEntry[] currentNodeCache = new NodeEntry[NewscastSN.tn.length];
		for (int k = 1; k < NewscastSN.tn.length && NewscastSN.tn[k] != null; k++){
//			System.out.print(contains(NewscastED.tn[k].n) + "," + NewscastED.tn[k].n.getID() + " ");
			currentNodeCache[k] = (NodeEntry)NewscastSN.tn[k].clone();
			currentNodeCache[k].type = containsAsFriend(NewscastSN.tn[k].n)? FRIEND : FRIEND_FRIEND;
		}
		
		peer.merge(peerNode, this, node);
		NodeEntry[] peerNodeCache = new NodeEntry[NewscastSN.tn.length];
		for (int k = 1; k < NewscastSN.tn.length && NewscastSN.tn[k] != null; k++){
			peerNodeCache[k] = (NodeEntry)NewscastSN.tn[k].clone();
			peerNodeCache[k].type = peer.containsAsFriend(NewscastSN.tn[k].n)? FRIEND : FRIEND_FRIEND;
		}
		
//		if (node.getID() == 180){
//			System.out.println(node.getID() + "X" + peerNode.getID());
//			System.out.println(this);
//			System.out.println(peer);
//			
//			String fType = null;
//			System.out.println("ZXXXX");
//			for (int i = 1; i < NewscastED.tn.length && NewscastED.tn[i] != null; ++i) {
//				fType = (NewscastED.tn[i].type == FRIEND)? "F": "FF";
//				System.out.print(" (" + NewscastED.tn[i].n.getIndex() + "," + NewscastED.tn[i].ts + "," + fType + ")");
//			}
//			
//			System.out.println();
//		}
		
		cache = new NodeEntry[currentNodeCache.length];
		peer.cache = new NodeEntry[peerNodeCache.length];
		System.arraycopy(currentNodeCache, 0, cache, 0, cache.length);
		System.arraycopy(peerNodeCache, 0, peer.cache, 0, peer.cache.length);

		// set first element
		cache[0] = new NodeEntry();
		peer.cache[0] = new NodeEntry();
		cache[0].ts = peer.cache[0].ts = CommonState.getIntTime();
		cache[0].type = peer.cache[0].type = type;
		cache[0].n = peerNode;
		peer.cache[0].n = node;

//		if (node.getID() == 180){ //61
//			System.out.println(node.getID() + " " + peerNode.getID());
//			System.out.println(this);
//			System.out.println(peer);
//		}
		
//		if (cache.length > 200 )
//			System.out.println(node.getID() + " " + cache.length);
		
//		System.out.println(this);
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
		cache[i].type = FRIEND;
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
		String fType = null;

		for (int i = 0; i < degree(); ++i) {
			fType = (cache[i].type == FRIEND)? "F": "FF";
			sb.append(" (" + cache[i].n.getIndex() + "," + cache[i].ts + "," + fType + ")");
		}
		return sb.toString();
	}

	public boolean containsAsFriend(Node n)
	{
		for (int i = 0; i < cache.length && cache[i] != null; i++) {
			if ((cache[i].n.getID() == n.getID()) && (cache[i].type == FRIEND))
				return true;
		}
		return false;
	}

	public boolean containsAsFriend(Node lnode, Node n) {
		// TODO Auto-generated method stub
		return false;
	}

	public Node getFriendPeer(Node n) {
		// TODO Auto-generated method stub
		return null;
	}



}
