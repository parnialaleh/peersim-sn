/*
 * Created on Apr 17, 2005 by Spyros Voulgaris
 *
 */
package example.sn.gossip.node;

import peersim.core.GeneralNode;

/**
 * @author Spyros Voulgaris
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeId extends GeneralNode implements NodeWithId
{
  /**
   * Internally used to provide sequential IDs.
   */
  private static int idCounter = 0;

  /**
   * The node ID.
   * IDs 0, 1, 2, ..., are assigned in the order that Nodes are created
   * by <b>cloning</b>. The constructor assigns ID=-1.
   */
  private int id;

  /**
   * Constructor.
   * @param prefix XXX
   */
  public NodeId(String prefix)
  {
    super(prefix);
    System.err.println(prefix); //XXX
    id = -1;
  }
  
  public Object clone()
  {
    NodeId node = (NodeId)super.clone();
    node.id = idCounter++;
    return node;
  }

//  public void setId(int id)
//  {
//    this.id = id;
//  }

  /** @return Returns the id.*/
  public int getId()
  {
    return id;
  }
}
