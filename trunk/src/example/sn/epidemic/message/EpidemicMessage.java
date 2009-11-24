package example.sn.epidemic.message;

import peersim.extras.am.epidemic.bcast.InfectionMessage;

public abstract class EpidemicMessage extends InfectionMessage
{
	private boolean isHash = false;
	
	public EpidemicMessage(boolean status, boolean isHash) {
		super(status);
		this.isHash = isHash;
	}
	
	public boolean isHash()
	{
		return this.isHash;
	}
	
	public void setHash(boolean isHash)
	{
		this.isHash = isHash;
	}
	


}
