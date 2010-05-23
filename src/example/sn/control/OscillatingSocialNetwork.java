package example.sn.control;

import java.util.ArrayList;
import java.util.List;

import example.sn.node.SNNode;

import peersim.config.Configuration;
import peersim.core.*;
import peersim.dynamics.NodeInitializer;

public class OscillatingSocialNetwork implements Control
{
	//private static final String PAR_MAX = "maxsize";
	private static final String PAR_MIN = "minsize";
	private static final String PAR_PERIOD = "period";
	private static final String PAR_INIT = "init";

	private final int period;
	private final int minsize;
	private final int maxsize;
	
	private List<SNNode> offLineNodes = null;
	private List<SNNode> onLineNodes = null;
	private final NodeInitializer[] inits;

	public OscillatingSocialNetwork(String prefix)
	{
		period = Configuration.getInt(prefix + "." + PAR_PERIOD);
		maxsize = Network.size();//Configuration.getInt(prefix + "." + PAR_MAX, Integer.MAX_VALUE);
		minsize = maxsize / Configuration.getInt(prefix + "." + PAR_MIN, 0);
		
		offLineNodes = new ArrayList<SNNode>();
		onLineNodes = new ArrayList<SNNode>();
		for (int i = 0; i < Network.size(); i++)
			if (Network.get(i).isUp())
				onLineNodes.add((SNNode)Network.get(i));
			else
				offLineNodes.add((SNNode)Network.get(i));
		
		Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
		inits = new NodeInitializer[tmp.length];
		for (int i = 0; i < tmp.length; ++i)
		{
			inits[i] = (NodeInitializer) tmp[i];
		}
	}

	protected void add(int n)
	{
		for (int i = 0; i < n; ++i) {
			int j = CommonState.r.nextInt(offLineNodes.size());
			SNNode node = offLineNodes.remove(j);
			node.setOnline(true);
			onLineNodes.add(node);
			for (int k = 0; k < inits.length; ++k) {
				inits[k].initialize(node);
			}
		}
	}

	protected void remove(int n)
	{
		for (int i = 0; i < n; ++i) {
			int j = CommonState.r.nextInt(onLineNodes.size());
			SNNode node = onLineNodes.remove(j);
			node.setOnline(false);
			offLineNodes.add(node);	
		}
	}

	public boolean execute()
	{
		long time = CommonState.getTime();
		int amplitude = (maxsize - minsize) / 2;
		int newsize = (maxsize + minsize) / 2 + 
		(int) (Math.sin(((double) time) / period * Math.PI) * amplitude);
		int diff = newsize - onLineNodes.size();
		
		System.err.println(diff);
		
		if (diff < 0)
			remove(-diff);
		else
			add(diff);

		return false;
	}

}
