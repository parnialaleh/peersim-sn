package example.sn.epidemic.observer;

import java.util.List;

import example.sn.NewsManager;
import example.sn.epidemic.message.News;
import example.sn.newscast.NewscastED;
import example.sn.newscast.NewscastSN;
import example.sn.newscast.NodeEntry;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class DisseminationObserver implements Control
{
	private static final String PAR_PROTOCOL_NEWS = "protocol.news";
	private static final String PAR_PROTOCOL_NEWSCAST = "protocol.newscast";
//	private static final String PAR_PROTOCOL_IDLE = "protocol.idle";

	private final int pidNews;
	private final int pidNewscast;
//	private final int pidIdle;
	private final String name;

	public DisseminationObserver(String n)
	{
		this.pidNews = Configuration.getPid(n + "." + PAR_PROTOCOL_NEWS);
		this.pidNewscast = Configuration.getPid(n + "." + PAR_PROTOCOL_NEWSCAST);
//		this.pidIdle = Configuration.getPid(n + "." + PAR_PROTOCOL_IDLE);
		this.name = n;
	}


	public boolean execute()
	{
		List<News> news = null;
		Node n = null;
		NewscastED ncast = null;
		NodeEntry[] friends = null;
		int know = 0;
		int friendsNo = 0;

		IncrementalStats stats = new IncrementalStats();

		for (int i = 0; i < Network.size(); i++){
			n = Network.get(i);
			//News list
			news = ((NewsManager)n.getProtocol(pidNews)).getOwnNews(n);

			ncast = (NewscastED)n.getProtocol(pidNewscast);
			//Friend list
			friends = ncast.getFriends();

			for (News nw : news){
				know = 0;
				friendsNo = 0;
				for (NodeEntry ne : friends)
					if (ne.n.isUp()){
						if (((NewsManager)ne.n.getProtocol(pidNews)).contains(nw))
							know++;
						friendsNo++;
					}
				System.out.println(n.getID() + " " + " " + friendsNo + " " + know + " " + friends.length + " " + ncast.degree());
				stats.add((double)know / (double)friendsNo);
			}
		}

		System.out.println(CommonState.getTime() + " " + name + ": " + stats);

		return false;
	}

}
