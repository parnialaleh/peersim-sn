/*
 * Created on Nov 26, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.protocol;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import peersim.cdsim.CDProtocol;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import example.sn.gossip.item.Item;
import example.sn.gossip.item.ItemSim;
import example.sn.newscast.LinkableSN;

/**
 * @author Spyros Voulgaris
 *
 * This is the base class for all gossiping protocols
 */
public abstract class Gossip extends LinkableSN implements EDProtocol, CDProtocol
{
  /**
   * This variable holds all protocol-instance related variable,
   * that have a fixed value for all nodes participating in an
   * instance of a protocol, such as the cachesize or gossiplen.
   */
  public GossipConfig gossipConfig;


  /**
   * This is the cache of gossiping protocols.
   * It is a Vector of 'Item' objects, or of its subclasses.
   */
  public Vector<Item> items;

  


  // ------------------------------------------------------------
  // ----------------- Initialization methods -------------------
  // ------------------------------------------------------------

  /**
   * Default constructor.
   * Called only once for a new protocol class instance.
   */
  public Gossip(String prefix)
  {
    gossipConfig = newGossipConfig(prefix);

    // 'items' does not need to be defined in this instance,
    // which is merely used for initialization.
    items = null;
  }

  /**
   * Individual nodes are instantiated by means of the clone function.
   */
  public Object clone()
  {
    Gossip gossip = null;
    try
    {
      gossip = (Gossip)super.clone();
    }
    catch (CloneNotSupportedException e) {e.printStackTrace();} // NEVER HAPPENS

    gossip.items = new Vector(gossipConfig.cacheSize);
    return gossip;
  }

  // XXX
  protected GossipConfig newGossipConfig(String prefix)
  {
    return new GossipConfig(prefix);
  }



  // ------------------------------------------------------------
  // --------------- Implementation of Linkable -----------------
  // ------------------------------------------------------------

  
  public final boolean addNeighbor(Node neighbour)
  {
    if (contains(neighbour))
      return false;

    if (items.size() >= gossipConfig.cacheSize)
      throw new IndexOutOfBoundsException();

    Item item = newItemInstance(neighbour);
    items.add(item);
    return true;
  }



  /**
   * Not implementing pack(), and no subclass is allowed
   * to implement it. Execution of pack() will trigger
   * a definite assertion.
   */
  public final void pack()
  {
    assert false : "Not implemented";
  }


//  public void nextCycle(Node node, int protocolID)
//  {
//    // This should not be called. It should be overriden.
//    assert false;
//  }


  public final int degree()
  {
    return items.size();
  }


  public final Node getNeighbor(int i)
  {
    return ((ItemSim)items.elementAt(i)).node;
  }


  public final boolean contains(Node neighbor)
  {
    for (int i=items.size()-1; i>=0; i--)
      if (((ItemSim)items.elementAt(i)).node == neighbor)
        return true;

    return false;
  }
  
  
  // --------------------------------------------
  // ---------- Various other methods -----------
  // --------------------------------------------

  /**
   * All 'item' instances are created by means of this method.
   * This provides the flexibility of defining and using
   * custom classes for Item objects.
   */
  protected Item newItemInstance()
  {
    try
    {
      return (Item)gossipConfig.itemClass.newInstance();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }


  /**
   * Customized constructor suitable for instances of ItemSim and
   * its subclasses.
   * 
   * @param node The node of the item
   * @return The new ItemSim instance
   */
  public final ItemSim newItemInstance(Node node)
  {
    ItemSim item = (ItemSim)newItemInstance();
    item.node = node;
    return item;
  }




  /**
   * This method sorts the cache according to the preference
   * to send items to a neighbor. In random selection, it
   * should simply shuffle the cache.
   */
  protected final void sortItemsToSend(List itemList, Item refItem)
  {
    // First shuffle no matter what, to avoid systematic errors.
    Collections.shuffle(itemList, CommonState.r);

    if (gossipConfig.sendComparator != null)
    {
      gossipConfig.sendComparator.setReference(refItem);
      Collections.sort(itemList, gossipConfig.sendComparator);
    }
  }



  /**
   * This list sorts the cache according to the preference
   * to keep items after gossiping. In random selection, it
   * should simply shuffle the cache.
   */
  public final void sortItemsToKeep(Item refItem)
  {
    // First shuffle no matter what, to avoid systematic errors.
    Collections.shuffle(items, CommonState.r);
    
    if (gossipConfig.keepComparator != null)
    {
      gossipConfig.keepComparator.setReference(refItem);
      Collections.sort(items, gossipConfig.keepComparator);
    }
  }



  /**
   * Invokes the 'selectPeerToGossip' method of the custom ItemSim
   * class defined in the configuration file for this protocol.
   * @return An integer, that is the index (in the cache ItemSim vector)
   * of the ItemSim suggested to be picked for communication
   * 
   * @deprecated
   */
  protected final int selectPeerToGossip()
  {
    assert false;
    return -1;
//    Object args[] = {items};
//    try
//    {
//      return ((Integer)gossipConfig.selectPeerToGossip.invoke(null, args)).intValue();
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//      return -1;
//    }
  }



  /**
   * Assuming that the cache is sorted so that items from the same node
   * lay in consecutive positions, this method eliminates duplicate entries.
   * More specifically, for any two items from the same node, it keeps
   * the one suggested by the keepComparator, and removes the other.
   * Does not work correctly if the cache is not already sorted.
   */
  protected final void eliminateDuplicates_sorted()
  {
    ItemSim curItem, nextItem = (ItemSim)items.lastElement();
    for (int i=items.size()-2; i>=0; i--)
    {
      curItem = (ItemSim)items.elementAt(i);
      if (curItem.node == nextItem.node)
      {
        // XXX gossipConfig.duplComparator.setReference(refItem);
        if (gossipConfig.duplComparator.compare(curItem, nextItem) < 0)
          items.removeElementAt(i+1);
        else
          items.removeElementAt(i);
      }
      else
        nextItem = curItem;
    }
  }


  /**
   * The same as {@link #eliminateDuplicates_sorted()}, but without
   * assuming a sorted cache. Obviously, this one is more expensive.
   * More specifically, for any two items from the same node, it keeps
   * the one suggested by the keepComparator, and removes the other.
   * Does not work correctly if the cache is not already sorted.
   */
  protected final void eliminateDuplicates()
  {
    Node nodeA, nodeB;

    // Start from the end of the list
    for (int a=items.size()-1; a>=0; a--)
    {
      nodeA = ((ItemSim)items.elementAt(a)).node;
      // Start from node[a-1], and check all the way to node[0]
      for (int b=a-1; b>=0; b--)
      {
        if (((ItemSim)items.elementAt(b)).node == nodeA) // if node[b]==node[a]
        {
          // XXX gossipConfig.duplComparator.setReference(refItem);
          if (gossipConfig.duplComparator.compare((ItemSim)items.elementAt(a),
                                                  (ItemSim)items.elementAt(b))<0)
          {
            // We have to evoke node[b].
            items.removeElementAt(b);
            // Now node[a]'s index has changed to a-1. So, decrease a, and let
            // the b-loop (inner) continue, to check with the rest nodes,
            // from b-1 to 0.
            a--;
          }
          else
          {
            // We have to evoke node[a].
            items.removeElementAt(a);
            // Then, since node[a] is removed, stop comparing it to other nodes,
            // and move to the next iteration of the a-loop (outer).
            break;
          }
        }
      }
    }
  }
  
  public void onKill()
  {
    items = null;
  }
}
