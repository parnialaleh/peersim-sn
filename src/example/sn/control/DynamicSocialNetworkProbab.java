package example.sn.control;

import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class DynamicSocialNetworkProbab implements Control
{
	
	private static final String PAR_PROBAB = "p";
	
	private final double p;
	
	public DynamicSocialNetworkProbab(String n)
	{
		p = Configuration.getDouble(n + "." + PAR_PROBAB);
	}

	public boolean execute() {

		for (int i = 0; i < Network.size(); i++){
			if (((SNNode)Network.get(i)).isUp())
				;
		}
		
		return false;
	}

}
