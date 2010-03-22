
package example.sn.gossip.dynamics;

import example.sn.gossip.node.NodeCoord;
import peersim.core.*;




/**
 * Takes a {@link Linkable} protocol and adds random connections. Note that no
 * connections are removed, they are only added. So it can be used in
 * combination with other initializers.
 */
public class CoordInit implements Control
{
  public CoordInit(String prefix) {}


  public boolean execute()
  {
    // Do coord initialization
    for (int coordIndex=0; coordIndex < 2; coordIndex++)
    {
      if (((NodeCoord)Network.get(0)).coord[coordIndex] == -1)
      {
        for (int i=Network.size()-1; i>=0; i--)
          ((NodeCoord)Network.get(i)).coord[coordIndex] = i;

        for (int i=Network.size()-1; i>=1; i--)
        {
          int r = CommonState.r.nextInt(i+1);
          int tmp = ((NodeCoord)Network.get(i)).coord[coordIndex];
          ((NodeCoord)Network.get(i)).coord[coordIndex] =
            ((NodeCoord)Network.get(r)).coord[coordIndex];
          ((NodeCoord)Network.get(r)).coord[coordIndex] = tmp;
        }
      }
    }

    return false; // don't stop simulation
  }
}
