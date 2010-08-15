package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import peersim.util.IncrementalStats;


public class ParseResultFile
{

	private class SubEntry
	{
		public long time;
		public double know;
		
		public SubEntry(long time, double know)
		{
			this.time = time;
			this.know = know;
		}
	}
	
	private class Entry
	{		
		public long node;
		public int messageID;
		public long startTime;
		public long endTime;
		public List<SubEntry> list = null;

		public Entry(long node, int id, long startTime)
		{
			this.node = node;
			this.messageID = id;
			this.startTime = startTime;
			this.endTime = -1;
			this.list = new ArrayList<SubEntry>();
		}

		public void addEntry(long time, double know)
		{
			this.list.add(new SubEntry(time - startTime, know));
		}
		
		public long getTime()
		{
			return endTime - startTime;
		}
	}

	public ParseResultFile(String fileNames[]) throws Exception
	{
		for (String s : fileNames)
			parseFile(s);
	}

	private int indexOf(List<Entry> list, long nodeID, int messageID)
	{
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).node == nodeID && list.get(i).messageID == messageID)
				return i;

		return -1;
	}

	private void parseFile(String fileName) throws Exception
	{
		String line = null;
		String[] split = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));

		List<Entry> list = new ArrayList<Entry>();
		int index = -1;

		IncrementalStats is = new IncrementalStats();
		Entry e = null;

		while ((line = br.readLine()) != null){ 
			if (line.indexOf("control.dissObs:") > 0){
				split = line.split(" ");
				try{
					index = indexOf(list, Long.parseLong(split[7]), Integer.parseInt(split[5]));
					if (index == -1){
						e = new Entry(Long.parseLong(split[7]), Integer.parseInt(split[5]), Long.parseLong(split[0]));
						e.addEntry(Long.parseLong(split[0]), Double.parseDouble(split[14]));
						list.add(e);
					}
					
					//if (Integer.parseInt(split[10]) > 1000)				
						list.get(index).addEntry(Long.parseLong(split[0]), Double.parseDouble(split[14]));

					if (split[14].equals("1.0")){
						if (list.get(index).endTime < 0){
							list.get(index).endTime = Long.parseLong(split[0]);
							System.out.println(list.get(index).node + " " +
									list.get(index).messageID + " " +
									list.get(index).startTime + " " +
									list.get(index).endTime + " " +
									list.get(index).getTime());
							is.add(list.get(index).getTime());
						}
					}
				} catch (Exception ex){}
			}
		}

		System.out.println("STATS " + is);
		
		TreeMap<Long, IncrementalStats> maps = new TreeMap<Long, IncrementalStats>();
		
		for (Entry entry: list){
			for (SubEntry sentry : entry.list){
				IncrementalStats is1 = maps.get(sentry.time);
				if (is1 == null)
					is1 = new IncrementalStats();
				
				is1.add(sentry.know);
				maps.put(sentry.time, is1);
			}
		}
		
		Iterator<Long> it = maps.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			IncrementalStats is2 = maps.get(key);
			System.out.println("STATS: " + key + " " + is2);
		}

		br.close();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			new ParseResultFile(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
