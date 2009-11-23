package example.sn.epidemic.message;


public class NewFriendshipMessage extends New
{
	private int sourceID;
	private int destId;
	
	public NewFriendshipMessage(int sourceID, int destId)
	{
		super();
		this.setSourceID(sourceID);
		this.setDestId(destId);
	}

	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	public int getSourceID() {
		return sourceID;
	}

	public void setDestId(int destId) {
		this.destId = destId;
	}

	public int getDestId() {
		return destId;
	}

}
