package example.sn.gossip.comparator;

import example.sn.gossip.item.Item;
import example.sn.gossip.item.ItemSim;
import example.sn.gossip.node.NodeCoord;


public class Proximity2D implements ItemComparator
{
  int refX, refY;

  public Proximity2D(String prefix)
  {
  }

  public void setReference(Item refItem)
  {
    refX = ((NodeCoord)((ItemSim)refItem).node).coord[0];
    refY = ((NodeCoord)((ItemSim)refItem).node).coord[1];
  }

  public int compare(Object objA, Object objB)
  {
    int xA = ((NodeCoord)((ItemSim)objA).node).coord[0];
    int yA = ((NodeCoord)((ItemSim)objA).node).coord[1];
    int xB = ((NodeCoord)((ItemSim)objB).node).coord[0];
    int yB = ((NodeCoord)((ItemSim)objB).node).coord[1];

    int a_dx = Math.abs(xA - refX);
    int a_dy = Math.abs(yA - refY);
    int b_dx = Math.abs(xB - refX);
    int b_dy = Math.abs(yB - refY);
    
    int distA = a_dx*a_dx + a_dy*a_dy;
    int distB = b_dx*b_dx + b_dy*b_dy;

    if (distA < distB)
      return -1;
    else if (distA > distB)
      return 1;
    else
      return 0;
  }
}
