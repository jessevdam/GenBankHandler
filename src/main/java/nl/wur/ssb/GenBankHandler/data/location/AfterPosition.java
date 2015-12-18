package nl.wur.ssb.GenBankHandler.data.location;

public class AfterPosition implements Position
{
	private int position;
	
	public AfterPosition(int position)
	{
		this.position = position;
	}
	
	public String toString(int offset)
	{
		return ">" + (this.position + offset);
	}
	
	public int getMinPos()
	{
		return this.position;
	}
	
	public int getMaxPos()
	{
		// should be genome size
		return this.position;
	}
}
