package nl.wur.ssb.GenBankHandler.data;

import java.util.Date;

public class DateVersion
{
	public Date date;
	public String comment;
	public DateVersion(Date date,String comment)
	{
		this.date = date;
		this.comment = comment;
	}
}
