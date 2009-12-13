package example.sn.epidemic.message;

import peersim.extras.am.epidemic.bcast.InfectionMessage;

public class EpidemicHashMessage extends InfectionMessage
{
	private int hashcode = 0;


	public EpidemicHashMessage(boolean status, int hashcode) {
		super(status);
		this.hashcode = hashcode;
	}
	
	public int getHashcode()
	{
		return this.hashcode;
	}

}
