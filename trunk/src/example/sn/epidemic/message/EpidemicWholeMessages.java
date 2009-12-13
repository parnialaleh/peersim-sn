package example.sn.epidemic.message;

import java.util.List;

import peersim.extras.am.epidemic.bcast.InfectionMessage;

public class EpidemicWholeMessages extends InfectionMessage
{
	private List<News> messages = null;
	private boolean isFirst = false;
	
	public EpidemicWholeMessages(boolean status,  List<News> messages, boolean isFirst)
	{
		super(status);
		this.messages = messages;
		this.isFirst = isFirst;
	}

	public List<News> getMessages()
	{
		return messages;
	}
	
	public boolean isFirst()
	{
		return this.isFirst;
	}

	
}
