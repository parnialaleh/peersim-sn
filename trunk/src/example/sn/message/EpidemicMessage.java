package example.sn.message;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.extras.am.epidemic.bcast.InfectionMessage;

public abstract class EpidemicMessage extends InfectionMessage implements Comparable<EpidemicMessage>
{
	private int eventTime = 0;
	private long nodeID = 0;
	
	public EpidemicMessage(boolean status, Node n) {
		super(status);
		this.eventTime = CommonState.getIntTime();
		this.nodeID = n.getID();
	}
	
	public int getEventTime()
	{
		return this.eventTime;
	}
	
	public long getID()
	{
		return this.nodeID;
	}
	
	public int compareTo(EpidemicMessage message)
	{
		if (message.eventTime < eventTime)
			return 1;
		else if (message.eventTime > eventTime)
			return -1;
		
		return 0;
	}

}
