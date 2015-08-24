package nl.wur.ssb.GenBankHandler.parser;

public class ParseException extends Exception
{
	private static final long serialVersionUID = 8144973898537394170L;

	public ParseException(String message)
	{
		super(message);
	}
	public ParseException(String message,Throwable cause)
	{
		super(message,cause);
	}
}
