package example.sn.epidemic.message;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.extras.am.epidemic.bcast.InfectionMessage;

public abstract class EpidemicMessage extends InfectionMessage implements Comparable<EpidemicMessage>
{
	private int eventTime = 0;
	private long nodeID = 0;
	private boolean isHash = false;
	
	public EpidemicMessage(boolean status, boolean isHash, Node n) {
		super(status);
		this.eventTime = CommonState.getIntTime();
		this.nodeID = n.getID();
		this.isHash = isHash;
	}
	
	public int getEventTime()
	{
		return this.eventTime;
	}
	
	public long getID()
	{
		return this.nodeID;
	}
	
	public boolean isHash()
	{
		return this.isHash;
	}
	
	public void setHash(boolean isHash)
	{
		this.isHash = isHash;
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
