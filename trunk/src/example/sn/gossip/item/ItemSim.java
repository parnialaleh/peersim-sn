/*
 * Created on Oct 10, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.item;

import peersim.core.Node;



/**
 * @author Spyros Voulgaris
 */
public class ItemSim implements Item
{
  public Node node = null;


  public Object clone()
  {
    try {return super.clone();}
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
      System.exit(-1);
      return null; // to make the compiler shut up
    }
  }



  /**
   * Default equality comparator for ItemSim objects.
   * Two ItemSim instances are equal iff they refer to
   * the same Node instance.
   */
  public final boolean equals(Object obj)
  {
    return node == ((ItemSim)obj).node;
  }
}
