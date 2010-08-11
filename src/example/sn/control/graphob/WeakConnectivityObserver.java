package example.sn.control.graphob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import example.sn.linkable.LinkableSN;
import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.graph.Graph;
import peersim.graph.GraphAlgorithms;
import peersim.graph.NeighbourListGraph;
import peersim.util.IncrementalStats;

public class WeakConnectivityObserver implements Control {

	class Point implements Comparator<Point>
	{
		Node n;
		int index;

		public Point(Node n, int index)
		{
			this.n = n;
			this.index = index;
		}

		public int compare(Point p1, Point p2)
		{
			return (int)(p1.n.getID() - p2.n.getID());
		}
	}

	private static final String PAR_PROT_GOSSIP = "protocol";

	protected final String name;
	protected final int pidGossip;

	private GraphAlgorithms ga = new GraphAlgorithms();
	private Comparator<Point> c = new Point(null, 0);

	public WeakConnectivityObserver(String name) {
		this.name = name;
		pidGossip = Configuration.getPid(name + "." + PAR_PROT_GOSSIP);
	}

	private Graph graphInit()
	{
		NeighbourListGraph g = new NeighbourListGraph(true);
		Node n = null;
		LinkableSN l = null;

		List<Point> indexes = new ArrayList<Point>();
		for (int i = 0; i < Network.size(); i++){
			n = Network.get(i);
			if (((SNNode)n).isOnline())
				indexes.add(new Point(n, g.addNode(n))); 
		}
		
		Collections.sort(indexes, c);

		for (int i = 0; i < indexes.size(); i++){
			n = indexes.get(i).n;
			l = (LinkableSN)n.getProtocol(pidGossip);
			for (int j = 0; j < l.degree(); j++)
				if (((SNNode)l.getNeighbor(j)).isOnline())
					g.setEdge(indexes.get(i).index, Collections.binarySearch(indexes, new Point(l.getNeighbor(j), 0), c)); //indexOf(l.getNeighbor(j), indexes));
		}

		return g;
	}

	public boolean execute() {
		Map clst = ga.weaklyConnectedClusters(graphInit());
		IncrementalStats stats = new IncrementalStats();
		Iterator it = clst.values().iterator();
		while (it.hasNext()) {
			stats.add(((Integer) it.next()).intValue());
		}
		System.out.println(name + ": " + CommonState.getTime() + " " + stats);

		return false;
	}

}
