package example.sn.epidemic.control;

import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import example.sn.NewsManager;
import example.sn.epidemic.message.NewsFriendship;
import example.sn.epidemic.message.NewsStatusChange;
import example.sn.newscast.LinkableSN;

public class AddNews implements Control
{
	private static final String PAR_PROT_NEWS_MANAGER = "protocol.newsManager";
	private static final String PAR_PROT_IDLE = "protocol.idle";
	private static final String PAR_PROT_NEWSCAST = "protocol.newscast";
	private static final String PAR_FRIENDSHIP = "friendshipNo";
	private static final String PAR_STATUS_CHANGE = "statusChangeNo";
	private static final String PAR_START_PROTOCOL = "starttime";
	private static final String PAR_END_PROTOCOL = "endtime";

	private final int pidNewsManager;
	private final int pidIdle;
	private final int pidNewscast;
	private final int friendshipNo;
	private final int statusChangeNo;
	private final long startTime;
	private final long endTime;

	public AddNews(String n)
	{
		this.pidNewsManager = Configuration.getPid(n + "." + PAR_PROT_NEWS_MANAGER);
		this.pidIdle = Configuration.getPid(n + "." + PAR_PROT_IDLE);
		this.pidNewscast = Configuration.getPid(n + "." + PAR_PROT_NEWSCAST);
		this.friendshipNo = Configuration.getInt(n + "." + PAR_FRIENDSHIP);
		this.statusChangeNo = Configuration.getInt(n + "." + PAR_STATUS_CHANGE);
		this.startTime = Configuration.getLong(n + "." + PAR_START_PROTOCOL, Long.MIN_VALUE);
		this.endTime = Configuration.getLong(n + "." + PAR_END_PROTOCOL, Long.MAX_VALUE);
	}


	public boolean execute()
	{
		if ((CommonState.getTime() >= endTime) || (CommonState.getTime() < startTime))
			return false;
		
		final int size = Network.size();
		Set<Integer> s = new HashSet<Integer>();

		int i;
		NewsManager newsManager;
		while (s.size() < statusChangeNo){
			//i = CommonState.r.nextInt(size);
			i = 0;
			if (!s.contains(i) && Network.get(i).isUp()){
				newsManager = (NewsManager)Network.get(i).getProtocol(pidNewsManager);
				newsManager.addNews(new NewsStatusChange(Network.get(i)), Network.get(i));
				s.add(i);
			}
		}

		s = new HashSet<Integer>();
		LinkableSN idle;
		LinkableSN newscast;
		while (s.size() < friendshipNo){
			i = CommonState.r.nextInt(size);
			if (!s.contains(i)){
				idle = (LinkableSN)Network.get(i).getProtocol(pidIdle);
				newscast = (LinkableSN)Network.get(i).getProtocol(pidNewscast);

				Node n = Network.get(CommonState.r.nextInt(size));
				if (!idle.contains(n) && !newscast.contains(n)){				
					newsManager = (NewsManager)Network.get(i).getProtocol(pidNewsManager);
					newsManager.addNews(new NewsFriendship(Network.get(i), n), Network.get(i));
					s.add(i);
				}
			}
		}

		return false;
	}

}
