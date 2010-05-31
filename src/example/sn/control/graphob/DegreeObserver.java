package example.sn.control.graphob;

import java.util.HashSet;
import java.util.Set;

import example.sn.epidemic.control.AddNews;
import example.sn.linkable.LinkableSN;
import example.sn.node.SNNode;
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

	private int indexOf(long nodeRealID)
	{
		for (int i = 0; i < Network.size(); i++)
			if (((SNNode)Network.get(i)).getRealID() == nodeRealID)
				return i;

		return -1;
	}

	public boolean execute()
	{
		int indegree = 0;
		Node rootNode = Network.get(indexOf(AddNews.getRoot()));

		int indegree2 = 0;
		int snInDegree = 0;
		for (int i = 0; i < Network.size(); i++){
			if (((LinkableSN)Network.get(i).getProtocol(idle)).contains(rootNode))
				snInDegree++;
			if (Network.get(i).isUp())
				if (((LinkableSN)Network.get(i).getProtocol(pid)).contains(rootNode))
					indegree2++;

		}

		LinkableSN linkable = (LinkableSN)rootNode.getProtocol(idle);
		Set<Node> set = new HashSet<Node>();
		for (int i = 0; i < linkable.degree(); i++){
			if (linkable.getNeighbor(i).isUp())
				set.add(linkable.getNeighbor(i));
			LinkableSN rLinkable = (LinkableSN)linkable.getNeighbor(i).getProtocol(idle);
			for (int j = 0; j < rLinkable.degree(); j++)
				if (rLinkable.getNeighbor(j).isUp())
					set.add(rLinkable.getNeighbor(j));
		}
		for (Node n : set)
			if (((LinkableSN)n.getProtocol(pid)).contains(rootNode))
				indegree++;
		/*for (int i = 0; i < linkable.degree(); i++){
			LinkableSN rLinkable = (LinkableSN)linkable.getNeighbor(i).getProtocol(pid);
			if (rLinkable.contains(rootNode))
				indegree++;
			for (int j = 0; j < rLinkable.degree(); j++)
				if (((LinkableSN)rLinkable.getNeighbor(j).getProtocol(pid)).contains(rootNode))
					indegree++;
		}*/

		System.out.println(" " + CommonState.getTime() + " " + name + ": " + indegree + " " + indegree2 + " SnInDegree " + snInDegree + " " + set.size());

		/*Entry[] entry = new Entry[Network.size()];
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
		}*/

		return false;
	}

}
