package example.sn.idle;

import example.sn.linkable.LinkableSN;
import example.newscast.NodeEntry;
import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;

public class IdleProtocolSN extends LinkableSN implements Protocol
{
	private static final int DEFAULT_INITIAL_CAPACITY = 50;
	private static final String PAR_INITCAP = "capacity";
	protected NodeEntry[] neighbors;
	protected int len;
	protected int pid;

	public IdleProtocolSN(String s)
	{
		neighbors = new NodeEntry[Configuration.getInt(s + "." + PAR_INITCAP,
				DEFAULT_INITIAL_CAPACITY)];
		pid = Configuration.lookupPid(s.split("protocol.")[1]);
		len = 0;
	}

	public Object clone()
	{
		IdleProtocolSN ip = null;
		try { ip = (IdleProtocolSN) super.clone(); }
		catch( CloneNotSupportedException e ) {} 
		ip.neighbors = new NodeEntry[neighbors.length];
		ip.len = 0;
		ip.pid = pid;
		return ip;
	}

	public boolean contains(Node n)
	{
		for (int i = 0; i < len && neighbors[i] != null; i++) {
			if (neighbors[i].n.getID() == n.getID())
				return true;
		}
		return false;
	}

	public boolean addNeighbor(Node n)
	{
		for (int i = 0; i < len; i++) {
			if (neighbors[i].n == n)
				return false;		
		}

		if (len == neighbors.length) {
			NodeEntry[] temp = new NodeEntry[3 * neighbors.length / 2];
			System.arraycopy(neighbors, 0, temp, 0, neighbors.length);
			neighbors = temp;
		}

		neighbors[len] = new NodeEntry();
		neighbors[len].n = n;
		neighbors[len].type = FRIEND;
		len++;
		return true;
	}

	// --------------------------------------------------------------------------

	public Node getNeighbor(int index)
	{
		/*int tmp = len;
		if (index >= len)
			for (int i = 0; i < neighbors.length && neighbors[i] != null; i++){
				int tmp2 = tmp;
				tmp += ((IdleProtocolSN)neighbors[i].n.getProtocol(pid)).localDegree();
				if (tmp > index){
					return ((IdleProtocolSN)neighbors[i].n.getProtocol(pid)).getNeighbor((index - tmp2));
				}
			}
		else{
			return neighbors[index].n;
		}*/
		
		return neighbors[index].n;
	}

	// --------------------------------------------------------------------------

	public int localDegree()
	{
		return len;
	}

	public int degree()
	{
		int degree = len;

		//for (int i = 0; i < neighbors.length && neighbors[i] != null; i++)
		//	degree += ((IdleProtocolSN)neighbors[i].n.getProtocol(pid)).localDegree();

		return degree;
	}

	// --------------------------------------------------------------------------

	public void pack()
	{
		if (len == neighbors.length)
			return;
		NodeEntry[] temp = new NodeEntry[len];
		System.arraycopy(neighbors, 0, temp, 0, len);
		neighbors = temp;
	}

	// --------------------------------------------------------------------------

	public String toString()
	{
		if( neighbors == null ) return "DEAD!";
		StringBuffer buffer = new StringBuffer();
		buffer.append("len=" + len + " maxlen=" + neighbors.length + " [");
		for (int i = 0; i < len; ++i) {
			buffer.append(neighbors[i].n.getID() + " ");
		}
		return buffer.append("]").toString();
	}

	// --------------------------------------------------------------------------

	public void onKill()
	{
		neighbors = null;
		len = 0;
	}

	public boolean containsAsFriend(Node lnode, Node n)
	{
		for (int i = 0; i < len && neighbors[i] != null; i++) {
			if ((neighbors[i].n.getID() == n.getID()) && (neighbors[i].type == FRIEND))
				return true;
		}
		return false;
	}

	public Node getFriendPeer(Node lnode, Node n)
	{
		final int d = localDegree();
		if (d == 0)
			return null;
		int index = CommonState.r.nextInt(d);
		Node result = neighbors[index].n;

		if ((result.isUp()) && (neighbors[index].type == FRIEND))
			return result;

		for (int i = index + 1; i < d; ++i){
			if((neighbors[i].n.isUp()) && (neighbors[i].type == FRIEND))
				return neighbors[i].n;
		}

		for (int i = index - 1; i >= 0; --i)
			if ((neighbors[i].n.isUp()) && (neighbors[i].type == FRIEND))
				return neighbors[i].n;

		return null;
	}

	public NodeEntry[] getFriends(Node lnode, Node n)
	{
		if (lnode.equals(n))
			return getFriends(neighbors);
		else
			for (int j = 0; j < neighbors.length && neighbors[j] != null; j++)
				if (neighbors[j].n.equals(n))
					return getFriends(((IdleProtocolSN)neighbors[j].n.getProtocol(pid)).neighbors);

		System.err.println(((SNNode)lnode).getRealID() + " Not Found " + ((SNNode)n).getRealID());
		return new NodeEntry[0];
	}
	
	public Node getPeer(Node node)
	{
		return neighbors[CommonState.r.nextInt(len)].n;
	}

	@Override
	public Node[] getNodes(Node node)
	{
		Node[] nodes = new Node[this.neighbors.length];
		for (int i = 0; i < nodes.length; i++)
			nodes[i] = this.neighbors[i].n;
		
		return nodes;
	}

	@Override
	public void clearCache() {
		// TODO Auto-generated method stub
	}

}
