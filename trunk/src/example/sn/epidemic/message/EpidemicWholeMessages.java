package example.sn.epidemic.message;

import java.util.List;

public class EpidemicWholeMessages extends EpidemicMessage
{
	private List<News> messages = null;
	
	public EpidemicWholeMessages(boolean status,  List<News> messages)
	{
		super(status, false);
		this.messages = messages;
	}

	public List<News> getMessages()
	{
		return messages;
	}
	
}
