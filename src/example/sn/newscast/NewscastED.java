package example.sn.newscast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import example.sn.node.SNNode;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.IdleProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class NewscastED implements EDProtocol, CDProtocol, LinkableSN
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

	public NewscastED(String n)
	{
		final int cachesize = Configuration.getInt(n + "." + PAR_CACHE);
		ff_communication = Configuration.getBoolean(n + "." + PAR_COMMUNICATE, true);
		idle_protocol = Configuration.getPid(n + "." + PAR_IDLE_PROTOCOL);
		if (NewscastED.tn == null || NewscastED.tn.length < cachesize) {
			NewscastED.tn = new NodeEntry[cachesize];
		}
		cache = new NodeEntry[cachesize];
	}

	public Object clone()
	{
		NewscastED sn = null;
		try { sn = (NewscastED) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		sn.cache = new NodeEntry[cache.length];

		//TODO needed?
		//System.arraycopy(cache, 0, sn.cache, 0, cache.length);
		return sn;
	}

	public Node getPeer()
	{
		final int d = degree();
		if (d == 0)
			return null;
		int index = CommonState.r.nextInt(d);
		Node result = cache[index].n;

		//		System.out.println(result.getID() + " " + cache[index].type);
		if ((result.isUp()) && ((cache[index].type == FRIEND) || ff_communication))
			return result;

		// proceed towards older entries
		for (int i = index + 1; i < d; ++i){
			//			System.out.println(cache[i].n.getID() + " " + cache[i].n.isUp() + " " + cache[i].type);
			if (cache[i].n.isUp() && ((cache[i].type == FRIEND) || ff_communication))
				return cache[i].n;
		}

		// proceed towards younger entries
		for (int i = index - 1; i >= 0; --i)
			if (cache[i].n.isUp() && ((cache[i].type == FRIEND) || ff_communication))
				return cache[i].n;

		// no accessible peer
		return null;
	}

	private Node getPeer(Node node)
	{
		IdleProtocol idle = (IdleProtocol)node.getProtocol(idle_protocol);

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

	private void merge(Node thisNode, NewscastED peer, Node peerNode)
	{
		int i1 = 0;
		int i2 = 0;
		boolean first;
		boolean lastTieWinner = CommonState.r.nextBoolean();
		int i = 1;

		NodeEntry peerFriends[] = peer.getFriends();

		//merge only with the peer friends that I already know
		HashSet<NodeEntry> tmp = new HashSet<NodeEntry>(Arrays.asList(peerFriends));

		int k = 0;
		for (k=cache.length-1; cache[k] == null && k > 0; k--);
		NodeEntry lcache[] = new NodeEntry[++k];
		System.arraycopy(cache, 0, lcache, 0, k);
		tmp.addAll(Arrays.asList(lcache));

		if (tmp.size() >= cache.length) {
			NodeEntry[] temp = new NodeEntry[tmp.size()];
			System.arraycopy(cache, 0, temp, 0, cache.length);
			cache = temp;
		}
		if (tmp.size() >= peer.cache.length) {
			NodeEntry[] temp = new NodeEntry[tmp.size()];
			System.arraycopy(peer.cache, 0, temp, 0, peer.cache.length);
			peer.cache = temp;
		}
		

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
				if (cache[i1].n != peerNode && !NewscastED.contains(i, cache[i1].n, tn)) {
					tn[i] = (NodeEntry)cache[i1].clone();
					i++;
				}
				i1++;
			} else {
				if (peerFriends[i2].n != thisNode
						&& !NewscastED.contains(i, peerFriends[i2].n, tn)) {
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
				if (cache[i1].n != peerNode && !NewscastED.contains(i, cache[i1].n, tn)) {
					tn[i] = (NodeEntry)cache[i1].clone();
					i++;
				}
			}

			for (; i2 < d2 && i < cache.length; ++i2) {
				if (peerFriends[i2].n != thisNode
						&& !NewscastED.contains(i, peerFriends[i2].n, tn)) {
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

	private static boolean contains(int size, Node peer, NodeEntry[] list)
	{
		for (int i = 0; i < size && list[i] != null; i++) {
			if (list[i].n == peer)
				return true;
		}
		return false;
	}

	public NodeEntry[] getFriends()
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
		nextCycle(node, pid);		
	}

	public void nextCycle(Node node, int protocolID) 
	{
		Node peerNode = getPeer(node);
		if (peerNode == null) {
			System.err.println("Newscast: no accessible peer " + node.getID() + " " + this);
			return;
		}

		NewscastED peer = (NewscastED) (peerNode.getProtocol(protocolID));

//		if (node.getID() == 180){
//			System.out.println(node.getID() + "X" + peerNode.getID());
//			System.out.println(this);
//			System.out.println(peer);
//		}

		merge(node, peer, peerNode);
		
//		if (node.getID() == 180){
//			System.out.println(node.getID() + "X" + peerNode.getID());
//			System.out.println(this);
//			System.out.println(peer);
//			
//			String fType = null;
//			for (int i = 1; i < NewscastED.tn.length && NewscastED.tn[i] != null; ++i) {
//				fType = (NewscastED.tn[i].type == FRIEND)? "F": "FF";
//				System.out.print(" (" + NewscastED.tn[i].n.getIndex() + "," + NewscastED.tn[i].ts + "," + fType + ")");
//			}
//			
//			System.out.println();
//		}
		
		NodeEntry[] currentNodeCache = new NodeEntry[NewscastED.tn.length];
		NodeEntry[] peerNodeCache = new NodeEntry[NewscastED.tn.length];

		for (int k = 1; k < NewscastED.tn.length && NewscastED.tn[k] != null; k++){
//			System.out.print(contains(NewscastED.tn[k].n) + "," + NewscastED.tn[k].n.getID() + " ");
			currentNodeCache[k] = (NodeEntry)NewscastED.tn[k].clone();
			peerNodeCache[k] = (NodeEntry)NewscastED.tn[k].clone();
			
			currentNodeCache[k].type = containsAsFriend(NewscastED.tn[k].n)? FRIEND : FRIEND_FRIEND;
			peerNodeCache[k].type = peer.containsAsFriend(NewscastED.tn[k].n)? FRIEND : FRIEND_FRIEND;
		}
		
		cache = new NodeEntry[currentNodeCache.length];
		peer.cache = new NodeEntry[peerNodeCache.length];
		System.arraycopy(currentNodeCache, 0, cache, 0, cache.length);
		System.arraycopy(peerNodeCache, 0, peer.cache, 0, peer.cache.length);
		//System.arraycopy(NewscastED.tn, 0, peer.cache, 0, peer.cache.length);

		// set first element
		cache[0] = new NodeEntry();
		peer.cache[0] = new NodeEntry();
		cache[0].ts = peer.cache[0].ts = CommonState.getIntTime();
		cache[0].type = peer.cache[0].type = FRIEND;
		cache[0].n = peerNode;
		peer.cache[0].n = node;

//		if (node.getID() == 180){ //61
//			System.out.println(node.getID() + " " + peerNode.getID());
//			System.out.println(this);
//			System.out.println(peer);
//		}
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
			if (cache[i].n == node)
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
			if ((cache[i].n == n) && (cache[i].type == FRIEND))
				return true;
		}
		return false;
	}



}
