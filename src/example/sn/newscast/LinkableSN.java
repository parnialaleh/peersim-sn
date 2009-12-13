package example.sn.newscast;

import peersim.core.Linkable;
import peersim.core.Node;

public interface LinkableSN extends Linkable
{
	static final int FRIEND = 1;
	static final int FRIEND_FRIEND = 2;
	
	/**
	 * Search in the cache the node n and check if it is a friend
	 * 
	 * @param n Note to search
	 * @return true if n is found and is a friend, false otherwise
	 */
	public boolean containsAsFriend(Node n);

}
