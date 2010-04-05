package example.newscast;

import peersim.core.Node;

public class NodeEntry implements Comparable<NodeEntry>, Cloneable
{
	public Node n = null;
	public int ts = -1;
	public int type = 0;
	
	public NodeEntry(){}
	
	public NodeEntry(Node n, int ts, int type)
	{
		this.n = n;
		this.ts = ts;
		this.type = type;
	}

	@Override
	public boolean equals(Object obj)
	{
		return ((NodeEntry)obj).n.equals(n);
		//			if (((NodeEntry)obj).n.equals(n))
		//				return ((NodeEntry)obj).ts < ts; //obj is different from this if it is freshness than this
		//			return false;
	}

	public int compareTo(NodeEntry node)
	{
		if (node.ts > ts)
			return -1;
		else if (node.ts < ts)
			return 1;

		return 0;
	}
	
	@Override
	public Object clone(){
		NodeEntry ne = null;
		try { ne = (NodeEntry) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		ne.ts = this.ts;
		ne.type = this.type;
		return ne;
	}
}
