package example.sn.epidemic.message;

import peersim.extras.am.epidemic.bcast.InfectionMessage;

public abstract class EpidemicMessage extends InfectionMessage
{
	private boolean isHash = false;
	private boolean request = false;
	
	public EpidemicMessage(boolean status, boolean isHash, boolean request) {
		super(status);
		this.isHash = isHash;
		this.request = request;
	}
	
	public boolean isHash()
	{
		return this.isHash;
	}
	
	public void setHash(boolean isHash)
	{
		this.isHash = isHash;
	}
	
	public boolean isRequest()
	{
		return this.request;
	}
	


}
