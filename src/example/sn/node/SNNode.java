package example.sn.node;

import peersim.core.GeneralNode;

public class SNNode extends GeneralNode
{
	private boolean isOnline = false;
	private long realID = -1;

	public SNNode(String n)
	{
		super(n);
		this.setOnline(true);
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public boolean isUp() {
		return isOnline;
	}

	public void setRealID(long realID) {
		this.realID = realID;
	}

	public long getRealID() {
		return realID;
	}

}
