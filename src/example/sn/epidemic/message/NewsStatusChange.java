package example.sn.epidemic.message;

import peersim.core.Node;

public class NewsStatusChange extends News
{
	public NewsStatusChange(Node sourceNode)
	{
		super(sourceNode, sourceNode);
	}
}
