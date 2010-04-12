package example.sn.init;

import java.util.ArrayList;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import example.sn.linkable.LinkableSN;

public class WireNet implements Control
{
	private static final String PAR_LINKABLE = "linkable";
	private static final String PAR_IDLE = "idle";
	private static final String PAR_N = "n";

	private final int pid;
	private final int pIdle;
	private final int n;

	public WireNet(String prefix)
	{
		this.pid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		this.pIdle = Configuration.getPid(prefix + "." + PAR_IDLE);
		this.n = Configuration.getInt(prefix + "." + PAR_N);
	}

	public boolean execute()
	{
		for (int i = 0; i < Network.size(); i++){
			Node node = Network.get(i);
			LinkableSN idle = (LinkableSN)node.getProtocol(pIdle);
			Linkable gossip = (Linkable)node.getProtocol(pid);

			//Set<Node> set = new HashSet<Node>();

			List<Integer> s = new ArrayList<Integer>();
			
			for (int j = 0; j < idle.degree(); j++)
				s.add(j);
			
			for (int j = 0; j < Math.min(idle.degree(), n); j++)
				//gossip.addNeighbor(idle.getNeighbor(j));
				gossip.addNeighbor(idle.getNeighbor(s.remove(CommonState.r.nextInt(s.size()))));
			
			/*
			 if (idle.degree() <= n){
				set.add(idle.getNeighbor(0));
			}
			else
				while (set.size() < n){
					Node nd = idle.getPeer(node);
					if (nd.isUp())
						set.add(nd);
				}
			
			for (Node n : set.toArray(new Node[0]))
				cyclon.addNeighbor(n);
				*/
		}

		return false;
	}

}
