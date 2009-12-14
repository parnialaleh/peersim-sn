package example.sn.control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.sn.node.SNNode;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class DynamicSocialNetwork implements Control
{
	private static final String PAR_MIN_NETWORK_SIZE = "minsize";
	private static final String PAR_MAX_NETWORK_SIZE = "maxsize";
	
	private final int minNetworkSize;
	private final int maxNetworkSize;
	
	public DynamicSocialNetwork(String n)
	{
		this.minNetworkSize = Configuration.getInt(n + "." + PAR_MIN_NETWORK_SIZE);
		this.maxNetworkSize = Configuration.getInt(n + "." + PAR_MAX_NETWORK_SIZE);
	}
	
	private void add(int n, List<Node> offLineNodes)
	{		
		final int size = offLineNodes.size();
		Set<Integer> s = new HashSet<Integer>();
		int i;
		while (s.size() < n){
			i = CommonState.r.nextInt(size);
			if (!s.contains(i)){
				((SNNode)offLineNodes.get(i)).setOnline(true);
				s.add(i);
			}
		}
	}
	
	private void remove(int n, List<Node> onLineNodes)
	{		
		final int size = onLineNodes.size();
		Set<Integer> s = new HashSet<Integer>();
		int i;
		while (s.size() < n){
			i = CommonState.r.nextInt(size);
			if (!s.contains(i)){
				System.out.print(" " + onLineNodes.get(i).getID());
				((SNNode)onLineNodes.get(i)).setOnline(false);
				s.add(i);
			}
		}
		
		System.out.println();
	}

	public boolean execute()
	{
		final int size = Network.size();
		final int minsize = Math.min(size, minNetworkSize);
		final int maxsize = Math.min(size, maxNetworkSize);
		
		List<Node> offLineNodes = new ArrayList<Node>();
		List<Node> onLineNodes = new ArrayList<Node>();
		for (int i = 0; i < size; i++)
			if (Network.get(i).isUp())
				onLineNodes.add(Network.get(i));
			else
				offLineNodes.add(Network.get(i));
		
		final int newsize = CommonState.r.nextInt(maxsize+1 - minsize) + minsize;
		final int add = newsize - onLineNodes.size();
				
		if (add > 0)
			add(add, offLineNodes);
		if (add < 0)
			remove(-add, onLineNodes);

//		System.out.println(add + " " + maxsize + " " + minsize + " " + newsize);

		
		return false;
	}

}
