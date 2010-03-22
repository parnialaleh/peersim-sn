/*
 * Created on Dec 9, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.comparator;

import example.sn.gossip.item.Item;
import example.sn.gossip.item.ItemAge;


public class AgeAscending implements ItemComparator
{
  /**
   * Default constructor.
   */
  public AgeAscending(String prefix) {}


  /**
   *  Do nothing. Sorting independent of reference item.
   */
  public void setReference(Item refItem)
  {
    //assert false: "Is this supposed to be used?";
  }


  /**
   * Sorts based on age, in ascending order,
   * so that the freshest items end up in the vector's head.
   */
  public int compare(Object objA, Object objB)
  {
    return (int)(((ItemAge)objA).age()-((ItemAge)objB).age());
  }
}
