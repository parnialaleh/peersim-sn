package example.sn.init;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class InitSocialNetwork implements Control
{
	class HowToHandler extends DefaultHandler {
		boolean title = false;
		boolean url   = false;
		public void startElement(String nsURI, String strippedName,
				String tagName, Attributes attributes)
		throws SAXException {
			if (tagName.equalsIgnoreCase("title"))
				title = true;
			if (tagName.equalsIgnoreCase("url"))
				url = true;
		}

		public void characters(char[] ch, int start, int length) {
			if (title) {
				System.out.println("Title: " + new String(ch, start, length));
				title = false;
			}
			else if (url) {
				System.out.println("Url: " + new String(ch, start,length));
				url = false;
			}
		}
	}




	private static final String PAR_LINKABLE = "linkable";
	private static final String PAR_FILE = "file";
//	private static final String PAR_XML = "xml";

	private final int pid;
	private final String fileName;
//	private final boolean isXml;

	public InitSocialNetwork(String n)
	{
		this.pid = Configuration.getPid(n + "." + PAR_LINKABLE);
		this.fileName = Configuration.getString(n + "." + PAR_FILE);
//		this.isXml = Configuration.getBoolean(n + " " + PAR_XML, false);
	}

	public boolean execute()
	{
		List<String> nodes = new ArrayList<String>();
		try{
			BufferedReader d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));
			String line = null;
			while ((line = d.readLine()) != null){
				if (line.contains("<node")){
					nodes.add(line.split("\"")[1].split("n")[1]);
				}
				if (line.contains("<edge")){
					String[] fstElmnt = line.split("\"");
					String src = fstElmnt[3].split("n")[1];
					String dest = fstElmnt[5].split("n")[1];

					((Linkable)Network.get(nodes.indexOf(src)).getProtocol(pid)).addNeighbor(Network.get(nodes.indexOf(dest)));
					((Linkable)Network.get(nodes.indexOf(dest)).getProtocol(pid)).addNeighbor(Network.get(nodes.indexOf(src)));
				}
			}

		} catch (Exception e){
			e.printStackTrace();
		}

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
