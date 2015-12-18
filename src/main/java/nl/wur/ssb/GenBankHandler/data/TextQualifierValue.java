package nl.wur.ssb.GenBankHandler.data;

public class TextQualifierValue extends QualifierValue
{
	public TextQualifierValue(String val)
	{
		super(val);
		if(val.startsWith("\"") && val.endsWith("\""))
			throw new RuntimeException("Text qualifier should not be escaped with quotes");
	}
	public String toString()
	{
		return "\"" + this.getVal() + "\"";
	}
}
