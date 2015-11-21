package nl.wur.ssb.GenBankHandler.data.location;

public interface Position
{
	public String toString(int offset); 
	
	public int getMinPos();
	
	public int getMaxPos();
}
