package example.sn;

import java.util.List;

import example.sn.message.EpidemicMessage;
import example.sn.message.EpidemicWholeMessages;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.extras.am.epidemic.EpidemicProtocol;
import peersim.extras.am.epidemic.Message;
import peersim.extras.am.epidemic.bcast.Infectable;
import peersim.extras.am.epidemic.bcast.InfectionMessage;

public class EpidemicNews implements EpidemicProtocol, Infectable
{	
	private static final String PAR_NETWORK_MANAGER = "protocol.network_manager";
	private static final String PAR_NEWS_MANAGER = "protocol.news_manager";
	
	protected final int pidNetworkManger;
	protected final int pidNewsManger;

	private boolean infected = false;

	public EpidemicNews(String n)
	{
		this.infected = false;

		this.pidNewsManger = Configuration.getPid(n + "." + PAR_NEWS_MANAGER);
		this.pidNetworkManger = Configuration.getPid(n + "." + PAR_NETWORK_MANAGER);
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

	@SuppressWarnings("unchecked")
	public void merge(Node lnode, Node rnode, Message msg) {
		//never terminate (?)
		infected = infected || ((NewsManager)lnode.getProtocol(pidNewsManger)).merge((List<EpidemicMessage>)msg);
	}

	public Message prepareRequest(Node lnode, Node rnode) {
		return (infected?
				new EpidemicWholeMessages(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews()) :
					new InfectionMessage(false));
	}

	public Message prepareResponse(Node lnode, Node rnode, Message request) {
		if (!infected)
			return null;
		
		return new EpidemicWholeMessages(true, ((NewsManager)lnode.getProtocol(pidNewsManger)).getNews());
	}

	public Node selectPeer(Node lnode)
	{
		//ask the peer to Newscast
		return ((NewscastED)(lnode.getProtocol(pidNetworkManger))).getPeer();
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
