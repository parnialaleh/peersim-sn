package example.sn.epidemic.message;

import peersim.core.CommonState;
import peersim.core.Node;

public abstract class News implements Comparable<News>
{
	private int eventTime = 0;
	private Node sourceNode;
	private Node destNode;
	
	public News(Node sourceNode, Node destNode)
	{
		this.sourceNode = sourceNode;
		this.destNode = destNode;
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
	
	public Node getSourceNode()
	{
		return this.sourceNode;
	}
	
	public Node getDestNode()
	{
		return this.destNode;
	}

}
