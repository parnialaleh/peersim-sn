package example.sn.newscast.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peersim.core.CommonState;
import peersim.core.Node;
import example.sn.graphob.ConnectivityObserver;
import example.sn.graphob.UnionFind;
import example.sn.graphob.UpperGraph;
import example.sn.newscast.LinkableSN;
import example.sn.node.SNNode;

public class AnalizeFriends extends ConnectivityObserver
{
	private SNNode lnode = null;
	protected SNNode[] node;

	public AnalizeFriends(int pidNcast, int pidIdle, SNNode lnode)
	{
		super(pidNcast, pidIdle);
		this.lnode = lnode;
	}

	private int addNode(Node nd, int nodes, Set<SNNode> node)
	{
		if (nd.getID() == 4212)
			System.err.println("XXXXXXXXXXXXXxx");
		if (nd.isUp() && nd.getID() != CommonState.getNode().getID()){
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
				if (((SNNode)nd2).isUp() && nd2.getID() != CommonState.getNode().getID()){
					ug.addEdge(nodeList.indexOf(nd), nodeList.indexOf(nd2));
				}
			}
		}	
	}

	private void graphInit()
	{
		int i = 0;
		int j = 0;
		SNNode tmp = null;
		LinkableSN linkable = null;
		int nDegree = 0;
		Node nd = null;

		//create the list of nodes
		Set<SNNode> nodesSet = new HashSet<SNNode>();
		createNodeGraphList(pidNcast, nodesSet);
		createNodeGraphList(pidIdle, nodesSet);
		System.out.println("endCreate");

		List<SNNode> nodeList = new ArrayList<SNNode>();
		for (SNNode n : nodesSet.toArray(new SNNode[0])){
			nodeList.add(n);
			System.out.print(n.getID() + " ");
		}
		System.out.println();

		nodes = nodeList.size();
		ug = new UpperGraph(nodes);
		n = new int[nodes];
		color = new int[nodes];
		parent = new int[nodes];
		dt = new int[nodes];
		ft = new int[nodes];
		node = nodeList.toArray(new SNNode[0]);

		createGraph(pidNcast, nodeList);
		createGraph(pidIdle, nodeList);
		/*for (i = 0; i < node.length; ++i) {
			tmp = node[i];
			n[i] = i;
			linkable = (LinkableSN) tmp.getProtocol(pidNcast);
			nDegree = linkable.degree();
			for (j = 0; j < nDegree; ++j){
				nd = (SNNode)linkable.getNeighbor(j);
				if (((SNNode)nd).isUp()){
					ug.addEdge(i, nodeList.indexOf(nd));
				}
			}
			
			linkable = (LinkableSN) tmp.getProtocol(pidIdle);
			nDegree = linkable.degree();
			for (j = 0; j < nDegree; ++j){
				nd = (SNNode)linkable.getNeighbor(j);
				System.out.println(nd.getID() + " " + nodeList.indexOf(nd));
				if (((SNNode)nd).isUp()){
					ug.addEdge(i, nodeList.indexOf(nd));
				}
			}
		}*/
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
//		Iterator<Integer> it = hm.values().iterator();
//		while (it.hasNext()) {
//			System.out.println();
//		}
		
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
		System.out.println("INIT");
		graphInit();
		// esecuzione dfs
		System.out.println("DFS");
		dfs();
		// ordinamento in ordine decrescente di ft
		System.out.println("SORT");
		sort(0, nodes - 1);
		// esecuzione dfs su G' (grafo trasposto)
		System.out.println("DFST");
		dfsT();
		// visita dell'albero generato dall'ultima dfs
		System.out.println("VISTI");
		return treeVisit();
	}



}
