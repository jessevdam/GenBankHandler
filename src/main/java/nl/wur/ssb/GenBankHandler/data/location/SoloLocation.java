package nl.wur.ssb.GenBankHandler.data.location;

import nl.wur.ssb.GenBankHandler.parser.ParseException;

public class SoloLocation implements Location
{
	private Position pos;
	
	public SoloLocation()
	{
	
	}
	
	public SoloLocation(Position pos)
	{
	this.pos = pos;
	}
	
	public void addLocation(Location location) throws Exception
	{
	throw new ParseException("Location within Solo location not allowed");
	}
	
	public void addPosition(Position position) throws Exception
	{
	if (this.pos == null)
		this.pos = position;
	else
		throw new ParseException(
			"Solo can have only one position and not more");
	}
	
	public String toString(int sequenceSize)
	{
	return pos.toString(1);
	}
	
	public int getBeginPosition()
	{
	return this.pos.getMinPos();
	}
	
	public int getEndPosition()
	{
	return this.pos.getMaxPos();
	}
}
