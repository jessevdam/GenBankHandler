package nl.wur.ssb.GenBankHandler.data;

public class CrossRef
{
	private String db;
	private String id;
	public CrossRef(String db,String id)
	{
		this.db = db;
		this.id = id;
	}
	public String getDb()
	{
		return this.db;
	}
	public String getId()
	{
		return this.id;
	}
	public String toString()
	{
		return this.db + ":" + this.id;
	}
}
