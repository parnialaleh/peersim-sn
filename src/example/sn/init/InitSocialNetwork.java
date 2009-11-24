package example.sn.init;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.IdleProtocol;
import peersim.core.Network;

public class InitSocialNetwork implements Control
{
	private static final String PAR_IDLE = "idleProtocol";
	private static final String PAR_FILE = "file";
	
	private final int pid;
	private final String fileName;
	
	public InitSocialNetwork(String n)
	{
		this.pid = Configuration.getPid(n + "." + PAR_IDLE);
		this.fileName = Configuration.getString(n + "." + PAR_FILE);
	}
	
	public boolean execute()
	{
		if (CommonState.getTime() != 0) return false;
		
		String line;
		String tmp[];
		int source;
		int friend;
		try {
		     BufferedReader d = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));
		     while ((line = d.readLine()) != null){
		    	 tmp = line.split(" ");
		    	 source = Integer.parseInt(tmp[0]);
		    	 friend = Integer.parseInt(tmp[1]);
		    	 
		    	 ((IdleProtocol)Network.get(source).getProtocol(pid)).addNeighbor(Network.get(friend));
		     }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
