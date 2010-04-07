package example.sn;

import java.util.Collections;
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
import example.sn.linkable.LinkableSN;
import example.sn.linkable.clustering.AnalizeFriends;
import example.sn.node.SNNode;

public class EpidemicNews implements EpidemicProtocol, Infectable
{	
	private static final String PAR_GOSSIP = "protocol.gossip";
	private static final String PAR_IDLE = "protocol.idle";
	private static final String PAR_NEWS_MANAGER = "protocol.news_manager";
	private static final String PAR_HASH_MESSAGE = "hash";

	protected final int pidGossip;
	protected final int pidIdle;
	protected final int pidNewsManger;
	protected final boolean hash_message;

	private boolean infected = false;

	private long lastSelectedPeer = Long.MIN_VALUE;

	public EpidemicNews(String n)
	{
		this.infected = true;

		this.pidNewsManger = Configuration.getPid(n + "." + PAR_NEWS_MANAGER);
		this.pidGossip = Configuration.getPid(n + "." + PAR_GOSSIP);
		this.pidIdle = Configuration.getPid(n + "." + PAR_IDLE);
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

	private boolean isInList(Node n, List<News> list)
	{
		for (News nw : list)
			if (nw.getSourceNode().equals(n))
				return true;
		return false;
	}

	private int findLastSelectedCluster(List<List<Node>> cluster, long lastSelectedPeer)
	{
		for (int i = 0; i < cluster.size(); i++)
			for (Node n : cluster.get(i))
				if (n.getID() == lastSelectedPeer)
					return i;
		return -1;
	}

	public Node selectPeer(Node lnode)
	{
		List<News> list = ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews();

		AnalizeFriends af = new AnalizeFriends(pidGossip, pidIdle, (SNNode)lnode);
		List<List<Node>> cluster = af.analize();
		Collections.shuffle(cluster, CommonState.r);
		for (List<Node> lst : cluster)
			Collections.shuffle(lst, CommonState.r);			

		int lastSelectedCluster = findLastSelectedCluster(cluster, lastSelectedPeer);
		for (int i = 0; i < cluster.size(); i++)
			if (i != lastSelectedCluster){
				for (Node n : cluster.get(i))
					if (isInList(n, list)){
						Node peer = ((LinkableSN)(lnode.getProtocol(pidGossip))).getFriendPeer(lnode, n);
						if (peer != null){
							lastSelectedPeer = peer.getID();
							return peer;
						}
					}
			}

		if (lastSelectedCluster >= 0)
			//Nothing found in other clusters
			for (Node n : cluster.get(lastSelectedCluster))
				if (isInList(n, list)){
					Node peer = ((LinkableSN)(lnode.getProtocol(pidGossip))).getFriendPeer(lnode, n);
					if (peer != null){
						lastSelectedPeer = peer.getID();
						return peer;
					}
				}

		//No messages
		return ((LinkableSN)(lnode.getProtocol(pidGossip))).getPeer(lnode);
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
