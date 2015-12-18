package nl.wur.ssb.GenBankHandler.data.location;

import nl.wur.ssb.GenBankHandler.parser.ParseException;

public class ComplementLocation implements Location
{
	private Location location;
	
	public ComplementLocation()
	{
		
	}
	
	public ComplementLocation(Location location)
	{
		this.location = location;
	}
	
	public void addLocation(Location location) throws Exception
	{
		if (this.location != null)
			throw new ParseException("Complement already has a child location object");
		this.location = location;
	}
	
	public void addPosition(Position position) throws Exception
	{
		throw new ParseException("Position within complement not allowed");
	}
	
	public String toString(int sequenceSize)
	{
		return "complement(" + this.location.toString(sequenceSize) + ")";
	}
	
	public int getBeginPosition()
	{
		return location.getEndPosition();
	}
	
	public int getEndPosition()
	{
		return location.getBeginPosition();
	}
	
}
