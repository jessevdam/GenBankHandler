package nl.wur.ssb.GenBankHandler.data.location;



public interface Location
{
	public void addLocation(Location location) throws Exception;
	public void addPosition(Position position) throws Exception;
	public String toString(int sequenceSize);
	public int getBeginPosition();
	public int getEndPosition();
}
