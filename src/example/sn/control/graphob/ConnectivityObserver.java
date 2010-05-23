package example.sn.control.graphob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import example.sn.linkable.LinkableSN;
import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

public class ConnectivityObserver implements Control {

	private static final String PAR_PROT_GOSSIP = "protocol";

	protected final String name;
	protected final int pidGossip;
	protected int nodes = 0;
	protected int[] n;
	protected static final int WHITE = 0;
	protected static final int GRAY = 1;
	protected static final int BLACK = 2;
	protected UpperGraph ug = null;
	protected int color[];
	protected int parent[];
	protected int dt[];
	protected int ft[];
	protected int time = 0;
	protected int networksize = 0;
	
	public ConnectivityObserver(String name) {
		this.name = name;
		pidGossip = Configuration.getPid(name + "." + PAR_PROT_GOSSIP);
	}
	
	public ConnectivityObserver(int pidNcast)
	{
		this.pidGossip = pidNcast;
		this.name = "";
	}

	// ------------------------------------------
	// dfs sul grafo
	private void dfsVisit(int i) {
		int v = -1;
		int uDegree = ug.degree(i);

		color[i] = GRAY;
		time++;
		dt[i] = time;

		for (int j = 0; j < uDegree; j++) {
			v = ug.getNeighbor(i, j);
			if (color[v] == WHITE) {
				parent[v] = i;
				dfsVisit(v);
			}
		}
		time++;
		ft[i] = time;
		color[i] = BLACK;
	}

	protected void dfs() {
		int i = 0;
		time = 0;

		for (i = 0; i < nodes; ++i) {
			color[i] = WHITE;
			parent[i] = -1;
		}

		for (i = 0; i < nodes; i++)
			if (color[i] == WHITE)
				dfsVisit(i);
	}

	// ------------------------------------------

	// ------------------------------------------
	// dfs sul grafo trasposto
	protected void dfsVisitT(int i) {
		int v = -1;
		int uDegree = ug.reverseDegree(i);

		color[i] = GRAY;
		time++;
		dt[i] = time;

		for (int j = 0; j < uDegree; j++) {
			v = ug.getReverseNeighbor(i, j);
			if (color[v] == WHITE) {
				parent[v] = i;
				dfsVisitT(v);
			}
		}
		time++;
		ft[i] = time;
		color[i] = BLACK;
	}

	protected void dfsT() {
		int i = 0;

		for (i = 0; i < nodes; ++i) {
			color[i] = WHITE;
			parent[i] = -1;
		}

		for (i = 0; i < nodes; i++)
			if (color[n[i]] == WHITE)
				dfsVisitT(n[i]);
	}

	// ------------------------------------------

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

	// ------------------------------------------
	// inizializzazione grafo
	private void graphInit() {
		int i = 0;
		int j = 0;
		SNNode tmp = null;
		LinkableSN linkable = null;
		int nDegree = 0;
		SNNode nd = null;

		nodes = 0;
		ArrayList<SNNode> node = new ArrayList<SNNode>();
		// conteggio numero di nodi
		for (i = 0; i < Network.size(); ++i){
			nd = (SNNode)Network.get(i);
			if (((SNNode)nd).isUp()){
				nodes++;
				node.add((SNNode)Network.get(i));
			}
		}
		
		networksize = nodes;

		ug = new UpperGraph(nodes);
		n = new int[nodes];
		color = new int[nodes];
		parent = new int[nodes];
		dt = new int[nodes];
		ft = new int[nodes];

		for (i = 0; i < node.size(); ++i) {
			n[i] = i;
			tmp = node.get(i);
			linkable = (LinkableSN) tmp.getProtocol(pidGossip);
			nDegree = linkable.degree();
			for (j = 0; j < nDegree; ++j){
				nd = (SNNode)linkable.getNeighbor(j);
				if (((SNNode)nd).isUp()){
					ug.addEdge(i, node.indexOf(linkable.getNeighbor(j)));
				}
			}
			/*linkable = (LinkableSN) tmp.getProtocol(pidIdle);
			nDegree = linkable.degree();
			for (j = 0; j < nDegree; ++j){
				nd = (SNNode)linkable.getNeighbor(j);
				if (((SNNode)nd).isUp()){
					ug.addEdge(i, node.indexOf(nd));
				}
			}*/
		}
	}

	// ------------------------------------------

	// ------------------------------------------
	// visita albero generato dall'ultima
	// dfs sul grafo trasposto
	private void treeVisit(IncrementalStats is) {
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		int i = 0;
		Integer v = null;
		UnionFind uf = new UnionFind(nodes, parent);

		for (i = 0; i < nodes; ++i) {
			v = hm.get(uf.findSet(i));
			if (v == null)
				hm.put(uf.findSet(i), new Integer(1));
			else
				hm.put(uf.findSet(i), new Integer(v.intValue() + 1));
		}
		Iterator<Integer> it = hm.values().iterator();
		while (it.hasNext()) {
			is.add((it.next()).intValue());
		}
	}

	// ------------------------------------------

	public boolean execute() {
		IncrementalStats is = new IncrementalStats();

		// inizializzazione grafo della rete
		graphInit();
		// esecuzione dfs
		dfs();
		// ordinamento in ordine decrescente di ft
		sort(0, nodes - 1);
		// esecuzione dfs su G' (grafo trasposto)
		dfsT();
		// visita dell'albero generato dall'ultima dfs
		treeVisit(is);

		// stampa risultato visita
		System.out.println(CommonState.getTime() + " " + name + ": " + is + " " + networksize);

		return false;
	}

}
