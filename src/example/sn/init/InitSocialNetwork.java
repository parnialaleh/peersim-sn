package example.sn.init;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import example.sn.epidemic.control.AddNews;
import example.sn.node.SNNode;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class InitSocialNetwork implements Control
{
	private static final String PAR_LINKABLE = "linkable";
	private static final String PAR_DIRECTORY = "directory";
	private static final String PAR_FILE_BEGIN = "fileBegin";
	private static final String PAR_FILE_NO = "fileNo";
	private static final String PAR_XML = "xml";

	private final int pid;
	private final String directoryName;
	private final String fileBegin;
	private final int fileNo;
	private final boolean isXml;

	public InitSocialNetwork(String n)
	{
		this.pid = Configuration.getPid(n + "." + PAR_LINKABLE);
		this.directoryName = Configuration.getString(n + "." + PAR_DIRECTORY);
		this.fileBegin = Configuration.getString(n + "." + PAR_FILE_BEGIN);
		this.fileNo = Configuration.getInt(n + "." + PAR_FILE_NO);
		this.isXml = Configuration.contains(n + "." + PAR_XML);
	}

	private boolean parseXML()
	{
		List<String> nodes = new ArrayList<String>();
		try {
			DOMParser parser = new DOMParser();
			parser.parse(directoryName);
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
	
	private void adjustNetworkSize(int newSize)
	{
		int size = Network.size();
		for (int i = newSize; i < size; i++)
			((SNNode)Network.remove(CommonState.r.nextInt(Network.size()))).setOnline(false);

		for (int i = size; i < newSize; i++)
			Network.add((Node)Network.prototype.clone());	
	}
	
	public boolean execute()
	{
		if (isXml)
			parseXML();
		
		try {
			String line;
			String tmp[] = null;
			int source = 0;
			int friend = 0;
			
			Set<Long> idsSet = new HashSet<Long>();
			List<Long> ids = new ArrayList<Long>();
			
			BufferedReader d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(directoryName + fileBegin + "" + fileNo))))));
			while ((line = d.readLine()) != null){
				tmp = line.split(" ");
				idsSet.add(Long.parseLong(tmp[0]));
				idsSet.add(Long.parseLong(tmp[1]));
			}
			d.close();
			
			for (Long l : idsSet.toArray(new Long[0]))
				ids.add(l);
			
			adjustNetworkSize(ids.size());

			boolean rootSetted = false;
			d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(directoryName + fileBegin + "" + fileNo))))));
			while ((line = d.readLine()) != null){
				tmp = line.split(" ");
				if (!rootSetted){
					AddNews.setRoot(Long.parseLong(tmp[0]));
					rootSetted = true;
				}
				source = ids.indexOf(Long.parseLong(tmp[0]));
				friend = ids.indexOf(Long.parseLong(tmp[1]));
				((SNNode)Network.get(source)).setRealID(Long.parseLong(tmp[0]));
				((SNNode)Network.get(friend)).setRealID(Long.parseLong(tmp[1]));
				
				((Linkable)Network.get(source).getProtocol(pid)).addNeighbor(Network.get(friend));
				((Linkable)Network.get(friend).getProtocol(pid)).addNeighbor(Network.get(source));				
			}
			d.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
