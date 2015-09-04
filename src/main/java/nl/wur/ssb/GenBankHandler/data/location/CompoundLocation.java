package nl.wur.ssb.GenBankHandler.data.location;

import java.util.ArrayList;

import nl.wur.ssb.GenBankHandler.parser.ParseException;
import nl.wur.ssb.GenBankHandler.util.Util;

public class CompoundLocation implements Location
{
	public String command; //join|order|bond
	public ArrayList<Location> elements;
	public CompoundLocation(String command)
	{
		this.command = command;
		this.elements = new ArrayList<Location>();
	}
	
	public CompoundLocation(String command,ArrayList<Location> elements)
	{
		this.command = command;
		this.elements = elements;
	}
	
	public void addLocation(Location location) throws Exception
	{
		this.elements.add(location);
	}

	public void addPosition(Position position) throws Exception
	{
		throw new ParseException("Position within compound location not allowed");		
	}

	public String toString(int sequenceSize)
	{
		String toRet = "" + command + "(";
		for(Location elem : this.elements)
		{
			toRet += elem.toString(sequenceSize) + ",";
		}
		return Util.i(toRet,0,-1) + ")";
	}
	public int getBeginPosition()
	{
	  int begin = Integer.MAX_VALUE;
	  for(Location loc : elements)
	  	 begin = Math.min(begin, loc.getEndPosition());
	  return begin;		
	}
	public int getEndPosition()
	{
      int end = Integer.MIN_VALUE;
      for(Location loc : elements)
    	 end = Math.max(end, loc.getEndPosition());
      return end;
 	}
}
