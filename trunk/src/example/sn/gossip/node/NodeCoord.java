/*
 * Created on Apr 17, 2005 by Spyros Voulgaris
 *
 */
package example.sn.gossip.node;


/**
 * @author Spyros Voulgaris
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeCoord extends NodeId
{
  /**
   * Coordinates of the node
   */
  public int[] coord = new int[2];

  /**
   * Constructor.
   * @param prefix XXX
   */
  public NodeCoord(String prefix)
  {
    super(prefix);

    // reset coordinates
    for (int i=0; i<coord.length; i++)
      coord[i] = -1;
  }

  public Object clone()
  {
    NodeCoord node = (NodeCoord)super.clone();
    node.coord = coord.clone();
    return node;
  }
}
