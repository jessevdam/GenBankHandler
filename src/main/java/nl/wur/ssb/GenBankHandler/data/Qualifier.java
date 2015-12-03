package nl.wur.ssb.GenBankHandler.data;

import java.util.LinkedList;
import java.util.List;

public class Qualifier
{
	private String key;
	private LinkedList<String> values = new LinkedList<String>();
	public Qualifier(String key,String value)
	{
		this.key = key;
		this.values.add(value);
	}
	public String getKey()
	{
		return this.key;
	}
	public List<String> getValues()
	{
		return this.values;
	}
	public void addValue(String value)
	{
		this.values.add(value);
	}
}
