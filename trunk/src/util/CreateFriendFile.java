package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import peersim.reports.DegreeStats;

import example.sn.graphob.UpperGraph;

public class CreateFriendFile
{
	private final String sourceFileName;
	private final String destFileName;
	private final String sourceIDFile;

	public CreateFriendFile(String sourceFileName, String sourceIDFile, String destFileName)
	{
		this.sourceFileName = sourceFileName;
		this.destFileName = destFileName;
		this.sourceIDFile = sourceIDFile;

		parseFile();
	}

	private void parseFile()
	{
		try{
			List<Long> nodes = new ArrayList<Long>();

			String line = null;
			String tmp[] = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(sourceIDFile))))));
			while ((line = br.readLine()) != null)
				nodes.add(Long.parseLong(line));
			br.close();

			UpperGraph graph = new UpperGraph(nodes.size());
			br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(sourceFileName))))));
			while ((line = br.readLine()) != null){
				tmp = line.split(" ");
				graph.addSimmetriEdge(nodes.indexOf(Long.parseLong(tmp[0])), nodes.indexOf(Long.parseLong(tmp[1])));
			}
			br.close();

			long id = -1;
			for (int root = 0; root < graph.degree(); root++){
				id = nodes.get(root);
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(destFileName + "" + root)));
				List<Long> friends = graph.getNeighbours(root);
				for (Long f : friends){
					bw.write(id + " " + nodes.get(f.intValue()) + "\n");
						for (Long ff : graph.getNeighbours(f.intValue()))
							bw.write(nodes.get(f.intValue()) + " " + nodes.get(ff.intValue()) + "\n");
				}
				bw.close();
			}

		} catch (Exception e){
			e.printStackTrace();
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CreateFriendFile(args[1], args[0], args[2]);
	}

}
