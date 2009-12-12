package example.sn.newscast;

import peersim.core.Node;

public class NodeEntry implements Comparable<NodeEntry>
{
	public Node n = null;
	public int ts = -1;
	public int type = 0;

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
}
