package example.sn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import peersim.core.Node;
import peersim.edsim.EDProtocol;

import example.sn.message.EpidemicMessage;

/**
 * Class to manage all news.
 * 
 * @author Roberto Zandonati
 *
 */
public class NewsManager implements EDProtocol
{

	private List<EpidemicMessage> news = null;
	
	public NewsManager()
	{
		this.news = new ArrayList<EpidemicMessage>();
	}
	
	public Object clone()
	{
		NewsManager nm = null;
		try { nm = (NewsManager) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		nm.news = new ArrayList<EpidemicMessage>();

		return nm;
	}
	
	public void addNews(EpidemicMessage news)
	{
		this.news.add(news);
	}
	
	public List<EpidemicMessage> getNews()
	{
		return this.news;
	}
	
	public EpidemicMessage getNews(int index)
	{
		return this.news.get(index);
	}
	
	public boolean merge(List<EpidemicMessage> messages)
	{
		boolean addSomething = this.news.addAll(messages);
		Collections.sort(this.news);
		return addSomething;
	}

	public void processEvent(Node node, int pid, Object event) {}
	
}
