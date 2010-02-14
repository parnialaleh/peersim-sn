package example.sn;

import java.util.List;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.extras.am.epidemic.EpidemicProtocol;
import peersim.extras.am.epidemic.Message;
import peersim.extras.am.epidemic.bcast.Infectable;
import example.sn.epidemic.message.EpidemicHashMessage;
import example.sn.epidemic.message.EpidemicWholeMessages;
import example.sn.epidemic.message.News;
import example.sn.newscast.LinkableSN;
import example.sn.node.SNNode;

public class EpidemicNews implements EpidemicProtocol, Infectable
{	
	private static final String PAR_NETWORK_MANAGER = "protocol.network_manager";
	private static final String PAR_NEWS_MANAGER = "protocol.news_manager";
	private static final String PAR_HASH_MESSAGE = "hash";

	protected final int pidNetworkManger;
	protected final int pidNewsManger;
	protected final boolean hash_message;

	private boolean infected = false;
	
	private long lastSelectedPeer = Long.MIN_VALUE;

	public EpidemicNews(String n)
	{
		this.infected = true;

		this.pidNewsManger = Configuration.getPid(n + "." + PAR_NEWS_MANAGER);
		this.pidNetworkManger = Configuration.getPid(n + "." + PAR_NETWORK_MANAGER);
		this.hash_message = Configuration.getBoolean(n + "." + PAR_HASH_MESSAGE, true);
	}

	public Object clone()
	{
		EpidemicNews ev = null;
		try {
			ev = (EpidemicNews)super.clone();
		} catch (CloneNotSupportedException e) {}
		ev.infected = true;
		ev.lastSelectedPeer = Long.MIN_VALUE;
		return ev;	
	}

	public void merge(Node lnode, Node rnode, Message msg) {
		boolean res = ((NewsManager)lnode.getProtocol(pidNewsManger)).merge(((EpidemicWholeMessages)msg).getMessages());

//		if (res)
//			System.out.println(lnode.getID() + "->" + rnode.getID() + " " + ((SNNode)lnode).getRealID() + "->" + ((SNNode)rnode).getRealID() + " " + res);

		infected =  res || infected;
	}

	public Message prepareRequest(Node lnode, Node rnode) {
		if (!infected)
			return null;

		if (hash_message)
			return new EpidemicHashMessage(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode).hashCode());

		return new EpidemicWholeMessages(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode), true);
	}

	public Message prepareResponse(Node lnode, Node rnode, Message request) {
		if (!infected)
			return null;

		if ((request instanceof EpidemicHashMessage) && ((EpidemicHashMessage)request).getStatus())
			return new EpidemicHashMessage(false, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode).hashCode());

		return new EpidemicWholeMessages(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode), (request instanceof EpidemicHashMessage));
	}

	public Node selectPeer(Node lnode)
	{
		List<News> list = ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews();

		Node n = null;
		if (list.size() == 0)
			n = ((LinkableSN)(lnode.getProtocol(pidNetworkManger))).getPeer(lnode);
		else
			n = list.get(CommonState.r.nextInt(list.size())).getNode();

		Node peer = ((LinkableSN)(lnode.getProtocol(pidNetworkManger))).getFriendPeer(lnode, n);

		while (peer.getID() == lastSelectedPeer)
			peer = ((LinkableSN)(lnode.getProtocol(pidNetworkManger))).getFriendPeer(lnode, n);
		
		return peer;
	}

	public boolean isInfected()
	{
		return infected;
	}

	public void setInfected(boolean infected)
	{
		this.infected = infected;
	}

}
