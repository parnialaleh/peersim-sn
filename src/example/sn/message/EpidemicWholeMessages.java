package example.sn.message;

import java.util.List;

import peersim.extras.am.epidemic.bcast.InfectionMessage;

public class EpidemicWholeMessages extends InfectionMessage
{

	private List<EpidemicMessage> messages = null;
	
	public EpidemicWholeMessages(boolean status,  List<EpidemicMessage> messages)
	{
		super(status);
		this.messages = messages;
	}

	public List<EpidemicMessage> getMessages() {
		return messages;
	}
	
}
