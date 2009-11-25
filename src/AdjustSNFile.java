import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class AdjustSNFile
{

	private class Data implements Comparable<Data>, Comparator<Data>
	{
		public List<Long> friends;
		public long source;

		public Data(long source)
		{
			this.friends = new ArrayList<Long>();
			this.source = source;
		}

		public void replace(long s, long d)
		{
			for (int i = 0; i < friends.size(); i++)
				if (friends.get(i) == s)
					friends.set(i, d);

		}

		public int compareTo(Data o) {
			return (source == o.source) ? 0 : 1;
		}

		public int compare(Data o1, Data o2) {
			return (o1.source == o2.source) ? 0 : 1;
		}
	}

	private final String sourcefileName;
	private final String destfileName;

	public AdjustSNFile(String sourcefileName, String destfileName)
	{
		this.sourcefileName = sourcefileName;
		this.destfileName = destfileName;
		
		parseFile();
	}
	/**
	 * @param args args[0] sourcefile args[1] dest file
	 */
	public static void main(String[] args)
	{
		new AdjustSNFile(args[0], args[1]);
	}

	private void parseFile()
	{
		try {
			String line = null;
			String tmp[];
			long s, d;
			int i;
			int j=0;
			List<Data> list = new ArrayList<Data>();

			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(new FileInputStream(new File(sourcefileName))))));

			while ((line = br.readLine()) != null){
				tmp = line.split(" ");
				s = Long.parseLong(tmp[0]);
				d = Long.parseLong(tmp[1]);

				i = list.indexOf(new Data(s));
				if (i < 0){
					list.add(new Data(s));
					i = list.size() - 1;
				}
				list.get(i).friends.add(d);
				j++;
				if (j%65536 == 0)
					System.out.println(j);
			}
			br.close();

			int networkSize = list.size();
			System.err.println("sourcefile read, network size " + networkSize);
			
			for (i = 0; i < networkSize; i++){
				s = list.get(i).source;
				list.get(i).source = i;
				replace(list, s, i);
			}

			System.err.println("Print to file");
			printToFile(list);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void replace(List<Data> list, long source, long newSource)
	{
		for (Data d: list)
			d.replace(source, newSource);
	}

	private void printToFile(List<Data> list)
	{
		try{
			PrintStream p = new PrintStream(new FileOutputStream(destfileName));
			
			for (Data d: list){
				for (Long i : d.friends)
					p.println (d.source + " " + i);
			}			
			p.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
