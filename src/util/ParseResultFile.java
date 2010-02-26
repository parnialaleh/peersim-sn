package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class ParseResultFile
{

	public ParseResultFile(String fileName) throws Exception
	{
		String line = null;
		String[] split = null;
		boolean isNewsTest = true;
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))));

		while ((line = br.readLine()) != null){ 
			if (line.indexOf("control") > 0){
				split = line.split(" ");

				if (split[11].equals("1.0") && isNewsTest){
					isNewsTest = false;
					System.out.println((Long.parseLong(split[0])-2000) + " " + split[11] + " " + split[9] + " " + split[6] + " " + split[7]);
				}

				if (split[0].equals("2000"))
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
			new ParseResultFile(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
