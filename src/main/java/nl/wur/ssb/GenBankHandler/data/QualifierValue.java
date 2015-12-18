package nl.wur.ssb.GenBankHandler.data;

public class QualifierValue
{
	private String val;
	public QualifierValue(String val)
	{
		this.val = val;
	}
	public String getVal()
	{
		return val;
	}
	public String toString()
	{
		return this.getVal();
	}
}
