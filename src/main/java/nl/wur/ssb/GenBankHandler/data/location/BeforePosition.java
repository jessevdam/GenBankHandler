package nl.wur.ssb.GenBankHandler.data.location;

public class BeforePosition implements Position
{
	private int position;
	
	public BeforePosition(int position)
	{
		this.position = position;
	}
	
	public String toString(int offset)
	{
		return "<" + (this.position + offset);
	}
	
	public int getMinPos()
	{
		// should be 0
		return this.position;
	}
	
	public int getMaxPos()
	{
		return this.position;
	}
}
