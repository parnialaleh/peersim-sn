package example.sn.node;

import peersim.core.GeneralNode;

public class SNNode extends GeneralNode
{
	private boolean isOnline = false;

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

}
