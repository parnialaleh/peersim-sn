package example.sn.control.graphob;

import example.sn.linkable.LinkableSN;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class DegreeObserver implements Control
{
	private final static String PAR_PID = "protocol";
	private final static String PAR_IDLE = "idle";

	private final int pid;
	private final int idle;
	private final String name;
	
	protected class Entry{
		public int inDegree;
		public int snInDegree;
		
		public Entry()
		{
			this.inDegree = 0;
			this.snInDegree = 0;
		}
		
		public Entry(int inDegree, int snInDegree)
		{
			this.inDegree = inDegree;
			this.snInDegree = snInDegree;
		}
		
		public void incInDegree()
		{
			this.inDegree++;
		}
		
		public void incSnInDegree()
		{
			this.snInDegree++;
		}
	}
	
	public DegreeObserver(String prefix)
	{
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		this.idle = Configuration.getPid(prefix + "." + PAR_IDLE);
		this.name = prefix;
	}
		
	public boolean execute()
	{
		Entry[] entry = new Entry[Network.size()];
		for (int i = 0; i < Network.size(); i++){
			entry[i] = new Entry();
		}
		
		for (int i = 0; i < Network.size(); i++){
			Node n = Network.get(i);
			
			if (n.isUp()){
				LinkableSN linkable = (LinkableSN)n.getProtocol(pid);
				for (int j = 0; j < linkable.degree(); j++)
					entry[((Long)linkable.getNeighbor(j).getID()).intValue()].incInDegree();
				
				linkable = (LinkableSN)n.getProtocol(idle);
				for (int j = 0; j < linkable.degree(); j++)
					entry[((Long)linkable.getNeighbor(j).getID()).intValue()].incSnInDegree();
			}
		}
		
		for (int i = 0; i < Network.size(); i++){
			System.out.println(" " + CommonState.getTime() + " " + name + ": " + entry[i].inDegree + " SnInDegree " + entry[i].snInDegree + " " + ((float)entry[i].inDegree / (float)entry[i].snInDegree));
		}
		
		return false;
	}

}
