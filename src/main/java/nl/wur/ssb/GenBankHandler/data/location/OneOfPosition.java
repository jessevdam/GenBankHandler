package nl.wur.ssb.GenBankHandler.data.location;

import java.util.ArrayList;

import nl.wur.ssb.GenBankHandler.util.Util;

public class OneOfPosition implements Position
{
	public int def;
	public ArrayList<Integer> positions;
	
	public OneOfPosition(int def, ArrayList<Integer> positions)
	{
	this.def = def;
	this.positions = positions;
	}
	
	public String toString(int offset)
	{
	// one-of(4,7,10)
	String toRet = "one-of(";
	for (int pos : positions)
	{
		toRet += "" + (pos + offset) + ",";
	}
	toRet = Util.i(toRet,0,-1) + ")";
	return toRet;
	}
	
	public int getMinPos()
	{
	int toRet = Integer.MAX_VALUE;
	for (int num : positions)
		toRet = Math.min(toRet,num);
	return toRet;
	}
	
	public int getMaxPos()
	{
	int toRet = Integer.MIN_VALUE;
	for (int num : positions)
		toRet = Math.max(toRet,num);
	return toRet;
	}
	
}
