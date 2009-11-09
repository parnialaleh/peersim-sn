package example.sn.message;

import peersim.core.Node;

public class NewStatusMessage extends EpidemicMessage {

	public NewStatusMessage(boolean status, Node node)
{
		super(status, node);
	}

}
