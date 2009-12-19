package example.sn.init;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import example.sn.node.SNNode;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class InitSocialNetwork implements Control
{
	private static final String PAR_LINKABLE = "linkable";
	private static final String PAR_FILE = "file";
		private static final String PAR_XML = "xml";

	private final int pid;
	private final String fileName;
	private final boolean isXml;

	public InitSocialNetwork(String n)
	{
		this.pid = Configuration.getPid(n + "." + PAR_LINKABLE);
		this.fileName = Configuration.getString(n + "." + PAR_FILE);
		this.isXml = Configuration.contains(n + "." + PAR_XML);
	}

	private boolean parseXML()
	{
		List<String> nodes = new ArrayList<String>();
		try {
			DOMParser parser = new DOMParser();
			parser.parse(fileName);
			Document doc = parser.getDocument();

			NodeList nodelist = doc.getElementsByTagName("node");
			
			System.err.println("Nodes " + nodelist.getLength());
			
			for (int i = 0; i < nodelist.getLength(); i++){
				org.w3c.dom.Node n = nodelist.item(i);
				if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ){
					Element e = (Element)n;
					nodes.add(e.getAttribute("id").split("n")[1]);
				}
			}
			
			int newsize = nodes.size();
			int size = Network.size();
			for (int i = newsize; i < size; i++)
				((SNNode)Network.remove(CommonState.r.nextInt(Network.size()))).setOnline(false);
		
			for (int i = size; i < newsize; i++)
				Network.add((Node)Network.prototype.clone());
		
			System.err.println(Network.size() + " " + nodes.size());

			nodelist = doc.getElementsByTagName("edge");
			for (int i = 0; i < nodelist.getLength(); i++){
				org.w3c.dom.Node n = nodelist.item(i);
				if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
					Element e = (Element)n;
					String src = e.getAttribute("source").split("n")[1];
					String dest = e.getAttribute("target").split("n")[1];

					((Linkable)Network.get(nodes.indexOf(src)).getProtocol(pid)).addNeighbor(Network.get(nodes.indexOf(dest)));
					((Linkable)Network.get(nodes.indexOf(dest)).getProtocol(pid)).addNeighbor(Network.get(nodes.indexOf(src)));
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean execute()
	{

		if (isXml)
			parseXML();

//		try{
//			BufferedReader d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));
//			String line = null;
//			while ((line = d.readLine()) != null){
//				if (line.contains("<node")){
//					nodes.add(line.split("\"")[1].split("n")[1]);
//				}
//				if (line.contains("<edge")){
//					String[] fstElmnt = line.split("\"");
//					String src = fstElmnt[3].split("n")[1];
//					String dest = fstElmnt[5].split("n")[1];
//
//					((Linkable)Network.get(nodes.indexOf(src)).getProtocol(pid)).addNeighbor(Network.get(nodes.indexOf(dest)));
//					((Linkable)Network.get(nodes.indexOf(dest)).getProtocol(pid)).addNeighbor(Network.get(nodes.indexOf(src)));
//				}
//			}
//
//		} catch (Exception e){
//			e.printStackTrace();
//		}

		IncrementalStats isCache = new IncrementalStats();
		Node n;
		for (int i = 0; i < Network.size(); i++){
			n = Network.get(i);
			if (n.isUp())
				isCache.add(((Linkable)n.getProtocol(pid)).degree());
		}
		System.out.println(CommonState.getTime() + " Cache: " + isCache);

		//
		//	String line;
		//	String tmp[] = new String[2];
		//	int source = 0;
		//	int friend = 0;
		//	String oldTmp[] = null;
		//	try {
		//		System.out.println(new Date());
		//		BufferedReader d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));
		//		while ((line = d.readLine()) != null){
		//			oldTmp = tmp;
		//			tmp = line.split(" ");
		//			if (!tmp[0].equals(oldTmp[0]))
		//				source = ids.indexOf(Long.parseLong(tmp[0]));
		//			friend = ids.indexOf(Long.parseLong(tmp[1]));
		//
		//			if (friend != -1)
		//				
		//		}
		//		d.close();
		//		System.out.println(new Date());
		//
		//	} catch (Exception e) {
		//		e.printStackTrace();
		//	}

		return false;
	}
}
