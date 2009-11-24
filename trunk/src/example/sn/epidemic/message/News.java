package example.sn.epidemic.message;

import peersim.core.CommonState;
import peersim.core.Node;

public abstract class News implements Comparable<News>
{
	
	private int eventTime = 0;
	private Node node;
	
	public News(Node n)
	{
		this.node = n;
		this.eventTime = CommonState.getIntTime();
	}
	
	public int getEventTime()
	{
		return this.eventTime;
	}
	
	public int compareTo(News message)
	{
		if (message.eventTime < eventTime)
			return 1;
		else if (message.eventTime > eventTime)
			return -1;
		
		return 0;
	}
	
	public Node getNode()
	{
		return this.node;
	}

}
