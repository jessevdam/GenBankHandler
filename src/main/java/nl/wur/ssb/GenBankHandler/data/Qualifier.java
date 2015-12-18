package nl.wur.ssb.GenBankHandler.data;

import java.util.LinkedList;
import java.util.List;

public class Qualifier
{
	private String key;
	private LinkedList<QualifierValue> values = new LinkedList<QualifierValue>();
	public Qualifier(String key,QualifierValue value)
	{
		this.key = key;
		this.values.add(value);
	}
	public String getKey()
	{
		return this.key;
	}
	public List<QualifierValue> getValues()
	{
		return this.values;
	}
	public List<String> getValuesAsStrings()
	{
		List<String> toRet = new LinkedList<String>();
		for(QualifierValue val : this.values)
			toRet.add(val.toString());
		return toRet;
	}
	public void addValue(QualifierValue value)
	{
		this.values.add(value);
	}
}
