package example.sn.control;

import java.util.ArrayList;
import java.util.List;

import example.sn.EpidemicNews;
import example.sn.node.SNNode;

import peersim.config.Configuration;
import peersim.core.*;
import peersim.dynamics.NodeInitializer;

public class OscillatingSocialNetwork implements Control
{
	private static final String PAR_MAX_MAX = "maxsize.max";
	private static final String PAR_MAX_MIN = "maxsize.min";
	private static final String PAR_MIN_MAX = "minsize.max";
	private static final String PAR_MIN_MIN = "minsize.min";
	private static final String PAR_LONG_PERIOD = "longPeriod";
	
	private static final String PAR_PERIOD = "period";
	private static final String PAR_INIT = "init";
	private static final String PAR_NEWS_MANAGER = "news";

	private final int period;
	private final int longPeriod;
	private final int pidNews;
	private final int maxMaxSize;
	private final int maxMinSize;
	private final int minMaxSize;
	private final int minMinSize;
	
	private int minsize;
	private int maxsize;
	
	private final int size;
	
	private List<SNNode> offLineNodes = null;
	private List<SNNode> onLineNodes = null;
	private final NodeInitializer[] inits;

	public OscillatingSocialNetwork(String prefix)
	{
		size = Network.size();
		period = Configuration.getInt(prefix + "." + PAR_PERIOD);
		longPeriod = Configuration.getInt(prefix + "." + PAR_LONG_PERIOD);

		maxMaxSize = Configuration.getInt(prefix + "." + PAR_MAX_MAX);
		maxMinSize = Configuration.getInt(prefix + "." + PAR_MAX_MIN);
		minMaxSize = Configuration.getInt(prefix + "." + PAR_MIN_MAX);
		minMinSize = Configuration.getInt(prefix + "." + PAR_MIN_MIN);
		
		maxsize = maxMinSize;
		minsize = maxsize / minMinSize;
		this.pidNews = Configuration.getPid(prefix + "." + PAR_NEWS_MANAGER);
		
		offLineNodes = new ArrayList<SNNode>();
		onLineNodes = new ArrayList<SNNode>();
		for (int i = 0; i < Network.size(); i++)
			if (((SNNode)Network.get(i)).isOnline())
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
			
			System.err.println("ONLINE " + node.getID());
			
			for (int k = 0; k < inits.length; ++k) {
				inits[k].initialize(node);
			}
			((EpidemicNews)(node).getProtocol(pidNews)).setInfected(true);
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
		maxsize = (maxMaxSize + maxMinSize) / 2 + (int) (Math.sin(((double) time) / longPeriod * Math.PI) * ((maxMaxSize - maxMinSize) / 2));
		minsize = (minMaxSize + minMinSize) / 2 + (int) (Math.sin(((double) time) / longPeriod * Math.PI) * ((minMaxSize - minMinSize) / 2));
		
		maxsize = size*maxsize/100;
		minsize = size*minsize/100;
		
		int amplitude = (maxsize - minsize) / 2;
		int newsize = (maxsize + minsize) / 2 + 
		(int) (Math.sin(((double) time) / period * Math.PI) * amplitude);
		int diff = newsize - onLineNodes.size();
		
		System.out.println( " " + time + " OSCILLATING " + maxsize + " " + minsize + " " + newsize);
		
		if (diff < 0)
			remove(-diff);
		else
			add(diff);

		return false;
	}

}
