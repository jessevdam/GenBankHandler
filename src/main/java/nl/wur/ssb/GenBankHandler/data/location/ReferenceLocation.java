package nl.wur.ssb.GenBankHandler.data.location;

import nl.wur.ssb.GenBankHandler.parser.ParseException;

public class ReferenceLocation implements Location
{
	public String reference;
	public Location location;
	
	public ReferenceLocation(String reference)
	{
	this.reference = reference;
	}
	
	public ReferenceLocation(String reference, Location location)
	{
	this.reference = reference;
	this.location = location;
	}
	
	public void addLocation(Location location) throws Exception
	{
	if (location != null)
		throw new ParseException(
			"Reference location already has a child location object");
	this.location = location;
	}
	
	public void addPosition(Position position) throws Exception
	{
	throw new ParseException("Position within reference location not allowed");
	}
	
	public String toString(int sequenceSize)
	{
	return "" + this.reference + ":" + location.toString(sequenceSize);
	}
	
	public int getBeginPosition()
	{
	return this.location.getBeginPosition();
	}
	
	public int getEndPosition()
	{
	return this.location.getEndPosition();
	}
	
}
