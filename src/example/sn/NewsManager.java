package example.sn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import example.sn.epidemic.message.News;
import example.sn.epidemic.message.NewsFriendship;
import example.sn.linkable.LinkableSN;
import example.sn.newscast.NewscastED;

/**
 * Class to manage all news.
 * 
 * @author Roberto Zandonati
 *
 */
public class NewsManager implements EDProtocol
{
	private static final String PAR_IDLE_MANAGER = "protocol.idle";
	private static final String PAR_NETWORK_MANAGER = "protocol.network_manager";
	
	protected final int pidNetworkManger;
	protected final int pidIdle;
	
	private List<News> news = null;
	
	public NewsManager(String n)
	{
		this.pidNetworkManger = Configuration.getPid(n + "." + PAR_NETWORK_MANAGER);
		this.pidIdle = Configuration.getPid(n + "." + PAR_IDLE_MANAGER);
		this.news = new ArrayList<News>();
	}
	
	public Object clone()
	{
		NewsManager nm = null;
		try { nm = (NewsManager) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		nm.news = new ArrayList<News>();

		return nm;
	}
	
	public boolean contains(News n)
	{
		return news.contains(n);
	}
	

	public void addNews(News news, Node n)
	{
		this.news.add(news);
		if (news instanceof NewsFriendship)
			((NewscastED)n.getProtocol(pidNetworkManger)).addNeighbor(Network.get(((NewsFriendship)news).getDestId()));
	}
	
	/**
	 * 
	 * @return the whole list of messages
	 */
	public List<News> getNews()
	{
		return news;
	}
	
	/**
	 * 
	 * @param lnode
	 * @param rnode
	 * @return the list of messages of lnode where the author is a friend in common with rnode
	 */
	public List<News> getNews(Node lnode, Node rnode)
	{
		List<News> list = new ArrayList<News>();
		
//		System.err.println(((SNNode)lnode).getRealID() + " " + ((SNNode)rnode).getRealID());
		
		for (News n: news)
			if (((LinkableSN)n.getNode().getProtocol(pidNetworkManger)).containsAsFriend(n.getNode(), rnode))// || ((LinkableSN)n.getNode().getProtocol(pidIdle)).containsAsFriend(n.getNode(), rnode)){
				list.add(n);
				/*if (n.getNode().getID() == lnode.getID() || ((LinkableSN)lnode.getProtocol(pidNetworkManger)).containsAsFriend(n.getNode()) || ((LinkableSN)lnode.getProtocol(pidIdle)).containsAsFriend(n.getNode()))
				list.add(n);*/
		
//		if (((SNNode)lnode).getRealID() == 1455300416)
//			System.err.println(list.size() + " " + ((SNNode)rnode).getRealID());
		
		return list;
	}
	
	/**
	 * 
	 * @param lnode
	 * @return the news of lnode
	 */
	public List<News> getOwnNews(Node lnode)
	{
		List<News> list = new ArrayList<News>();
		
		for (News n: news)
			if (n.getNode().getID() == lnode.getID())
				list.add(n);
						
		return list;
	}
	
	public News getNews(int index)
	{
		return this.news.get(index);
	}
	
	public boolean merge(List<News> messages)
	{
		boolean addSomething = false;
		for (News n : messages)
			if (!news.contains(n)){
				news.add(n);
				addSomething = true;
			}

		Collections.sort(this.news);
		return addSomething;
	}

	public void processEvent(Node node, int pid, Object event) {}
	
}
