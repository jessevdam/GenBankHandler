package nl.wur.ssb.GenBankHandler.writer;

import java.io.OutputStream;
import java.io.Writer;

import nl.wur.ssb.GenBankHandler.data.CrossRef;
import nl.wur.ssb.GenBankHandler.data.Feature;
import nl.wur.ssb.GenBankHandler.data.Qualifier;
import nl.wur.ssb.GenBankHandler.data.Record;
import nl.wur.ssb.GenBankHandler.data.Reference;
import nl.wur.ssb.GenBankHandler.data.ResidueType;
import nl.wur.ssb.GenBankHandler.data.location.Accession;
import nl.wur.ssb.GenBankHandler.parser.InsdcParser;
import nl.wur.ssb.GenBankHandler.util.Util;

import org.apache.commons.lang.StringUtils;

//\[(\d+):(\d+)\]     	.substring(\1,\2)
//strip 								trim
//assert 								assert(
//\\\n\s.*							 
//'											"
//\s*==\s*\"([^"]*)"		.equals("\1")
//and 									 && 
//or 									 || 

//"""([^"]*)"""																/*\1*/
//\s*def ([a-z0-9_]+)\([^\)]*\):(\s*)					\n  }\n\n  public void \1()\n  {  \2
//output \+?= ("[^"]*")\s*\%([^/\n]*)([/\n])	writeln(String.format(\1,\2));\3
//output \+?= ([^\n/]*)([\n/])								writeln(\1);\2
//("[^"]")\s*\*\s(\d+)												rep(\1,\2)

public class GenBankWriter extends InsdcWriter
{
	private static int MAX_WIDTH = 80;
	public int getLineLength()	{ return 79; }
	public int getBaseIndent() { return 12; }
  private static int GB_FEATURE_INDENT = 21;
	private static int GB_FEATURE_INTERNAL_INDENT = 5;
  private static int GB_SEQUENCE_INDENT = 9;
	
	private static String BASE_FORMAT = "%-12s";
	private static String SEQUENCE_FORMAT = "%" + (GB_SEQUENCE_INDENT) + "s";
	private static String FEATURE_KEY_INDENT = rep(" ",GB_FEATURE_INTERNAL_INDENT);
	
	public GenBankWriter(OutputStream output)
	{
		super(output);
	}
	
	public GenBankWriter(Writer output)
	{
		super(output);
	}
	
	public boolean getRePrintKey()
	{
		return false;
	}
	
  public String indentGenbank(String information, int indent)
  {
    /*Write out information with the specified indent.

    Unlike _wrapped_genbank, this function makes no attempt to wrap
    lines -- it assumes that the information already has newlines in the
    appropriate places, and will add the specified indent to the start of
    each line.
    */
    // split the info into lines based on line breaks
    String info_parts[] = information.split("\n");

    // the first line will have no indent
    String output_info = info_parts[0] + "\n";
    for(int i = 1;i < info_parts.length;i++)
    {
    	 String info_part = info_parts[i];    
       output_info += rep(" ",indent) + info_part + "\n";
    }

    return output_info;
  }

	public void writeRecord(Record record) throws Exception
	{
    /*Provide a GenBank formatted output option for a Record.

    The objective of this is to provide an easy way to read in a GenBank
    record, modify it somehow, and then output it in 'GenBank format.'
    We are striving to make this work so that a parsed Record that is
    output using this function will look exactly like the original
    record.

    Much of the output is based on format description info at:

    ftp://ncbi.nlm.nih.gov/genbank/gbrel.txt
    */
    _write_the_first_line(record);
    
  	this.writeMultiLine("DEFINITION",record.definition,false);
  	
    _accession_line(record);
    _version_line(record);
    //_project_line(record); DEPRECATED
    if(record.dblinks != null)
    	this.writeMultiLine("DBLINK",StringUtils.join(record.dblinks," ").replaceAll(":",": "),false);
    if(record.keywords != null)
  	  this.writeMultiLine("KEYWORDS",StringUtils.join(record.keywords,"; ") + ".",false);
  	this.writeSingleLine("SEGMENT",record.segment,true);
  	this.writeMultiLine("SOURCE",record.source,true);
    _organism_line(record);
    //DEPRECATED
    _nid_line(record);
    //DEPRECATED
    _pid_line(record);
    //DEPRECATED
    _db_source_line(record);       
    for(Reference reference : record.references)
    {
      _reference_line(reference);
      //TODO split by , or space ???
    	this.writeMultiLine("  AUTHORS",reference.authors,true);
    	this.writeMultiLine("  CONSRTM",reference.consrtm,true);
    	this.writeMultiLine("  TITLE",reference.title,true);
    	this.writeMultiLine("  JOURNAL",reference.journal,true);
    	for(CrossRef ref : reference.crossRefs)
    	{
    		if(ref.getDb().equals("MEDLINE"))
    	    this.writeMultiLine("  " + ref.getDb(),ref.getId(),false);
    		if(ref.getDb().equals("PUBMED"))
    	    this.writeMultiLine("   " + ref.getDb(),ref.getId(),false);
    	}
    	this.writeMultiLine("  REMARK",reference.remark,true);
    }
    if(record.comment != null)
    {
      String commentLines[] = record.comment.split("\\n");
  	  this.writeMultiLine("COMMENT",commentLines[0],false);
  	  for(int i = 1;i < commentLines.length;i++)
  	  	this.writeMultiLine("",commentLines[i],false);
    }
    if(record.features.size() > 0)
     	this.wrappedLine("FEATURES","Location/Qualifiers", GenBankWriter.GB_FEATURE_INDENT," ",false,false);
    for(Feature feature : record.features)
        this.writeFeature(record,feature);
    //DEPRECATED
    _base_count_line(record);
    _sequence_line(record);
    //DEPRECATED
    this.writeMultiLine("WGS",record.wgs,true);
    //DEPRECATED
    this.writeMultiLine("WGS_SCAFLD",record.wgsScaffold,true);
    write("//\n\n");
    this.out.flush();
  }
 
	
  public void  _write_the_first_line(Record record) throws Exception
  {  
    /*Provide the output string for the LOCUS line.
      LOCUS       NC_003479               1111 bp ss-DNA     circular VRL 10-FEB-2015
      012345678901234567890123456789012345678901234567890123456789012345678901234567890
      000000000011111111112222222222333333333344444444445555555555666666666677777777777
      00:05      LOCUS
      05:12      spaces
      12:??      Locus name
      ??:??      space
      ??:40      Length of sequence, right-justified
      40:44      space, bp, space
      44:47      Blank, ss-, ds-, ms-
      47:??      Blank, DNA, RNA, tRNA, mRNA, uRNA, snRNA, cDNA
      ??:??      space
      ??:63      Blank (implies linear), linear or circular
      63:64      space
      64:67      The division code (e.g. BCT, VRL, INV)
      67:68      space
      68:79      Date, in the form dd-MMM-yyyy (e.g., 15-MAR-1991)
    */
  	String residueType = " " + this.getResidueTypeText(record).toLowerCase() + " ";
    String strandType = record.strandType != null ? record.strandType : "";
    String strandType1 = "";
    String strandType2 = strandType;
    if(strandType.indexOf("-") != -1)
    {
    	String tmp[] = strandType.split("\\-",2);
    	strandType1 = tmp[0] + "-";
    	strandType2 = tmp[1];
    }
    String circular = "linear";
    if(record.circular)
    	circular = "circular";
    String date = record.dates.size() == 0 ? "" : InsdcParser.dateFormat.format(record.dates.get(record.dates.size() -1).date).toUpperCase();
  	write(String.format("%-12s%s%" + (28 - record.locus.length()) + "s%-4s%3s%-8s%-8s %s %s\n","LOCUS",record.locus,"" + record.size,residueType,strandType1,strandType2, circular,record.data_file_division,date));
  }

  public void _accession_line(Record record) throws Exception
  {  
    /*Output for the ACCESSION line.
    */
    //TODO what should it be 1 or multiple accession numbers
    String acc_info = "";
    for(Accession accession : record.accessions)
    {
        acc_info += accession.getId() + " ";
    }
    // strip off an extra space at the end
    acc_info = Util.rtrim(acc_info);
    this.writeMultiLine("ACCESSION",acc_info,false);
  }

  public void _version_line(Record record) throws Exception
  {  
    /*Output for the VERSION line.
    */
  	//TODO what should we do with version line
  	String accession = record.accessions.get(0).toString();
  	if(accession.equals("unknown"))
  		return;
  	String gi = "";
    if(record.gi != null)
      gi = "  GI:" + record.gi;
    if(record.accessions != null)
    	this.writeSingleLine("VERSION",record.accessions.get(0).toString() + gi,false);
  }

 /* public void _project_line(Record record)
  {  

    if len(record.projects) > 0:
        writeln(String.format(GenBankWriter.BASE_FORMAT,"PROJECT"));
        writeln(String.format("%s\n", "  ".join(record.projects)));
  }*/

  public void _nid_line(Record record) throws Exception
  {  
    /*Output for the NID line. Use of NID is obsolete in GenBank files.
    */
    if(record.nid != null)
    {
        write(String.format(GenBankWriter.BASE_FORMAT,"NID"));
        write(String.format("%s\n", record.nid));
    }
  }

  public void _pid_line(Record record) throws Exception
  {  
    /*Output for PID line. Presumedly, PID usage is also obsolete.
    */
    if(record.pid != null)
    {
        write(String.format(GenBankWriter.BASE_FORMAT,"PID"));
        write(String.format("%s\n", record.pid));
    }
  }

  public void _db_source_line(Record record) throws Exception
  {  
    /*Output for DBSOURCE line.
    */
    if(record.db_source != null)
    {
        write(String.format(GenBankWriter.BASE_FORMAT,"DBSOURCE"));
        write(String.format("%s\n", record.db_source));
    }
  }

  public void _organism_line(Record record) throws Exception
  {  
    /*Output for ORGANISM line with taxonomy info.
    */
    // Now that species names can be too long, this line can wrap (Bug 2591)
    String org = record.organism;
    if(org != null)
    {
      if(org.length() > MAX_WIDTH - getBaseIndent())
        org = Util.i(org,0,MAX_WIDTH - getBaseIndent() - 4) + "...";
      this.writeSingleLine("  ORGANISM",org,false);
      this.writeMultiLine(" ",StringUtils.join(record.taxonomy,"; ") + ".",false);
    }
  }

  public void _base_count_line(Record record) throws Exception
  {  
    /*Output for the BASE COUNT line with base information.
    */
    if(record.baseCount != null)
    {
        String toWrite = "";
        for(String base : record.baseCount.keySet())
        	toWrite += String.format("%7s %s", "" + record.baseCount.get(base), base);
        this.writeSingleLine("BASE COUNT",toWrite,false);
    }
  }
        
  public void _write_origin(Record record) throws Exception
  {
    if(record.originName != null)
    	this.writeSingleLine("ORIGIN",record.originName,false);
    else
    	write("ORIGIN      \n");
  }

	public void _sequence_line(Record record) throws Exception
	{
		/* Output for all of the sequence. */
		
		// Loosely based on code from Howard Salis
		
		if (record.sequence == null)
		{
			// We have already recorded the length, and there is no need
			// to record a long sequence of NNNNNNN...NNN or whatever.
			//TODO what should contig be like now its bogus
			if (record.contigLocation != null)
				this.wrappedLine("CONTIG",record.contigLocation,this.getBaseIndent(),",",false,false);
			else
				_write_origin(record);
		}
		else
		{
			_write_origin(record);
			int cur_seq_pos = 0;
			while (cur_seq_pos < record.sequence.length())
			{
				write(String.format(GenBankWriter.SEQUENCE_FORMAT,"" + (cur_seq_pos + 1)));
				
				for (int section = 0; section < 6; section++)
				{
					int start_pos = cur_seq_pos + section * 10;
					int end_pos = start_pos + 10;
					String seq_section = Util.i(record.sequence,start_pos,end_pos);
					write(String.format(" %s",seq_section.toLowerCase()));
					
					// stop looping if we are out of sequence
					if (end_pos > record.sequence.length())
						break;
				}
				
				write("\n");
				cur_seq_pos += 60;
			}
		}
	}
 
  public void _reference_line(Reference ref) throws Exception
  {  
    /*Output for REFERENCE lines. */
    String temp = "";
    if(ref.id != -1)
    {
        if(ref.locations.size() > 0)
        {
            temp = String.format("%-3s", ref.id);
            //(bases 1 to 105654; 110423 to 111122)
            temp += "(" + ref.locationRefType + " ";
            temp += StringUtils.join(ref.locations,"; ");
            temp = temp.trim() + ")";    
        }
        else
        	temp = "" + ref.id;
    }
    this.writeMultiLine("REFERENCE",temp,false);
  }
  
  public void writeFeature(Record record,Feature feature) throws Exception
  {
  	this.wrappedLine(FEATURE_KEY_INDENT + feature.key,feature.location.toString(record.size), GenBankWriter.GB_FEATURE_INDENT,",",false,false);
    for(Qualifier qualifier : feature.qualifiers)
      wrappedLine("","/" + qualifier.key + "=" + qualifier.value, GenBankWriter.GB_FEATURE_INDENT," ",false,true);
  }
	
}
