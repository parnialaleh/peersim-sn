package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import peersim.util.IncrementalStats;

public class SelectNode
{
	
	private class Node
	{
		long id;
		int minInDegree;
		int maxInDegree;
		
		IncrementalStats is;
		
		public Node(long id, int degree)
		{
			this.id = id;
			this.minInDegree = degree;
			this.maxInDegree = degree;
			
			this.is = new IncrementalStats();
			is.add(degree);
		}
		
		public void addItem(int degree)
		{
			this.is.add(degree);
			this.minInDegree = Math.min(this.minInDegree, degree);
			this.maxInDegree = Math.max(this.maxInDegree, degree);
		}
	}
	
	private Node indexOf(List<Node> list, long id)
	{
		for (Node n : list)
			if (n.id == id)
				return n;
		
		return null;
		
	}
	
	public SelectNode(String file)
	{
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(file))))));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		List<Node> list = new ArrayList<Node>();
		String line = null;
		String[] split = null;
		
		long id = 0;
		int degree = 0;
		Node tmp = null;
		
		
		try {
			while ((line = br.readLine()) != null){ 
				split = line.split(" ");
				id = Long.parseLong(split[0]);
				degree = Integer.parseInt(split[1]);
				
				if ((tmp = indexOf(list, id)) == null)
					tmp = new Node(id, degree);
				else
					tmp.addItem(degree);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Node n : list)
			System.out.println(n.id + " minDegree " + n.minInDegree + " maxDegree " + n.maxInDegree + " is " + n.is);
		
	}
	
	public static void main(String[] args)
	{
		new SelectNode(args[0]);
	}

}
