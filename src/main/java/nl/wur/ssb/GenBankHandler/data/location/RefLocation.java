package nl.wur.ssb.GenBankHandler.data.location;

//Location of a REFERENCE
public class RefLocation
{
	public int start;
	public int end;
	public RefLocation(int start,int end)
	{
		this.start = start;
		this.end = end;
	}
	public String toString()
	{
		return "" + (start + 1) + " to " + end;
	}
}
