package example.sn.init;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.IdleProtocol;
import peersim.core.Network;

public class InitSocialNetwork implements Control
{
	private static final String PAR_IDLE = "idleProtocol";
	private static final String PAR_FILE = "file";
	private static final String PAR_ID   = "id";

	private final int pid;
	private final String fileName;
	private final String fileID;
	
	private ArrayList<Long> ids = null;

	public InitSocialNetwork(String n)
	{
		this.pid = Configuration.getPid(n + "." + PAR_IDLE);
		this.fileName = Configuration.getString(n + "." + PAR_FILE);
		this.fileID = Configuration.getString(n + "." + PAR_ID);
	}

	public boolean execute()
	{
		//if (CommonState.getTime() != 0) return false;
		
		System.out.println("ParseID");
		parseID();

		String line;
		String tmp[] = new String[2];
		int source = 0;
		int friend = 0;
		String oldTmp[] = null;
		try {
			System.out.println(new Date());
			BufferedReader d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));
			while ((line = d.readLine()) != null){
				oldTmp = tmp;
				tmp = line.split(" ");
				if (!tmp[0].equals(oldTmp[0]))
					source = ids.indexOf(Long.parseLong(tmp[0]));
				friend = ids.indexOf(Long.parseLong(tmp[1]));

				if (friend != -1)
					((IdleProtocol)Network.get(source).getProtocol(pid)).addNeighbor(Network.get(friend));
			}
			d.close();
			System.out.println(new Date());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private void parseID()
	{
		String line = null;
		ids = new ArrayList<Long>();
		
		try{
			BufferedReader d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileID))))));
			String[] tmp = null;
			while ((line = d.readLine()) != null){
				tmp = line.split(" ");
				ids.add(Long.parseLong(tmp[tmp.length-1]));
			}

			d.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
