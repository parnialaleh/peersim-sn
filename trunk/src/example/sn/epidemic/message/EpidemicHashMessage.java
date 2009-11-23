package example.sn.epidemic.message;

import peersim.core.Node;

public class EpidemicHashMessage extends EpidemicMessage
{
	private int hashcode = 0;

	public EpidemicHashMessage(boolean status, Node n, int hashcode) {
		super(status, true, n);
		this.hashcode = hashcode;
	}
	
	public int getHashcode()
	{
		return this.hashcode;
	}


}
