package example.sn.graphob;

import java.util.ArrayList;
import java.util.List;

public class UpperGraph
{
	private ArrayList<ArrayList<Long>> graph = null;
	private ArrayList<ArrayList<Long>> graphReverse = null;

	public UpperGraph(int dim)
	{
		graph = new ArrayList<ArrayList<Long>>(dim);
		graphReverse = new ArrayList<ArrayList<Long>>(dim);
		for (int i = 0; i < dim; ++i){
			graph.add(new ArrayList<Long>());
			graphReverse.add(new ArrayList<Long>());
		}			
	}

	public UpperGraph(int dim, boolean alsoReverse)
	{
		graph = new ArrayList<ArrayList<Long>>(dim);
		if (alsoReverse)
			graphReverse = new ArrayList<ArrayList<Long>>(dim);
		for (int i = 0; i < dim; ++i){
			graph.add(new ArrayList<Long>());
			if (alsoReverse)
				graphReverse.add(new ArrayList<Long>());
		}			
	}

	public void addEdge(int i, int j)
	{
		ArrayList<Long> tmp = graph.get(i);
		tmp.add(new Long(j));

		if (graphReverse != null){
			tmp = graphReverse.get(j);
			tmp.add(new Long(i));
		}
	}

	public void addSimmetriEdge(int i, int j)
	{		
		if (i < 0 || j < 0)
			return;
		
		ArrayList<Long> tmp = graph.get(i);
		ArrayList<Long> tmp2 = graph.get(j);
		if ((tmp.indexOf(new Long(j)) >= 0) || (tmp2.indexOf(new Long(i)) >= 0))
			return;

		tmp.add(new Long(j));
		tmp = graph.get(j);
		tmp.add(new Long(i));
	}
	
	public int degree()
	{
		return graph.size();
	}

	public int degree(int i)
	{
		return graph.get(i).size();
	}

	public int reverseDegree(int i)
	{
		return graphReverse.get(i).size();
	}

	public int getNeighbor(int i, int j)
	{
		return ((Long)graph.get(i).get(j)).intValue();
	}

	public int getReverseNeighbor(int i, int j)
	{
		return ((Long)graphReverse.get(i).get(j)).intValue();
	}
	
	public List<Long> getNeighbours(int i)
	{
		return graph.get(i);
	}

	public void swap (int i, int j)
	{
		ArrayList<Long> tmp = graph.get(i);
		graph.set(i, graph.get(j));
		graph.set(j, tmp);

		tmp = graphReverse.get(i);
		graphReverse.set(i, graphReverse.get(j));
		graphReverse.set(j, tmp);
	}
}
