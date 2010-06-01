package example.sn.epidemic.observer;

import java.util.List;

import example.sn.NewsManager;
import example.sn.epidemic.message.News;
import example.sn.linkable.LinkableSN;
import example.newscast.NodeEntry;
import example.sn.node.SNNode;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class DisseminationObserver implements Control
{
	private static final String PAR_PROTOCOL_NEWS = "protocol.news";
	//private static final String PAR_PROTOCOL_GOSSIP = "protocol.gossip";
	private static final String PAR_PROTOCOL_IDLE = "protocol.idle";

	
	private final int pidNews;
	//private final int pidGossip;
	private final int pidIdle;
	private final String name;

	public DisseminationObserver(String n)
	{
		this.pidNews = Configuration.getPid(n + "." + PAR_PROTOCOL_NEWS);
		//this.pidGossip = Configuration.getPid(n + "." + PAR_PROTOCOL_GOSSIP);
		this.pidIdle = Configuration.getPid(n + "." + PAR_PROTOCOL_IDLE);
		this.name = n;
	}


	public boolean execute()
	{
		List<News> news = null;
		Node n = null;
		//LinkableSN ncast = null;
		LinkableSN idle = null;
		NodeEntry[] friends = null;
		int know = 0;
		int friendsNo = 0;

		IncrementalStats stats = new IncrementalStats();

		for (int i = 0; i < Network.size(); i++){
			n = Network.get(i);
			//News list
			news = ((NewsManager)n.getProtocol(pidNews)).getOwnNews(n);

			//ncast = (LinkableSN)n.getProtocol(pidNewscast);
			idle = (LinkableSN)n.getProtocol(pidIdle);			

			for (News nw : news){
				know = 0;
				friendsNo = 0;
				
				friends = idle.getFriends(n, n);
				for (NodeEntry ne : friends)
					if (ne.n.isUp()){
						if (((NewsManager)ne.n.getProtocol(pidNews)).contains(nw))
							know++;
						friendsNo++;
					}
				
				System.out.println(CommonState.getTime() + " " + name + ": " + n.getID() + " " + ((SNNode)n).getRealID() + " " + " " + friendsNo + " " + know + " " + ((double)know/(double)friendsNo));
				stats.add((double)know / (double)friendsNo);
			}
		}

		System.out.println(CommonState.getTime() + " " + name + " stats: " + stats);

		return false;
	}

}
