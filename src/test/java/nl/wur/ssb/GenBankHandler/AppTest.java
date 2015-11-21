package nl.wur.ssb.GenBankHandler;

import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.TestCase;
import nl.wur.ssb.GenBankHandler.data.RecordBuilder;
import nl.wur.ssb.GenBankHandler.parser.EmblParser;
import nl.wur.ssb.GenBankHandler.parser.GenBankParser;
import nl.wur.ssb.GenBankHandler.parser.InsdcParser;
import nl.wur.ssb.GenBankHandler.util.ParserConsumer2Stream;
import nl.wur.ssb.GenBankHandler.writer.EmblWriter;
import nl.wur.ssb.GenBankHandler.writer.GenBankWriter;
import nl.wur.ssb.GenBankHandler.writer.InsdcWriter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;

public class AppTest extends TestCase
{
    public AppTest( String testName )
    {
        super( testName );
        BasicConfigurator.configure();
    }
  
    private void runParseTest(String file,boolean isGenbank,boolean allowBogus) throws Exception
    {
 		  InputStream in = this.getClass().getResourceAsStream(file);
 		  StringWriter buf = new StringWriter();
 		  InsdcParser parser = null;
 		  if(isGenbank)
 		  	parser = new GenBankParser(in,new ParserConsumer2Stream(buf));
 		  else
 		    parser = new EmblParser(in,new ParserConsumer2Stream(buf));
 		  parser.parse(allowBogus);
 		  //System.out.println(buf);
    }
    
    public void testParsing() throws Exception {
 		  //logger.info("process");
    	this.runParseTest("/embl1.gbk",false,false);
    	this.runParseTest("/embl4.gbk",false,false);
    	this.runParseTest("/genbank1.gbk",true,true);
    	this.runParseTest("/genbank2.gbk",true,true);
    	this.runParseTest("/genbank3.gbk",true,false);
    	this.runParseTest("/genbank4.gbk",true,false);
    	this.runParseTest("/rast.gbk",true,false);
 	  }
   	private void compareResToOrig(String file,String orig,String newRes)
   	{
   		int line = 0;
   		int col = 0;
   		String sourceLastLine = "";
   		for(int i = 0;i < orig.length();i++)
   		{
   			assert i < newRes.length() : "file :" + file + " (" + line + ":" + col + ") " + sourceLastLine + "..."; 
   			char origChar = orig.charAt(i);
   			char newChar = newRes.charAt(i);
   			assert origChar == newChar : "file :" + file + " (" + line + ":" + col + ") " + sourceLastLine + "...";
   			sourceLastLine += new String(new char[] {origChar});
   			col++;
   			if(origChar == '\n')
   			{
   				sourceLastLine = "";
   				line++;
   				col = 0;
   			}
   		}
   	}
   
    private void runParseWriteTest(String file,boolean isGenbank,boolean allowBogus) throws Exception {
  		InputStream in = this.getClass().getResourceAsStream(file);
  		StringWriter buf = new StringWriter();
  		RecordBuilder builder = new RecordBuilder();
 		  InsdcParser parser = null;
 		  if(isGenbank)
 		  	parser = new GenBankParser(in,builder);
 		  else
 		    parser = new EmblParser(in,builder);
  		parser.parse(true);
  		InsdcWriter writer = null;
 		  if(isGenbank)
 		  	writer = new GenBankWriter(buf);
 		  else
 		  	writer = new EmblWriter(buf);
  		
  		//System.out.println("RECORDS = " + builder.getRecords().size());
  		writer.writeRecord(builder.getRecords().get(0));
  		in.close();
  		in = this.getClass().getResourceAsStream(file);
  		String orig = IOUtils.toString(in); 
  		//System.out.println(buf);
  		compareResToOrig(file,orig,buf.toString());
  	}
    
    private void runParseWriteCrossTest(String file,boolean isGenbank,boolean allowBogus) throws Exception {
  		InputStream in = this.getClass().getResourceAsStream(file);
  		StringWriter buf = new StringWriter();
  		RecordBuilder builder = new RecordBuilder();
 		  InsdcParser parser = null;
 		  if(isGenbank)
 		  	parser = new GenBankParser(in,builder);
 		  else
 		    parser = new EmblParser(in,builder);
  		parser.parse(true);
  		InsdcWriter writer = null;
 		  if(!isGenbank)
 		  	writer = new GenBankWriter(buf);
 		  else
 		  	writer = new EmblWriter(buf);
  		
  		//System.out.println("RECORDS = " + builder.getRecords().size());
  		writer.writeRecord(builder.getRecords().get(0));
  		in.close();
  	}
   	
    public void testParseWriting() throws Exception {
 		  //logger.info("process");
    	this.runParseWriteTest("/embl1.gbk",false,false);
    	this.runParseWriteTest("/embl4.gbk",false,false);
    	this.runParseWriteTest("/genbank3.gbk",true,false);
    	this.runParseWriteTest("/genbank4.gbk",true,false);
    	this.runParseWriteTest("/rast.gbk",true,false);
 	  }
    
    public void testCrossWriting() throws Exception {
 		  //logger.info("process");
    	this.runParseWriteCrossTest("/embl1.gbk",false,false);
    	this.runParseWriteCrossTest("/embl4.gbk",false,false);
    	//Journal line can not be translated to EMBL in this case
    	//this.runParseWriteCrossTest("/genbank3.gbk",true,false);
    	//his.runParseWriteCrossTest("/genbank4.gbk",true,false);
    	//OC is missing in this case, rest is ok
    	//this.runParseWriteCrossTest("/rast.gbk",true,false);
 	  }

}

