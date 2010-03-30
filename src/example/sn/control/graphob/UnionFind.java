package example.sn.control.graphob;

public class UnionFind
{
	
	int parent[];
	
	public UnionFind(int dim, int[] parent)
	{
		this.parent = new int[dim];
		System.arraycopy(parent, 0, this.parent, 0, dim);
	}
	
	public void makeSet(int x)
	{
		parent[x] = -1;
	}
	
	public int findSet(int x)
	{
		if (x == -1)
			return -1;
		
		if (parent[x] == -1)
			return x;
		
		parent[x] = findSet(parent[x]);
		return parent[x];
	}
	
	public void union(int x, int y)
	{
		int xRoot = findSet(x);
		int yRoot = findSet(y);
		
		parent[yRoot] = xRoot;
		findSet(y);
	}

}
