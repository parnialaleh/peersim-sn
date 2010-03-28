package example.dynamics;

import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class WireGraph implements Control
{
	private static final String PAR_LINKABLE = "protocol";
	private static final String PAR_N = "n";
	
	private final int protocol;
	private final int n;
	
	public WireGraph(String n)
	{
		this.protocol = Configuration.getPid(n + "." + PAR_LINKABLE);
		this.n = Configuration.getInt(n + "." + PAR_N);
	}


	public boolean execute()
	{
		for (int i = 0; i < Network.size(); i++){
			Node node = Network.get(i);
			Linkable linkable = (Linkable)node.getProtocol(protocol);
			
			Set<Node> set = new HashSet<Node>();
			while (set.size() < n){
				int index = CommonState.r.nextInt(Network.size());
				if (index != i)
					set.add(Network.get(index));
			}
			
			for (Node n : set.toArray(new Node[0]))
				linkable.addNeighbor(n);
		}
		return false;
	}
}
