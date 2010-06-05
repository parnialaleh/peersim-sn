package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class ParseResultFile
{

	public ParseResultFile(String fileNames[]) throws Exception
	{
		for (String s : fileNames)
			parseFile(s);
	}
	
	private void parseFile(String fileName) throws Exception
	{
		String line = null;
		String[] split = null;
		boolean isNewsTest = true;
		long time = -1;
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));

		while ((line = br.readLine()) != null){ 
			if (line.indexOf("control.dissObs:") > 0){
				split = line.split(" ");

				if (split[11].equals("1.0") && isNewsTest){
					isNewsTest = false;
					time = Long.parseLong(split[0]);
					System.out.println(time + " " + split[11] + " " + split[9] + " " + split[6] + " " + split[7]);
				}

				if (Long.parseLong(split[0]) < time)
					isNewsTest = true;
			}
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
