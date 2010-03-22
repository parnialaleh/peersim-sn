/*
 * Created on Dec 9, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.comparator;

import java.util.Comparator;

import example.sn.gossip.item.Item;


/**
 * @author Spyros Voulgaris
 *
 */
public interface ItemComparator extends Comparator
{
  /**
   * Declare a reference item for comparators that compare
   * two items with respect to a reference item.
   * @param refItem The refItem to set.
   */
  public void setReference(Item refItem);
}
