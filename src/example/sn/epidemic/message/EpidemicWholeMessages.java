package example.sn.epidemic.message;

import java.util.List;

import peersim.core.Node;

public class EpidemicWholeMessages extends EpidemicMessage
{
	private List<News> messages = null;
	
	public EpidemicWholeMessages(boolean status,  List<News> messages, Node n)
	{
		super(status, false, n);
		this.messages = messages;
	}

	public List<News> getMessages()
	{
		return messages;
	}
	
}
