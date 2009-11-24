package example.sn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireKOut;
import peersim.edsim.EDProtocol;

import example.sn.epidemic.message.News;
import example.sn.epidemic.message.NewsFriendship;

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
	

	public void addNews(News news, Node n)
	{
		this.news.add(news);
		if (news instanceof NewsFriendship)
			((NewscastED)n.getProtocol(pidNetworkManger)).addNeighbor(Network.get(((NewsFriendship)news).getDestId()));
	}
	
	public List<News> getNews(Node lnode, Node rnode)
	{
		List<News> list = new ArrayList<News>();
		
		for (News n: news)
			if (n.getNode().getID() == lnode.getID() || ((NewscastED)n.getNode().getProtocol(pidNetworkManger)).contains(n.getNode()) || ((IdleProtocol)n.getNode().getProtocol(pidNetworkManger)).contains(n.getNode()))
				list.add(n);
				
		
		return list;
	}
	
	public News getNews(int index)
	{
		return this.news.get(index);
	}
	
	public boolean merge(List<News> messages)
	{
		boolean addSomething = this.news.addAll(messages);
		Collections.sort(this.news);
		return addSomething;
	}

	public void processEvent(Node node, int pid, Object event) {}
	
}
