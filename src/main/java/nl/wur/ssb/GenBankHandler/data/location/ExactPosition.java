package nl.wur.ssb.GenBankHandler.data.location;



public class ExactPosition implements Position
{
	private int position;
	public ExactPosition(int position)
	{
		this.position = position;
	}
	public String toString(int offset)
	{
		return "" + (this.position + offset);
	}
	
	public int getMinPos()
	{
	return this.position;
	}
	
	public int getMaxPos()
	{
	return this.position;
	}
}
