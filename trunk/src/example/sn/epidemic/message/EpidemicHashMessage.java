package example.sn.epidemic.message;

public class EpidemicHashMessage extends EpidemicMessage
{
	private int hashcode = 0;

	public EpidemicHashMessage(boolean status, int hashcode) {
		super(status, true);
		this.hashcode = hashcode;
	}
	
	public int getHashcode()
	{
		return this.hashcode;
	}


}
