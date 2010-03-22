/*
 * Created on Oct 10, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import example.sn.gossip.comparator.Random;
import example.sn.gossip.item.Item;
import example.sn.gossip.item.ItemAge;
import example.sn.gossip.item.ItemSim;
import example.sn.gossip.message.CyclonMessage;
import example.sn.newscast.LinkableSN;
import example.sn.newscast.NodeEntry;
import peersim.config.Configuration;
import peersim.core.*;
import peersim.transport.Transport;


/**
 * @author Spyros Voulgaris
 *
 * Cyclon implements the basic shuffling protocol.
 * Depending on the Item instance used, it runs
 * either basic shuffling (Item) or Cyclon (ItemAge).
 *
 */
public class Cyclon extends Gossip
{
	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_IDLE_PROTOCOL = "idle_protocol";

	private Vector<Item> sendA2B = null;
	private final int tid;
	private final int idleProtocol;
	// ------------------------------------------------------------
	// ----------------- Initialization methods -------------------
	// ------------------------------------------------------------

	/**
	 * Default constructor.
	 * Called only once for a new protocol class instance.
	 */
	public Cyclon(String name)
	{
		super(name);
		tid = Configuration.getPid(name + "." + PAR_TRANSPORT);
		idleProtocol = Configuration.getPid(name + "." + PAR_IDLE_PROTOCOL);
	}



	private Vector<Item> selectItemsToSend(Item destItem, Vector<Item> receivedItems, int howmany)
	{
		Vector<Item> itemsToSend = new Vector<Item>(gossipConfig.gossipLen);

		// If I want to send ALL items, there's no need to sort them.
		// Otherwise shuffle them, to select random 'howmany' of them.
		if (howmany < items.size())
		{
			// Sort based on the selectComparator...
			Collections.shuffle(items, CommonState.r);
			if (!(gossipConfig.selectComparator instanceof Random))
				Collections.sort(items, gossipConfig.selectComparator);
		}

		// And now select the 'howmany' first of my sorted cache.
		for (int i = 0; i < items.size(); i++)
		{
			ItemSim item = (ItemSim)items.elementAt(i);

			// Check if the selected item is the destination node,
			// which should obviously be excluded.
			if (!item.equals(destItem))
			{
				if (((LinkableSN)((ItemSim)destItem).node.getProtocol(idleProtocol)).containsAsFriend(((ItemSim)destItem).node, item.node)){
					items.remove(i--);
					itemsToSend.add(item);
					if (--howmany==0)
						break;
				}
			}
		}

		return itemsToSend;
	}    

	private void insertReceivedItems(Vector<Item> received, Vector<Item> sent, Node sender)
	{
		for (int i=0; i<received.size(); i++)
			insertItem((ItemSim)received.elementAt(i));

		assert items.size() <= gossipConfig.cacheSize;

		// Now try filling up empty slots with items I sent to the other peer.
		int index=0;
		for (int i=gossipConfig.cacheSize-items.size(); i>0; i--)
		{
			if (index>=sent.size())
				break;

			// Now add the sent item back to the sender's cache, of course
			// making sure it's not the sender itself.
			ItemSim sentItem = (ItemSim)sent.elementAt(index);
			if (sentItem.node != sender)
				insertItem(sentItem);

			index++;
		}

		assert items.size() <= gossipConfig.cacheSize;
	}

	private final void insertItem(ItemSim newItem)
	{
		int foundAt;

		// If the 'newItem' is not in the list already, simply put it.
		if ( (foundAt=items.indexOf(newItem)) == -1 )
			items.add(newItem);

		// Else, we have a duplicate. Keep the prefered one, based
		// on the ItemSim class' criterion.
		else
		{
			// contained already, at position 'foundAt'
			ItemSim existingItem = (ItemSim)items.elementAt(foundAt);

			// XXX gossipConfig.duplComparator.setReference(refItem);
			if (gossipConfig.duplComparator.compare(newItem,existingItem) < 0)
				items.setElementAt(newItem, foundAt);
		}
	}

	public void nextCycle(Node nodeA, int protocolID)
	{
		assert !contains(nodeA);

		if ( (items.size() == 0) ||
				(gossipConfig.gossipLen == 0) )
			return;


		// Sort based on the selectComparator...
		Collections.shuffle(items, CommonState.r);
		Collections.sort(items, Collections.reverseOrder(gossipConfig.selectComparator));

		// ...and select from the end.
		int peerIndex = items.size()-1;
		ItemSim itemB = (ItemSim)items.remove(peerIndex);
		Item itemA = newItemInstance(nodeA);


		// Initially select 'gossipLen'-1 items from A to send to B.
		// Of course, exclude B itself.
		sendA2B = selectItemsToSend(itemB, null, gossipConfig.gossipLen-1);

		// Add my own item to the list to send to B.
		sendA2B.add(itemA);

		Transport tr = (Transport) nodeA.getProtocol(tid);
		CyclonMessage message = new CyclonMessage(sendA2B, true, nodeA, itemA);
		tr.send(nodeA, itemB.node, message, protocolID);
	}

	public void processEvent(Node lnode, int pid, Object event)
	{
		CyclonMessage message = (CyclonMessage)event;
		if (message.isRequest()){
			// And now select 'gossipLen' items of B to send to A.
			// This time exclude possible pointers to A.
			Vector<Item> sendB2A = selectItemsToSend(message.getItemA(), message.getItems(), gossipConfig.gossipLen);

			Transport tr = (Transport) lnode.getProtocol(tid);
			CyclonMessage msg = new CyclonMessage(sendB2A, false, lnode, null);
			tr.send(lnode, message.getLnode(), msg, pid);

			insertReceivedItems(message.getItems(), sendB2A, message.getLnode());
		}
		else {
			insertReceivedItems(message.getItems(), sendA2B, message.getLnode());
		}

		// If using ages, increase the age of each item in my cache by 1.
		if (ItemAge.class.isAssignableFrom(gossipConfig.itemClass))
			for (int i=items.size()-1; i>=0; i--)
				((ItemAge)items.elementAt(i)).incAge();
	}



	@Override
	public boolean containsAsFriend(Node lnode, Node n) {
		LinkableSN lnk = (LinkableSN)lnode.getProtocol(idleProtocol);
		return lnk.containsAsFriend(lnode,n);
	}



	@Override
	public Node getFriendPeer(Node lnode, Node n) {

		LinkableSN lnk = (LinkableSN)lnode.getProtocol(idleProtocol);
		List<Node> nodes = new ArrayList<Node>();

		for (int i = 0; i < items.size(); i++)
			if (lnk.containsAsFriend(lnode, ((ItemSim)items.get(i)).node))
				nodes.add(((ItemSim)items.get(i)).node);

		return nodes.get(CommonState.r.nextInt(nodes.size()));
	}



	@Override
	public NodeEntry[] getFriends(Node lnode, Node n) {
		LinkableSN lnk = (LinkableSN)lnode.getProtocol(idleProtocol);
		return lnk.getFriends(lnode, n);
	}


	@Override
	public Node getPeer(Node node) {
		try{
			return ((ItemSim)items.get(CommonState.r.nextInt(items.size()))).node;
		} catch (Exception e){
			System.out.println("NO PEER");
			return null;
		}
	}

	@Override
	public Node[] getNodes(Node node)
	{
		List<Node> list = new ArrayList<Node>();
		for (int i = 0; i < items.size(); i++)
			list.add(((ItemSim)items.get(i)).node);

		return list.toArray(new Node[0]);
	}

}
