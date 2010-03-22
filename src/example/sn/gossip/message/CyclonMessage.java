package example.sn.gossip.message;

import java.util.Vector;

import peersim.core.Node;

import example.sn.gossip.item.Item;

public class CyclonMessage
{
	private Vector<Item> items = null;
	private boolean isRequest = false;
	private Node lnode = null;
	private Item itemA = null;
	
	
	public CyclonMessage(Vector<Item> items, boolean isRequest, Node lnode, Item itemA)
	{
		this.items = items;
		this.isRequest = isRequest;
		this.lnode = lnode;
		this.itemA = itemA;
	}
	
	public Vector<Item> getItems() {
		return items;
	}


	public boolean isRequest() {
		return isRequest;
	}


	public Node getLnode() {
		return lnode;
	}
	
	public Item getItemA() {
		return itemA;
	}

}
