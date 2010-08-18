package example.sn.epidemic;

import example.sn.epidemic.message.EpidemicHashMessage;
import example.sn.epidemic.message.EpidemicResponseMessage;
import example.sn.epidemic.message.EpidemicWholeMessages;
import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.extras.am.epidemic.EDEpidemicManager;
import peersim.extras.am.epidemic.EpidemicProtocol;
import peersim.extras.am.epidemic.Message;
import peersim.extras.am.epidemic.bcast.InfectionMessage;
import peersim.transport.Transport;

public class EDEpidemicManagerMD5 extends EDEpidemicManager
{

	private static final String PAR_START_TIME = "startTime";
	private final long start;

	public EDEpidemicManagerMD5(String n)
	{
		super(n);
		start = Configuration.getLong(n+ "." + PAR_START_TIME, 0);
	}

	public void processEvent(Node lnode, int thisPid, Object event)
	{
		if (event instanceof Integer)
			EDSimulator.add(c.period, (Integer) event, lnode, thisPid);
		
		if (!((SNNode)lnode).isOnline()) return;
		
		if (event instanceof Integer) {
			if (CommonState.getTime() >= start)
				activeThread(lnode, (Integer) event, thisPid);
		} else {
			passiveThread(lnode, (Message) event, thisPid);
		}
	}

	private void activeThread(Node lnode, Integer pid, int thisPid)
	{
		EpidemicProtocol lpeer = (EpidemicProtocol) lnode.getProtocol(pid);
		Node rnode = lpeer.selectPeer(lnode);

		if (rnode == null){
			System.err.println("EPIDEMIC No Peer " + ((SNNode)lnode).getRealID());
			return;
		}
		Message request = lpeer.prepareRequest(lnode, rnode);

		if (request != null) {
			request.setPid(pid);
			request.setRequest(true);
			request.setSender(lnode);
			Transport tr = (Transport) lnode.getProtocol(c.tid);
			tr.send(lnode, rnode, request, thisPid);
			
			//System.err.println("SEND " + lnode.getID() + "->" + rnode.getID());
		}
	}

	private void passiveThread(Node lnode, Message message, int thisPid)
	{
		InfectionMessage msg = (InfectionMessage)message;
		int pid = msg.getPid();
		EpidemicProtocol lpeer = (EpidemicProtocol) lnode.getProtocol(pid);
		if (msg.isRequest()) {
			InfectionMessage reply = (InfectionMessage)lpeer.prepareResponse(lnode, msg.getSender(), msg);
			if (reply != null) {
				reply.setPid(pid);
				boolean isRequest = reply instanceof EpidemicHashMessage ||  reply instanceof EpidemicResponseMessage || ((reply instanceof EpidemicWholeMessages) && ((EpidemicWholeMessages)reply).isFirst());
				reply.setRequest(isRequest);
				reply.setSender(lnode);
				Transport tr = (Transport) lnode.getProtocol(c.tid);
				tr.send(lnode, msg.getSender(), reply, thisPid);
				
				//System.err.println("SEND " + lnode.getID() + "->" + msg.getSender().getID());
			}
		}
		if (msg instanceof EpidemicWholeMessages)
			lpeer.merge(lnode, msg.getSender(), msg);
	}

}
