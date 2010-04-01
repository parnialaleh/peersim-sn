package example.cyclon;

import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;

public class CyclonMessage
{
	public Node node;
	public List<CyclonEntry> list;
	public boolean isResuest;
	
	public long time;

	public CyclonMessage(Node node, List<CyclonEntry> list, boolean isRequest)
	{
		this.node = node;
		this.list = list;
		this.isResuest = isRequest;
		
		this.time = CommonState.getTime();
	}
}
