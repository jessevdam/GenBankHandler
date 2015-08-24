package nl.wur.ssb.GenBankHandler.data.location;

import nl.wur.ssb.GenBankHandler.parser.ParseException;

public class Accession
{
	private String id;
	private int version;
	public Accession(String id,int version)
	{
		this.id = id;
		this.version = version;
	}
	
	public String getId()
	{
		return this.id;
	}
	public void setVersion(int version) throws Exception
	{
		if(this.version != 0)
			throw new ParseException("Record can only have one version per accession number");
		this.version = version;
	}
	public int getVersion()
	{
		return this.version;
	}
	public String toString()
	{
		if(version == 0)
			return id;
		return "" + id + "." + version;
	}
}
