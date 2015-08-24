package nl.wur.ssb.GenBankHandler.data.location;

import nl.wur.ssb.GenBankHandler.parser.ParseException;

public class PairLocation implements Location
{
	public Position begin;
	public Position end;
	public PairLocation()
	{
		
	}
	public PairLocation(Position begin,Position end)
	{
		this.begin = begin;
		this.end = end;
	}
	public void addLocation(Location location) throws Exception
	{
		throw new ParseException("Location within Pair location not allowed");
	}

	public void addPosition(Position position) throws Exception
	{
		if(this.begin == null)
			this.begin = position;
		else if(this.end == null)
			this.end = position;
		else
		  throw new ParseException("Pair can have only 2 positions and not more");		
	}
	public String toString(int sequenceSize)
	{
		return "" + begin.toString(1) + ".." + end.toString(0);
	}
}
