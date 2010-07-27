package example.sn.epidemic.control;

import example.sn.EpidemicNews;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;

public class AntiEntrophy implements CDProtocol {

	private static final String PAR_PROT_EPIDEMIC = "epidemic";
	
	private final int pidEpidemic;
	
	public AntiEntrophy(String n)
	{
		this.pidEpidemic = Configuration.getPid(n + "." + PAR_PROT_EPIDEMIC);
	}
	
	@Override
	public Object clone()
	{
		AntiEntrophy a = null;
		try { a = (AntiEntrophy) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		
		return a;
	}

	public void nextCycle(Node node, int protocolID)
	{
		((EpidemicNews)node.getProtocol(pidEpidemic)).setInfected(true);
	}

}
