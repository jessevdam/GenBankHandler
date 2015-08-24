package nl.wur.ssb.GenBankHandler.data.location;



public class BeforePosition implements Position
{
	public int position;
	public BeforePosition(int position)
	{
		this.position = position;
	}
	public String toString(int offset)
	{
		return "<" + (this.position + offset);
	}
}
