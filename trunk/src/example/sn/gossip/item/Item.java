/*
 * Created on Apr 3, 2005 by Spyros Voulgaris
 *
 */
package example.sn.gossip.item;

import java.io.Serializable;

/**
 * @author Spyros Voulgaris
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Item extends Cloneable, Serializable
{
  /**
   * All implementors of this interface should provide a clone()
   * implementation.
   */
  public Object clone();// throws CloneNotSupportedException;
  
  /**
   * Default equality comparator for Item objects.
   */
  public boolean equals(Object obj);
}
