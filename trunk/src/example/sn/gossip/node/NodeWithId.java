/*
 * Created on Apr 17, 2005 by Spyros Voulgaris
 *
 */
package example.sn.gossip.node;

import peersim.core.Node;


/**
 * @author Spyros Voulgaris
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface NodeWithId extends Node
{
  /**
   * Returns the ID of this node.
   * The ID is final, therefore no setId() is provided.
   * @return The node ID.
   */
  public int getId();
}
