package nl.wur.ssb.GenBankHandler.data.location;

import nl.wur.ssb.GenBankHandler.parser.ParseException;

public class InBetweenPosition implements Location
{
	public int position;
	public InBetweenPosition(int position)
	{
		this.position = position;
	}
	public void addLocation(Location location) throws Exception
	{
		throw new ParseException("Inbetween locatin can not have childs");
	}
	public void addPosition(Position position) throws Exception
	{
		throw new ParseException("Inbetween locatin can not have childs");		
	}
	public String toString(int sequenceSize)
	{
		int end = position + 1;
		if(position == sequenceSize)
			end = 1;
		return "" + position + "^" + end;
	}
	public int getBeginPosition()
	{
	  return this.position;		
	}
	public int getEndPosition()
	{
      //should be 0 if on end of circular dna
      return this.position + 1;
 	}
}
