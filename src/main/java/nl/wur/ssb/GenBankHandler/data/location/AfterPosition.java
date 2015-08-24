package nl.wur.ssb.GenBankHandler.data.location;



public class AfterPosition implements Position
{
	public int position;
	public AfterPosition(int position)
	{
		this.position = position;
	}
	public String toString(int offset)
	{
		return ">" + (this.position + offset);
	}
}
