package example.reports;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class DegreeObserver implements Control
{
	private final static String PAR_PID = "protocol";
	private static final String PAR_START_PROTOCOL = "starttime";
	private static final String PAR_END_PROTOCOL = "endtime";

	private final int pid;
	private final String name;
	private final long startTime;
	private final long endTime;
	
	public DegreeObserver(String prefix)
	{
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		this.startTime = Configuration.getLong(prefix + "." + PAR_START_PROTOCOL, Long.MIN_VALUE);
		this.endTime = Configuration.getLong(prefix + "." + PAR_END_PROTOCOL, Long.MAX_VALUE);
		this.name = prefix;
	}
		
	public boolean execute()
	{
		if ((CommonState.getTime() >= endTime) || (CommonState.getTime() < startTime))
			return false;
		
		int[] entry = new int[Network.size()];
		for (int i = 0; i < Network.size(); i++){
			entry[i] = 0;
		}
		
		for (int i = 0; i < Network.size(); i++){
			Node n = Network.get(i);
			
			if (n.isUp()){
				Linkable linkable = (Linkable)n.getProtocol(pid);
				for (int j = 0; j < linkable.degree(); j++)
					entry[((Long)linkable.getNeighbor(j).getID()).intValue()]++;
			}
		}
		
		IncrementalStats is = new IncrementalStats();
		
		for (int i = 0; i < Network.size(); i++){
			System.out.println(CommonState.getTime() + " " + name + ": InDegree " + entry[i]);
			is.add(entry[i]);
		}
		
		//System.out.println(CommonState.getIntTime() + " " + name + ": size " + Network.size() + " " + is);
		
		return false;
	}

}
