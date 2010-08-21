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


public class ParseMessageCount
{

	private class Key implements Comparable<Key>
	{
		public long sourceNode;
		public long destNode;
		public long eventTime;
		public int indegree;

		public Key(long sourceNode, long destNode, long eventTime, int indegree)
		{
			this.sourceNode = sourceNode;
			this.destNode = destNode;
			this.eventTime = eventTime;
			this.indegree = indegree;
		}

		public int compareTo(Key o) {
			if (sourceNode == o.sourceNode && destNode == o.destNode && eventTime == o.eventTime)
				return 0;
			else
				if (sourceNode < o.sourceNode)
					return -1;
				else return 1;
		}
	}

	public ParseMessageCount(String fileNames[]) throws Exception
	{
		for (String s : fileNames)
			parseFile(s);
	}

	private void parseFile(String fileName) throws Exception
	{
		String line = null;
		String[] split = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));

		TreeMap<Key, Integer> maps = new TreeMap<Key, Integer>();

		//6772028 send: file 10 827 947 6760000 1586 indegree 203 1448
		while ((line = br.readLine()) != null){ 
			if (line.indexOf("send:") > 0){
				try{
					split = line.split(" ");

					Key k = new Key(Long.parseLong(split[4]),
							Long.parseLong(split[5]),
							Long.parseLong(split[6]),
							Integer.parseInt(split[9]));

					Integer count = maps.get(k);
					if (count == null)
						count = 1;
					else
						count++;
					
					maps.put(k, count);



				} catch (Exception ex){System.err.println(ex);}
			}
		}

		TreeMap<Integer, IncrementalStats> maps2 = new TreeMap<Integer, IncrementalStats>();
		Iterator<Key> it = maps.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Integer count = maps.get(key);
			
			IncrementalStats is2 = maps2.get(((Key)key).indegree);
			if (is2 == null)
				is2 = new IncrementalStats();
			if (count != null)
				is2.add(count);
			
			maps2.put(((Key)key).indegree, is2);
		}
		
		Iterator<Integer> it2 = maps2.keySet().iterator();
		while (it2.hasNext()) {
			Object key = it2.next();
			IncrementalStats is2 = maps2.get(key);
			
			System.out.println(key + " " + is2);
		}
		
		br.close();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			new ParseMessageCount(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
