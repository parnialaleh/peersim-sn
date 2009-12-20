package example.sn.newscast;

import peersim.core.Node;

public class NewscastMessage
{
	private boolean isRequest = false;
	private NodeEntry[] cache = null;
	private Node lnode = null;
	
	public NewscastMessage(Node lnode, boolean isRequest, NodeEntry[] cache)
	{
		this.setSender(lnode);
		this.setRequest(isRequest);
		this.setCache(cache);
	}

	public void setRequest(boolean isRequest) {
		this.isRequest = isRequest;
	}

	public boolean isRequest() {
		return isRequest;
	}

	public void setCache(NodeEntry[] cache) {
		this.cache = cache;
	}

	public NodeEntry[] getCache() {
		return cache;
	}

	public void setSender(Node lnode) {
		this.lnode = lnode;
	}

	public Node getSender() {
		return lnode;
	}

}
