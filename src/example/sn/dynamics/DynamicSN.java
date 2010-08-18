package example.sn.dynamics;

import java.util.ArrayList;
import java.util.List;

import example.sn.node.SNNode;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class DynamicSN implements Control {

	// --------------------------------------------------------------------------
	// Parameters
	// --------------------------------------------------------------------------
	private static final String PAR_REMOVE = "remove";

	// --------------------------------------------------------------------------
	// Fields
	// --------------------------------------------------------------------------

	protected final double remove;

	public DynamicSN(String prefix)
	{
		remove = Configuration.getDouble(prefix + "." + PAR_REMOVE);
	}

	public final boolean execute()
	{
		if (remove <= 0)
			return false;

		List<Node> onLINE = new ArrayList<Node>();
		for (int i = 0; i < Network.size(); i++)
			onLINE.add(Network.get(i));
		
		int removed = 0;
		while (removed < remove){
			((SNNode)onLINE.remove(CommonState.r.nextInt(onLINE.size()))).setOnline(false);
			removed++;
		}
		
		return false;
	}

	// --------------------------------------------------------------------------


}
