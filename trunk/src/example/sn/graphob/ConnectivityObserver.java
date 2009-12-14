package example.sn.graphob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import example.sn.newscast.LinkableSN;
import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class ConnectivityObserver implements Control {

	private static final String PAR_PROT = "protocol";

	private final String name;

	private final int pid;

	private int nodes = 0;

	private int[] n;

	private static final int WHITE = 0;

	private static final int GRAY = 1;

	private static final int BLACK = 2;

	private UpperGraph ug = null;

	private int color[];

	private int parent[];

	private int dt[];

	private int ft[];

	private int time = 0;

	public ConnectivityObserver(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROT);
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

	private void dfs() {
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
	private void dfsVisitT(int i) {
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

	private void dfsT() {
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

		ug = new UpperGraph(nodes);
		n = new int[nodes];
		color = new int[nodes];
		parent = new int[nodes];
		dt = new int[nodes];
		ft = new int[nodes];

		for (i = 0; i < node.size(); ++i) {
			n[i] = i;
			tmp = node.get(i);
			linkable = (LinkableSN) tmp.getProtocol(pid);
			nDegree = linkable.degree();
			for (j = 0; j < nDegree; ++j){
				nd = (SNNode)linkable.getNeighbor(j);
				if (((SNNode)nd).isUp()){
					ug.addEdge(i, node.indexOf(linkable.getNeighbor(j)));
				}
			}
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
		Iterator it = hm.values().iterator();
		while (it.hasNext()) {
			is.add(((Integer) it.next()).intValue());
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
		System.out.println(CommonState.getTime() + " " + name + ": " + is);

		return false;
	}

}
