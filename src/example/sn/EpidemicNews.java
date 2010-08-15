package example.sn;

import java.util.List;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.extras.am.epidemic.EpidemicProtocol;
import peersim.extras.am.epidemic.Message;
import peersim.extras.am.epidemic.bcast.Infectable;
import example.sn.epidemic.message.EpidemicHashMessage;
import example.sn.epidemic.message.EpidemicResponseMessage;
import example.sn.epidemic.message.EpidemicWholeMessages;
import example.sn.epidemic.message.News;
import example.sn.linkable.LinkableSN;

public class EpidemicNews implements EpidemicProtocol, Infectable
{	
	private static final String PAR_GOSSIP = "protocol.gossip";
	private static final String PAR_IDLE = "protocol.idle";
	private static final String PAR_NEWS_MANAGER = "protocol.news_manager";
	private static final String PAR_HASH_MESSAGE = "hash";
	private static final String PAR_PROB = "k";

	protected final int pidGossip;
	protected final int pidIdle;
	protected final int pidNewsManger;
	protected final boolean hash_message;
	protected final double k;

	private boolean infected = false;

	//private long lastSelectedPeer = Long.MIN_VALUE;

	public EpidemicNews(String n)
	{
		this.infected = false;

		this.pidNewsManger = Configuration.getPid(n + "." + PAR_NEWS_MANAGER);
		this.pidGossip = Configuration.getPid(n + "." + PAR_GOSSIP);
		this.pidIdle = Configuration.getPid(n + "." + PAR_IDLE);
		this.hash_message = Configuration.getBoolean(n + "." + PAR_HASH_MESSAGE, true);
		this.k = Configuration.getDouble(n + "." + PAR_PROB);
	}

	public Object clone()
	{
		EpidemicNews ev = null;
		try {
			ev = (EpidemicNews)super.clone();
		} catch (CloneNotSupportedException e) {}
		ev.infected = false;
		return ev;	
	}

	public void merge(Node lnode, Node rnode, Message msg) {
		boolean res = ((NewsManager)lnode.getProtocol(pidNewsManger)).merge(((EpidemicWholeMessages)msg).getMessages());
		if (!res && infected && ((EpidemicWholeMessages)msg).isFirst()){
			infected = CommonState.r.nextDouble() >= k;
			if (!infected)
				System.out.println("LOST INTEREST " + CommonState.getIntTime());
		}
		else if (res)
			infected = true;

	}

	private boolean cmpList(List<News> l1, List<News> l2)
	{
		for (News n : l2)
			if (!l1.contains(n))
				return false;

		return true;
	}

	public Message prepareRequest(Node lnode, Node rnode) {
		if (!infected)
			return null;

		if (hash_message)
			return new EpidemicHashMessage(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode));

		return new EpidemicWholeMessages(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode, pidNewsManger), true);
	}

	public Message prepareResponse(Node lnode, Node rnode, Message request) {
		if (request instanceof EpidemicHashMessage && ((EpidemicHashMessage)request).getStatus()){
			/*List<News> list = ((NewsManager)lnode.getProtocol(pidNewsManger)).getOwnNews(lnode);
			if (!cmpList(list, ((EpidemicHashMessage)request).getList()))
				return new EpidemicResponseMessage(false, true);
			else
				return new EpidemicResponseMessage(false, false);*/
			return new EpidemicHashMessage(false, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode));
		}
		else if (request instanceof EpidemicHashMessage && !((EpidemicHashMessage)request).getStatus()){
			//List<News> list = ((NewsManager)lnode.getProtocol(pidNewsManger)).getOwnNews(lnode);
			//if (!cmpList(list, ((EpidemicHashMessage)request).getList()))
			return new EpidemicWholeMessages(false, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode, pidNewsManger), true);
			//else
			//	return new EpidemicResponseMessage(false, false);
		}
		else if (request instanceof EpidemicWholeMessages){
			List<News> list = ((NewsManager)lnode.getProtocol(pidNewsManger)).getOwnNews(lnode);
			if (!cmpList(list, ((EpidemicWholeMessages)request).getMessages()))
				return new EpidemicWholeMessages(false, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode, pidNewsManger), false);
		}
		/*else if ((request instanceof EpidemicResponseMessage) && ((EpidemicResponseMessage)request).isInterested())
			//contatto necessario
			return new EpidemicWholeMessages(false, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews(lnode, rnode, pidNewsManger), (request instanceof EpidemicHashMessage));
		else if ((request instanceof EpidemicResponseMessage) && !((EpidemicResponseMessage)request).isInterested())
			//contatto non necessario
			if (infected)
				infected = CommonState.r.nextDouble() >= k;
						return null;*/

		return null;

	}

	/*private boolean isInList(Node n, List<News> list)
	{
		for (News nw : list)
			if (nw.getSourceNode().equals(n) || nw.getDestNode().equals(n))
				return true;
		return false;
	}*/

	/*private int findLastSelectedCluster(List<List<Node>> cluster, long lastSelectedPeer)
	{
		for (int i = 0; i < cluster.size(); i++)
			for (Node n : cluster.get(i))
				if (n.getID() == lastSelectedPeer)
					return i;
		return -1;
	}*/

	public Node selectPeer(Node lnode)
	{		
		Node n = ((LinkableSN)(lnode.getProtocol(pidGossip))).getPeer(lnode);

		return n;
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
