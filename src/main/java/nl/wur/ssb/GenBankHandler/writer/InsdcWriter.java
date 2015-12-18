package nl.wur.ssb.GenBankHandler.writer;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;

import nl.wur.ssb.GenBankHandler.data.Record;
import nl.wur.ssb.GenBankHandler.data.ResidueType;
import nl.wur.ssb.GenBankHandler.parser.ParseException;
import nl.wur.ssb.GenBankHandler.util.Util;

public abstract class InsdcWriter
{
	protected Writer out;
	
	public InsdcWriter(OutputStream output)
	{
		out = new OutputStreamWriter(new BufferedOutputStream(output));
	}
	
	public InsdcWriter(Writer output)
	{
		out = output;
	}
	
	
	protected void write(String line) throws Exception
	{
		this.out.write(line);
  	this.out.flush();
	}
	
	public abstract void writeRecord(Record record) throws Exception;
	public abstract int getBaseIndent();
	public abstract int getLineLength();
	public abstract boolean getRePrintKey();
	
	protected static String rep(String rep,int times)
	{
		return String.format("%1$"+times+"s",rep);
	}
	
	protected void writeSingleLine(String tag,String text,boolean canBeNull) throws Exception
  {
    if(text == null)
    {
    	if(!canBeNull)
    		throw new ParseException("tag: " + tag + " can not be null");
    }
    else
      wrappedLine(tag,text,this.getBaseIndent()," ",true,false);
  }

	protected void writeMultiLine(String tag, String text,boolean canBeNull) throws Exception
  {
    if(text == null)
    {
    	if(!canBeNull)
    		throw new ParseException("tag: " + tag + " can not be null");
    }
    else
    	wrappedLine(tag,text,this.getBaseIndent()," ",false,false);
  }
	
	protected void wrappedLine(String tag,String information, int indent, String split_char,boolean singleLine,boolean spaceToNextLine) throws Exception
	{
    /*Write a line of GenBank info that can wrap over multiple lines.

    This takes a line of information which can potentially wrap over
    multiple lines, and breaks it up with carriage returns and
    indentation so it fits properly into a GenBank record.

    Arguments:

        - information - The string holding the information we want
          wrapped in GenBank method.

        - indent - The indentation on the lines we are writing.

        - wrap_space - Whether or not to wrap only on spaces in the
          information.

        - split_char - A specific character to split the lines on. By default
          spaces are used.
    */
    assert tag.length() <= indent : "tag: (" + tag + ") indent:" + indent;
    String header = String.format("%-" + indent + "s",tag);
    write(header);
    int info_length = this.getLineLength() - indent;

    if(information.equals(""))
    {
        // GenBank files use "." for missing data
        write(".\n");
        return;
    }

    // first get the information string split up by line
    ArrayList<String> output_parts = new ArrayList<String>();
    String cur_part = "";
    int count = 0;
    String parts[] = information.split(split_char);
    LinkedList<String> newParts = new LinkedList<String>();
    for(String info_part : parts)
    {
      while(info_part.length() > info_length)
      {
      	newParts.add(info_part.substring(0,info_length));
      	info_part = info_part.substring(info_length);
      }
      newParts.add(info_part);
    }
    for(String info_part : newParts)
    {
        if(cur_part.length() + 1 + (spaceToNextLine && parts.length - 1 != count ? 1 : 0) + info_part.length() > info_length)
        {
            if(!cur_part.equals(""))
            {
                cur_part += split_char;
                output_parts.add(Util.rtrim(cur_part));
            }
            cur_part = info_part;
        }
        else
        {
            if(cur_part.equals(""))
                cur_part = info_part;
            else
                cur_part += split_char + info_part;
        }
        count++;
    }

    // add the last bit of information to the output
    if(!cur_part.equals(""))
        output_parts.add(cur_part);

    if(singleLine && output_parts.size() > 1)
    	throw new ParseException("tag: " + tag + " should be one a single line : " + information);
    
    // now format the information string for return
    if(singleLine)
    {
    	write(information + "\n");
    }
    else
    {
      write(output_parts.get(0) + "\n");
      String indentStr = "";
      if(this.getRePrintKey())
    	  indentStr = header;
      else
    	  indentStr = rep(" ",indent);
      output_parts.remove(0);

      for(String output_part : output_parts)
      {
        write(indentStr + output_part + "\n");
      }
    }
	}	
}
