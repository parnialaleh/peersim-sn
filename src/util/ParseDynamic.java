package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ParseDynamic
{
	
	public ParseDynamic(String[] files)
	{
		/*
		 *  2       3            4 5  6   7   8   9      10      11   12
		 *  1000 control.degree: k 0 file 0 1437 1437 SnInDegree 77 10447
		 *  1000 0 1437 10447
		 */
		for (String f : files)
			parseFile(f);
		
	}
	
	private void parseFile(String fileName)
	{
		String line = null;
		int file = -1;
		int indegree = -1;
		String split[] = null;
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));
			while ((line = br.readLine()) != null){
				split = line.split(" ");
				if (file != Integer.parseInt(split[1])){
					file = Integer.parseInt(split[1]);
					indegree = Integer.parseInt(split[3]);
				}
				System.out.println(" " + split[0] + " " + split[2] + " " + indegree);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new ParseDynamic(args);
	}

}
