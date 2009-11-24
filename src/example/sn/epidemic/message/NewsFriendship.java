package example.sn.epidemic.message;

import peersim.core.Node;


public class NewsFriendship extends News
{
	private int destId;
	
	public NewsFriendship(Node source, Node dest)
	{
		super(source);
		this.setDestId(dest.getIndex());
	}

	public void setDestId(int destId) {
		this.destId = destId;
	}

	public int getDestId() {
		return destId;
	}

}
