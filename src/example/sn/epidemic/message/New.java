package example.sn.epidemic.message;

import peersim.core.CommonState;

public abstract class New
{
	
	private int eventTime = 0;
	
	public New()
	{
		this.eventTime = CommonState.getIntTime();
	}
	
	public int getEventTime()
	{
		return this.eventTime;
	}

}
