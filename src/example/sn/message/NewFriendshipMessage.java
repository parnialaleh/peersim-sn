package example.sn.message;

import peersim.core.Node;

public class NewFriendshipMessage extends EpidemicMessage
{

	public NewFriendshipMessage(boolean status, Node node)
	{
		super(status, node);
	}

}
