package example.sn.epidemic.message;

import peersim.core.CommonState;

public abstract class News
{
	
	private int eventTime = 0;
	
	public News()
	{
		this.eventTime = CommonState.getIntTime();
	}
	
	public int getEventTime()
	{
		return this.eventTime;
	}

}
