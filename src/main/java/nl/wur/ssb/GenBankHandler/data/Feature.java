package nl.wur.ssb.GenBankHandler.data;

import java.util.ArrayList;

import nl.wur.ssb.GenBankHandler.data.location.Location;

public class Feature
{
	public String key;
	public Location location;
	public ArrayList<Qualifier> qualifiers = new ArrayList<Qualifier>();
	public Feature(String key)
	{
		this.key = key;
	}
}
