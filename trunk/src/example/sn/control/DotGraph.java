package example.sn.control;

import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;

public class DotGraph implements Control
{
	private static final String PAR_PROTOCOL = "linkable";

	private final int pid;
	private final String name;
	
	public DotGraph(String n)
	{
		this.pid = Configuration.getPid(n + "." + PAR_PROTOCOL);
		this.name = n;
	}
	
	public boolean execute()
	{
		System.out.println(name + " digraph G{");
		
		for (int i = 0; i < Network.size(); i++){
			SNNode node = (SNNode)Network.get(i);
			Linkable l = (Linkable)node.getProtocol(pid);
			for (int j = 0; j < l.degree(); j++)
				System.out.println(name + " " + node.getID() + " -> " + l.getNeighbor(j).getID());
		}
		
		System.out.println(name + " }");
		
		return false;
	}

}
