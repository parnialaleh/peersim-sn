package example.sn.graphob;

import java.util.ArrayList;

public class UpperGraph
{
	private ArrayList<ArrayList<Integer>> graph = null;
	private ArrayList<ArrayList<Integer>> graphReverse = null;

	public UpperGraph(int dim)
	{
		graph = new ArrayList<ArrayList<Integer>>(dim);
		graphReverse = new ArrayList<ArrayList<Integer>>(dim);
		for (int i = 0; i < dim; ++i){
			graph.add(new ArrayList<Integer>());
			graphReverse.add(new ArrayList<Integer>());
		}
			
	}
	
	public void addEdge(int i, int j)
	{
		ArrayList<Integer> tmp = graph.get(i);
		tmp.add(new Integer(j));
		
		tmp = graphReverse.get(j);
		tmp.add(new Integer(i));
		
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
		return ((Integer)graph.get(i).get(j)).intValue();
	}
	
	public int getReverseNeighbor(int i, int j)
	{
		return ((Integer)graphReverse.get(i).get(j)).intValue();
	}
	
	public void swap (int i, int j)
	{
		ArrayList<Integer> tmp = graph.get(i);
		graph.set(i, graph.get(j));
		graph.set(j, tmp);
		
		tmp = graphReverse.get(i);
		graphReverse.set(i, graphReverse.get(j));
		graphReverse.set(j, tmp);
	}
}
