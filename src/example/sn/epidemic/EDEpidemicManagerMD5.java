package example.sn.epidemic;

import example.sn.epidemic.message.EpidemicHashMessage;
import example.sn.epidemic.message.EpidemicWholeMessages;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.extras.am.epidemic.EDEpidemicManager;
import peersim.extras.am.epidemic.EpidemicProtocol;
import peersim.extras.am.epidemic.Message;
import peersim.extras.am.epidemic.bcast.InfectionMessage;
import peersim.transport.Transport;

public class EDEpidemicManagerMD5 extends EDEpidemicManager
{

	public EDEpidemicManagerMD5(String n)
	{
		super(n);
	}
	
	public void processEvent(Node lnode, int thisPid, Object event)
	{
//		if (lnode.getID() == 357)
//			System.out.println(lnode.getID() + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		
		if (event instanceof Integer) {
			activeThread(lnode, (Integer) event, thisPid);
		} else {
			passiveThread(lnode, (Message) event, thisPid);
		}
	}
	
	private void activeThread(Node lnode, Integer pid, int thisPid)
	{
		EDSimulator.add(c.period, pid, lnode, thisPid);
		EpidemicProtocol lpeer = (EpidemicProtocol) lnode.getProtocol(pid);
		Node rnode = lpeer.selectPeer(lnode);
		
		if (rnode == null)
			return;
		Message request = lpeer.prepareRequest(lnode, rnode);

		if (request != null) {
			request.setPid(pid);
			request.setRequest(true);
			request.setSender(lnode);
			Transport tr = (Transport) lnode.getProtocol(c.tid);
			tr.send(lnode, rnode, request, thisPid);
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
				boolean isRequest = reply instanceof EpidemicHashMessage || ((reply instanceof EpidemicWholeMessages) && ((EpidemicWholeMessages)reply).isFirst());
				reply.setRequest(isRequest);
				reply.setSender(lnode);
				Transport tr = (Transport) lnode.getProtocol(c.tid);
				tr.send(lnode, msg.getSender(), reply, thisPid);
			}
		}
		if (!(msg instanceof EpidemicHashMessage))
			lpeer.merge(lnode, msg.getSender(), msg);
	}
	
}
