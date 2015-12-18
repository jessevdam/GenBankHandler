package nl.wur.ssb.GenBankHandler.data;

import java.util.Collection;
import java.util.LinkedHashMap;

import nl.wur.ssb.GenBankHandler.data.location.Location;

public class Feature
{
	private Record record;
	private String key;
	private Location location;
	private LinkedHashMap<String,Qualifier> qualifiers = new LinkedHashMap<String,Qualifier>();
	Feature(Record record,String key,Location location)
	{
		this.key = key;
		this.record = record;
		this.location = location;
	}
	
	public Qualifier addQualifier(String key,QualifierValue val)
	{
		Qualifier qual = this.qualifiers.get(key);
		if(qual == null)
		{
			qual = new Qualifier(key,val);
			qualifiers.put(key,qual);
		}
		else
		{
			qual.addValue(val);
		}
		return qual;
	}
	public Qualifier getQualifier(String key)
	{
		return this.qualifiers.get(key);
	}
	public Collection<Qualifier> getAllQualifiers()
	{
		return this.qualifiers.values();
	}
	public String getKey()
	{
		return this.key;
	}
	public Location getLocation()
	{
		return this.location;
	}
	public String getLocationString()
	{
		return this.location.toString(this.record.getSize());
	}
}
