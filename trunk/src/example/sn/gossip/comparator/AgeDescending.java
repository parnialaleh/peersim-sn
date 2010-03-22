/*
 * Created on Dec 9, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.comparator;

import example.sn.gossip.item.Item;
import example.sn.gossip.item.ItemAge;

public class AgeDescending implements ItemComparator
{
  /**
   * Default constructor.
   */
  public AgeDescending(String prefix) {}


  /**
   *  Do nothing. Sorting independent of reference item.
   */
  public void setReference(Item refItem)
  {
    //assert false: "Is this supposed to be used?";
  }


  /**
   * Sorts based on age, in descending order,
   * so that the oldest items end up in the vector's head.
   */
  public int compare(Object objA, Object objB)
  {
    return (int)(((ItemAge)objB).age()-((ItemAge)objA).age());
  }
}
