package example.sn.linkable;

import java.util.ArrayList;
import java.util.List;

import example.sn.newscast.NodeEntry;

import peersim.core.Linkable;
import peersim.core.Node;

public abstract class LinkableSN implements Linkable
{
	public static final int FRIEND = 1;
	public static final int FRIEND_FRIEND = 2;
	
	/**
	 * Search in the cache the node n and check if it is a friend
	 * 
	 * @param n Note to search
	 * @return true if n is found and is a friend, false otherwise
	 */
	public abstract boolean containsAsFriend(Node lnode, Node n);
	
	/**
	 * 
	 * @param lnode
	 * @param n
	 * @return a node in the cache on lnode s.t. it is a friend of n
	 */
	public abstract Node getFriendPeer(Node lnode, Node n);
	
	public abstract NodeEntry[] getFriends(Node lnode, Node n);
	
	public abstract Node getPeer(Node node);
	
	public abstract Node[] getNodes(Node node);
	
	
	//----------------------
	protected NodeEntry[] getFriends(NodeEntry[] neighbors)
	{
		List<NodeEntry> friends = new ArrayList<NodeEntry>();
		for (int i = 0; i < neighbors.length && neighbors[i] != null; i++){
			NodeEntry ne = neighbors[i];
			if (ne.type == FRIEND) 
				friends.add(ne);
		}
		return friends.toArray(new NodeEntry[0]);
	}

}
