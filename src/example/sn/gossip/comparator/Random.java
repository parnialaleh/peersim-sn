/*
 * Created on Dec 9, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.comparator;

import example.sn.gossip.item.Item;
import peersim.core.CommonState;


public class Random implements ItemComparator
{
  /**
   * Default constructor.
   */
  public Random(String prefix) {}


  /**
   *  Do nothing. Sorting independent of reference item.
   */
  public void setReference(Item refItem)
  {
    //assert false: "Is this supposed to be used?";
  }


  /**
   * Return a random ordering of the two objects.
   */
  public int compare(Object objA, Object objB)
  {
    return (CommonState.r.nextBoolean() ? -1 : 1);
  }
}
