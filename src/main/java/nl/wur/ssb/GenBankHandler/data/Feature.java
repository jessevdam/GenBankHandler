package nl.wur.ssb.GenBankHandler.data;

import java.util.Collection;
import java.util.LinkedHashMap;

import nl.wur.ssb.GenBankHandler.data.location.Location;

public class Feature
{
	public String key;
	public Location location;
	private LinkedHashMap<String,Qualifier> qualifiers = new LinkedHashMap<String,Qualifier>();
	public Feature(String key)
	{
		this.key = key;
	}
	
	public void addQualifier(String key,String val)
	{
		Qualifier qual = this.qualifiers.get(key);
		if(qual == null)
		{
			qualifiers.put(key,new Qualifier(key,val));
		}
		else
		{
			qual.addValue(val);
		}
	}
	public Qualifier getQualifier(String key)
	{
		return this.qualifiers.get(key);
	}
	public Collection<Qualifier> getAllQualifiers()
	{
		return this.qualifiers.values();
	}
}
