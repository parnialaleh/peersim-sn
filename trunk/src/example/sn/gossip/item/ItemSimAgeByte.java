/*
 * Created on Oct 24, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.item;




/**
 * @author Spyros Voulgaris
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ItemSimAgeByte extends ItemSim implements ItemAge
{
  private byte age;

  public ItemSimAgeByte()
  {
    super();
    age = 0;
  }

  public String toString()
  {
    return node.getIndex()+" "+age;
  }


  public int age() {return age;}
  public void incAge() {age++;}
  public void zeroAge() {age=0;}
}
