package nl.wur.ssb.GenBankHandler.data;

import java.util.Date;

public class DateVersion
{
	private Date date;
	private String comment;
	public DateVersion(Date date,String comment)
	{
		this.date = date;
		this.comment = comment;
	}
	public Date getDate()
	{
		return date;
	}
	public String getComment()
	{
		return comment;
	}
}
