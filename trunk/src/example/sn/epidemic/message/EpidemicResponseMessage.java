package example.sn.epidemic.message;

import peersim.extras.am.epidemic.bcast.InfectionMessage;

public class EpidemicResponseMessage extends InfectionMessage {

	private boolean interested;

	public EpidemicResponseMessage(boolean status, boolean intereset) {
		super(status);
		this.interested = intereset;
	}
	
	public boolean isInterested() {
		return interested;
	}

	public void setInterested(boolean interested) {
		this.interested = interested;
	}

}
