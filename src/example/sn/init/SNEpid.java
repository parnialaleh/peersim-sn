package example.sn.init;

import example.sn.EpidemicNews;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class SNEpid implements NodeInitializer
{
	private static final String PAR_IDLE = "news";
	private final int n;
	
	public SNEpid (String prefix)
	{
		this.n = Configuration.getPid(prefix + "." + PAR_IDLE);
	}

	public void initialize(Node node) {
		((EpidemicNews)(node).getProtocol(n)).setInfected(true);

	}

}
