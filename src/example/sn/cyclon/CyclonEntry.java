package example.sn.cyclon;

import java.util.Comparator;

import peersim.core.Node;

public class CyclonEntry implements Comparable<CyclonEntry>, Comparator<CyclonEntry>
{
	public Node n;
	public int age;

	public CyclonEntry(){}

	public CyclonEntry(Node n, int age)
	{
		this.n = n;
		this.age = age;
	}

	public int compareTo(CyclonEntry ce)
	{
		if (ce.age > age)
			return 1;
		else if (ce.age == age)
			return 0;
		return -1;
	}

	public int compare(CyclonEntry ce1, CyclonEntry ce2){
		if (ce1.age > ce2.age)
			return 1;
		else if (ce1.age == ce2.age)
			return 0;
		return -1;

	}

	public void increase()
	{
		this.age++;
	}
}
