package example.sn.epidemic.message;

import java.util.List;

import peersim.extras.am.epidemic.bcast.InfectionMessage;

public class EpidemicHashMessage extends InfectionMessage
{
	private List<News> list = null;


	public EpidemicHashMessage(boolean status, List<News> list) {
		super(status);
		this.list = list;
	}
	
	public List<News> getList()
	{
		return this.list;
	}

}
