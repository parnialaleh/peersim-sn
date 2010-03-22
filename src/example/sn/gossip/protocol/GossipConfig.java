/*
 * Created on Nov 26, 2004 by Spyros Voulgaris
 *
 */
package example.sn.gossip.protocol;

import example.sn.gossip.comparator.ItemComparator;
import example.sn.gossip.comparator.Random;
import example.sn.gossip.item.Item;

import java.util.Comparator;
import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.CommonState;

/**
 * @author Spyros Voulgaris
 *
 */
public class GossipConfig
{
  /**
   *  Cache size of the protocol.
   */
  private static final String PAR_CACHESIZE = "cache";

  /**
   * Number of items to gossip in each cycle.
   * 
   * It can trivially be 0, but then it makes sense
   * only if this protocol's cache is fed by its underlying
   * protocol.
   */
  private static final String PAR_GOSSIPLEN = "gossip";

  /**
   *  Name of class to be used for ItemSim in this protocol.
   */
  private static final String PAR_ITEMCLASS = "item";

  /**
   *  Name of class to be used as a comparator for selecting a peer to gossip.
   */
  private static final String PAR_SELECT_COMPARATOR = "select";

  /**
   *  Name of class to be used as a comparator for items to send.
   */
  private static final String PAR_SEND_COMPARATOR = "send";

  /**
   *  Name of class to be used as a comparator for items to keep.
   */
  private static final String PAR_KEEP_COMPARATOR = "keep";

  /**
   *  Name of class to be used as a comparator for duplicate item conflicts.
   */
  private static final String PAR_DUPL_COMPARATOR = "duplicate";



  // Protocol specific parameters
  public int cacheSize = -1;
  public int gossipLen = -1;
  public int pid = -1;


  // Class for 'Item' objects.
  public Class itemClass;

  // Instances of ItemComparator classes
  /**
   * Comparator for selecting neighbor to gossip with.
   * The 'min' neighbor will be picked for gossiping.
   */
  public Comparator<Item> selectComparator = null;            // initialize with null
  
  /**
   * Comparator for defining the priority for neighbors to keep.
   * Typically up to cachesize 'min' neighbors will be kept.
   */
  public ItemComparator sendComparator = new Random(null); // initialize with random

  /**
   * Comparator for defining the priority for neighbors to send.
   * Typically up to gossiplen 'min' neighbors will be sent.
   */
  public ItemComparator keepComparator = new Random(null); // initialize with random

  /**
   * Comparator for selecting item to keep, in case of duplicate conflict.
   * The minimun item will be kept.
   */
  public ItemComparator duplComparator = new Random(null); // initialize with random




  public GossipConfig(String prefix)
  {
    String confProperty, className;

    gossipLen = Configuration.getInt(prefix+"."+PAR_GOSSIPLEN, 0);
    cacheSize = Configuration.getInt(prefix+"."+PAR_CACHESIZE);
    pid = CommonState.getPid();


    /*
     * Apply reflection for ItemSim
     */

    // Get the class for ItemSim.
    className = Configuration.getString(prefix+"."+PAR_ITEMCLASS);
    try {itemClass = Class.forName(className);}
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
      throw new Error();
    }


    /*
     * Apply reflection for comparators
     */
    // selectComparator
    confProperty = prefix+"."+PAR_SELECT_COMPARATOR;
    try {selectComparator = (Comparator)Configuration.getInstance(confProperty);}
    catch (MissingParameterException e) {}

    // sendComparator
    confProperty = prefix+"."+PAR_SEND_COMPARATOR;
    try {sendComparator = (ItemComparator)Configuration.getInstance(confProperty);}
    catch (MissingParameterException e) {}
    
    // keepComparator
    confProperty = prefix+"."+PAR_KEEP_COMPARATOR;
    try {keepComparator = (ItemComparator)Configuration.getInstance(confProperty);}
    catch (MissingParameterException e) {}
    
    // duplComparator
    confProperty = prefix+"."+PAR_DUPL_COMPARATOR;
    try {duplComparator = (ItemComparator)Configuration.getInstance(confProperty);}
    catch (MissingParameterException e) {}


//    try
//    {
//      // selectComparator
//      className = Configuration.getString(prefix+"."+PAR_SELECT_COMPARATOR, null);
//      if (className != null)
//      {
//        theClass = Class.forName(className);
//        selectComparator = (Comparator)theClass.newInstance();
//      }
//
//      // sendComparator
//      className = Configuration.getString(prefix+"."+PAR_SEND_COMPARATOR, null);
//      if (className != null)
//      {
//        theClass = Class.forName(className);
//        sendComparator = (ItemComparator)theClass.newInstance();
//      }
//
//      // keepComparator
//      className = Configuration.getString(prefix+"."+PAR_KEEP_COMPARATOR, null);
//      if (className != null)
//      {
//        theClass = Class.forName(className);
//        keepComparator = (ItemComparator)theClass.newInstance();
//      }
//
//      // duplComparator
//      className = Configuration.getString(prefix+"."+PAR_DUPL_COMPARATOR, null);
//      if (className != null)
//      {
//        theClass = Class.forName(className);
//        duplComparator = (ItemComparator)theClass.newInstance();
//      }
//    }
//    catch (ClassNotFoundException e)     {e.printStackTrace();}
//    catch (InstantiationException e)     {e.printStackTrace();}
//    catch (IllegalAccessException e)     {e.printStackTrace();}
//    catch (SecurityException e)          {e.printStackTrace();}
//    catch (IllegalArgumentException e)   {e.printStackTrace();}
  }
}
