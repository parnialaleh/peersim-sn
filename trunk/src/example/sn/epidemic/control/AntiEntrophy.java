package example.sn.epidemic.control;

import example.sn.EpidemicNews;
import example.sn.NewsManager;
import example.sn.epidemic.message.EpidemicHashMessage;
import example.sn.epidemic.message.EpidemicWholeMessages;
import example.sn.linkable.LinkableSN;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.extras.am.epidemic.EpidemicProtocol;
import peersim.extras.am.epidemic.Message;

public class AntiEntrophy implements EpidemicProtocol
{
	private static final String PAR_GOSSIP = "protocol.gossip";
	private static final String PAR_NEWS_MANAGER = "protocol.news_manager";
	private static final String PAR_RUMOR_MONGERING = "protocol.rumor_mongering";
	
	protected final int pidGossip;
	protected final int pidNewsManger;
	protected final int pidRumorMongering;
	
	public AntiEntrophy(String n)
	{
		this.pidNewsManger = Configuration.getPid(n + "." + PAR_NEWS_MANAGER);
		this.pidGossip = Configuration.getPid(n + "." + PAR_GOSSIP);
		this.pidRumorMongering = Configuration.getPid(n + "." + PAR_RUMOR_MONGERING);
	}
	
	@Override
	public Object clone()
	{
		AntiEntrophy a = null;
		try { a = (AntiEntrophy) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		
		return a;
	}

	public void merge(Node lnode, Node rnode, Message msg)
	{
		if (((NewsManager)lnode.getProtocol(pidNewsManger)).merge(((EpidemicWholeMessages)msg).getMessages()))
			((EpidemicNews)lnode.getProtocol(pidRumorMongering)).setInfected(true);
	}

	public Message prepareRequest(Node lnode, Node rnode)
	{
		return new EpidemicWholeMessages(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode, pidNewsManger), true);
	}

	public Message prepareResponse(Node lnode, Node rnode, Message request)
	{
		return new EpidemicWholeMessages(false, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode, pidNewsManger), (request instanceof EpidemicHashMessage));
	}

	public Node selectPeer(Node lnode)
	{
		return ((LinkableSN)(lnode.getProtocol(pidGossip))).getPeer(lnode);
	}

}
