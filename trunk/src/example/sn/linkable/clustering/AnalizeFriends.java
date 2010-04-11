package example.sn.linkable.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peersim.core.CommonState;
import peersim.core.Node;
import example.sn.control.graphob.ConnectivityObserver;
import example.sn.control.graphob.UnionFind;
import example.sn.control.graphob.UpperGraph;
import example.sn.linkable.LinkableSN;
import example.sn.node.SNNode;

public class AnalizeFriends extends ConnectivityObserver
{
	private SNNode lnode = null;
	protected SNNode[] node;

	/**
	 * This class use pidIdle protocol to build the network
	 * and pidGossip to return the list of known nodes
	 * 
	 * @param pidGossip pid of the gossip protocol
	 * @param pidIdle pid of the idle protocol used only as "friend list storage"
	 * @param lnode localNode
	 */
	public AnalizeFriends(int pidGossip, int pidIdle, SNNode lnode)
	{
		super(pidGossip, pidIdle);
		this.lnode = lnode;
	}

	private int addNode(Node nd, int nodes, Set<SNNode> node)
	{
		if (nd.getID() != CommonState.getNode().getID()){
			nodes++;
			node.add((SNNode)nd);
		}
		return nodes;
	}

	private void createNodeGraphList(int pid, Set<SNNode> node)
	{
		int i = 0;
		int j = 0;
		LinkableSN linkable = null;
		LinkableSN linkable2 = null;
		Node nd = null;

		linkable = (LinkableSN)lnode.getProtocol(pid);
		for (i = 0; i < linkable.degree(); ++i){
			nd = linkable.getNeighbor(i);
			nodes = addNode(nd, nodes, node);

			linkable2 = (LinkableSN)nd.getProtocol(pid);

			for (j = 0; j < linkable2.degree(); j++)
				nodes = addNode(linkable2.getNeighbor(j), nodes, node);
		}
	}
	
	private void createGraph(int pid, List<SNNode> nodeList)
	{
		int i = 0;
		int j = 0;
		LinkableSN linkable = null;
		LinkableSN linkable2 = null;
		Node nd = null;
		Node nd2 = null;


		linkable = (LinkableSN)lnode.getProtocol(pid);
		for (i = 0; i < linkable.degree(); ++i){
			nd = linkable.getNeighbor(i);
			linkable2 = (LinkableSN)nd.getProtocol(pid);

			for (j = 0; j < linkable2.degree(); j++){
				nd2 = linkable2.getNeighbor(j);
				//if (((SNNode)nd2).isUp() && nd2.getID() != CommonState.getNode().getID()){
				if (nd2.getID() != CommonState.getNode().getID())
					ug.addEdge(nodeList.indexOf(nd), nodeList.indexOf(nd2));
			}
		}	
	}

	private void graphInit()
	{
		//create the list of nodes
		Set<SNNode> nodesSet = new HashSet<SNNode>();
		createNodeGraphList(pidIdle, nodesSet);
		//createNodeGraphList(pidGossip, nodesSet);

		List<SNNode> nodeList = new ArrayList<SNNode>();
		for (SNNode n : nodesSet.toArray(new SNNode[0]))
			nodeList.add(n);

		nodes = nodeList.size();
		ug = new UpperGraph(nodes);
		n = new int[nodes];
		color = new int[nodes];
		parent = new int[nodes];
		dt = new int[nodes];
		ft = new int[nodes];
		node = nodeList.toArray(new SNNode[0]);

		createGraph(pidIdle, nodeList);
		//createGraph(pidGossip, nodeList);
	}

	private List<List<Node>> treeVisit() {
		
		List<List<Node>> cluster = new ArrayList<List<Node>>();
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		int i = 0;
		int pos = 0;
		Integer v = null;
		UnionFind uf = new UnionFind(nodes, parent);

		for (i = 0; i < nodes; ++i) {
			v = hm.get(uf.findSet(i));
			pos = uf.findSet(i);
			
			while (cluster.size() <= pos)
				cluster.add(new ArrayList<Node>());
			cluster.get(pos).add(node[i]);
				
			if (v == null)
				hm.put(pos, new Integer(1));
			else
				hm.put(pos, new Integer(v.intValue() + 1));
		}
		
		return cluster;
	}
	
	// ------------------------------------------
	// quick sort
	private void swap(int i, int r) {
		int dttmp = dt[i];
		dt[i] = dt[r];
		dt[r] = dttmp;

		int fttpm = ft[i];
		ft[i] = ft[r];
		ft[r] = fttpm;

		int ntmp = n[i];
		n[i] = n[r];
		n[r] = ntmp;
		
		SNNode nd = node[i];
		node[i] = node[r];
		node[r] = nd;
	}

	private int partition(int p, int r) {
		int pivot = ft[r];
		int i = p;

		for (int j = p; j < r; ++j)
			if (ft[j] >= pivot) {
				swap(i, j);
				i++;
			}
		swap(i, r);
		return i;
	}

	private void sort(int p, int r) {
		int q;

		if (p < r) {
			q = partition(p, r);
			sort(p, q - 1);
			sort(q + 1, r);
		}
	}
	// ------------------------------------------

	public List<List<Node>> analize()
	{
		// inizializzazione grafo della rete
		graphInit();
		// esecuzione dfs
		dfs();
		// ordinamento in ordine decrescente di ft
		sort(0, nodes - 1);
		// esecuzione dfs su G' (grafo trasposto)
		dfsT();
		// visita dell'albero generato dall'ultima dfs
		return treeVisit();		
	}



}
