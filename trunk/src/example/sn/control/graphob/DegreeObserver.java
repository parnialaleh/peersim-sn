package example.sn.control.graphob;

import java.util.HashSet;
import java.util.Set;

import example.sn.linkable.LinkableSN;
import example.sn.node.SNNode;
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
	private final static String PAR_IDLE = "idle";
	private static final String PAR_DEAD = "dead";

	private final int pid;
	private final int idle;
	private final String name;
	private final boolean dead;

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

	private class Entry2
	{
		long nodeId;
		int indegree;
	}

	public DegreeObserver(String prefix)
	{
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		this.idle = Configuration.getPid(prefix + "." + PAR_IDLE);
		this.name = prefix;
		this.dead = Configuration.contains(prefix + "." + PAR_DEAD);
	}

	/*private int indexOf(long nodeRealID)
	{
		for (int i = 0; i < Network.size(); i++)
			if (((SNNode)Network.get(i)).getRealID() == nodeRealID)
				return i;

		return -1;
	}*/

	public boolean execute()
	{
		IncrementalStats is = new IncrementalStats();

		for (int i = 0 ; i < Network.size(); i++)
			if (((SNNode)Network.get(i)).isOnline())
				calculeteInDegree(Network.get(i), is);

		System.out.println(" " + CommonState.getTime() + " " + name + "stats: " + is);

		if (!dead) return false;

		Entry2[] entry = new Entry2[Network.size()];
		for (int i = 0; i < Network.size(); i++)
			if (((SNNode)Network.get(i)).isOnline()){
				entry[i] = new Entry2();
				entry[i].indegree = 0;
				entry[i].nodeId = Network.get(i).getID();
			}

		int index = 0;
		int degreeToDead = 0;
		Set<Node> referredDeadNode = new HashSet<Node>();
		for (int i = 0; i < Network.size(); i++){
			Node n = Network.get(i);

			if (((SNNode)n).isOnline()){
				Linkable linkable = (Linkable)n.getProtocol(pid);
				for (int j = 0; j < linkable.degree(); j++)
					if ((index = indexOf(entry, linkable.getNeighbor(j).getID())) >= 0)
						entry[index].indegree++;
					else if (dead){
						degreeToDead++;
						referredDeadNode.add(linkable.getNeighbor(j));
					}
			}
		}

		System.out.println(CommonState.getIntTime() + " " + name + ": dead " + degreeToDead + " referred " + referredDeadNode.size());


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


	private int indexOf(Entry2[] entry, long id)
	{
		for (int i = 0; i < entry.length; i++)
			if (entry[i] != null && entry[i].nodeId == id)
				return i;
		return -1;
	}

	private void calculeteInDegree(Node rootNode, IncrementalStats is)
	{
		//Node rootNode = Network.get(indexOf(AddNews.getRoot()));
		int indegree = 0;
		int snInDegree = 0;
		for (int i = 0; i < Network.size(); i++){
			if (((LinkableSN)Network.get(i).getProtocol(idle)).contains(rootNode))
				snInDegree++;
		}

		LinkableSN linkable = (LinkableSN)rootNode.getProtocol(idle);
		Set<Node> set = new HashSet<Node>();
		for (int i = 0; i < linkable.degree(); i++){
			if (((SNNode)linkable.getNeighbor(i)).isOnline())
				set.add(linkable.getNeighbor(i));
			LinkableSN rLinkable = (LinkableSN)linkable.getNeighbor(i).getProtocol(idle);
			for (int j = 0; j < rLinkable.degree(); j++)
				if (((SNNode)rLinkable.getNeighbor(j)).isOnline())
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

		is.add(indegree);

		System.out.println(" " + CommonState.getTime() + " " + name + ": simulDegree " + indegree + " SnInDegree " + snInDegree + " indegree " + set.size());
	}

}