package nl.wur.ssb.GenBankHandler.writer;

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;

import nl.wur.ssb.GenBankHandler.data.CrossRef;
import nl.wur.ssb.GenBankHandler.data.DateVersion;
import nl.wur.ssb.GenBankHandler.data.Feature;
import nl.wur.ssb.GenBankHandler.data.Qualifier;
import nl.wur.ssb.GenBankHandler.data.Record;
import nl.wur.ssb.GenBankHandler.data.Reference;
import nl.wur.ssb.GenBankHandler.data.location.Accession;
import nl.wur.ssb.GenBankHandler.data.location.RefLocation;
import nl.wur.ssb.GenBankHandler.parser.InsdcParser;
import nl.wur.ssb.GenBankHandler.util.Util;

import org.apache.commons.lang.StringUtils;

public class EmblWriter extends InsdcWriter {
	@Override
	public int getBaseIndent() {
		return 5;
	}

	@Override
	public int getLineLength() {
		return 80;
	};

	private static int QUALIFIER_INDENT = 21;
	private static int LETTERS_PER_BLOCK = 10;
	private static int BLOCKS_PER_LINE = 6;
	private static int LETTERS_PER_LINE = LETTERS_PER_BLOCK * BLOCKS_PER_LINE;
	private static int POSITION_PADDING = 10;

	public EmblWriter(OutputStream output) {
		super(output);
	}

	public EmblWriter(Writer output) {
		super(output);
	}

	public boolean getRePrintKey() {
		return true;
	}

	public void _write_the_first_line(Record record) throws Exception {
		/* Write the ID and AC lines. */
		Accession acc = record.accessions.get(0);
		String subVersion = "";
		if (acc.getVersion() != 0)
			subVersion = "SV " + acc.getVersion();


		String circular = "linear";
		if (record.isCircular())
			circular = "circular";

		// Get the taxonomy division
		String taxDivision = record.taxDivision != null ? record.taxDivision : "";
		// TODO find better name
		String dataclass = record.data_file_division;
		// ID <1>; SV <2>; <3>; <4>; <5>; <6>; <7> BP.
		// 1. Primary accession number
		// 2. Sequence version number
		// 3. Topology: 'circular' or 'linear'
		// 4. Molecule type
		// 5. Data class
		// 6. Taxonomic division
		// 7. Sequence length
		this.writeSingleLine("ID",String.format("%s; %s; %s; %s; %s; %s; %s %s.",
  	acc.getId(),subVersion,circular,record.getStrandType().toString(),taxDivision,dataclass,"" + record.getSize(),record.getResidueType().toString()),false);

		write("XX\n");
		String accession = "";
		for (Accession item : record.accessions) {
			accession += item.getId() + "; ";
		}
		accession = Util.i(accession, 0, -1);
		this.writeSingleLine("AC", accession, false);
	}

  public void printDates(Record record) throws Exception
  {  
    /*Print the date lines */
    for(DateVersion dateVersion : record.dates)
    {
    	String comment = "";
    	if(dateVersion.getComment() != null)
    		comment = " (" + dateVersion.getComment() + ")";
    	this.writeSingleLine("DT",InsdcParser.dateFormat.format(dateVersion.getDate()).toUpperCase() + comment,false);
    }
  }
  
  public void writeFeature(Record record,Feature feature) throws Exception
  {
  	this.wrappedLine("FT   " + feature.getKey(),feature.getLocationString(), QUALIFIER_INDENT,",",false,false);
    for(Qualifier qualifier : feature.getAllQualifiers())
    {
    	for(String val : qualifier.getValuesAsStrings())
        wrappedLine("FT","/" + qualifier.getKey() + "=" + val + "", QUALIFIER_INDENT," ",false,true);
    }
  }
  
	public void _sequence_line(Record record) throws Exception
	{
		/* Output for all of the sequence. */

		// Loosely based on code from Howard Salis
		if (record.getSequence() != null)
		{
			String line = "Sequence " + record.getSize() + " " + record.getResidueType().toString() + ";";
			if (record.baseCount != null) {
				for (String key : record.baseCount.keySet()) {
					String val = key;
					if (val.equalsIgnoreCase("other"))
						val = val.toLowerCase();
					else
						val = val.toUpperCase();
					line += " " + record.baseCount.get(key) + " " + val + ";";
				}
			}
			this.writeSingleLine("SQ",line,false);
			
	    for(int line_number = 0;line_number < (record.getSize() / LETTERS_PER_LINE);line_number++)
	    {
	        write("     "); //5
	        for(int block = 0;block < BLOCKS_PER_LINE;block++)
	        {
	            int index = LETTERS_PER_LINE * line_number + LETTERS_PER_BLOCK * block;
	            write(Util.i(record.getSequence(),index,index + LETTERS_PER_BLOCK) + " ");
	        }
	        write(String.format("%" + (POSITION_PADDING - 1) + "s\n","" + ((line_number + 1) * LETTERS_PER_LINE)));
	    }
	    if(record.getSize() % LETTERS_PER_LINE != 0)
	    {
	        // Final (partial) line
	        int line_number = (record.getSize() / LETTERS_PER_LINE);
	        write("     "); //5
	        for(int block = 0;block < BLOCKS_PER_LINE;block++)
	        {
	            int index = LETTERS_PER_LINE * line_number + LETTERS_PER_BLOCK * block;
	            write(String.format("%-11s",Util.i(record.getSequence(),index,index + LETTERS_PER_BLOCK)));
	        }
	        write(String.format("%" + (POSITION_PADDING - 1) + "s\n","" + record.getSize()));
	    }
		}

	}

	@Override
	public void writeRecord(Record record) throws Exception
	{
    _write_the_first_line(record);
    write("XX\n");
    ArrayList<CrossRef> dbLinks = new ArrayList<CrossRef>();
    if(record.dblinks != null)
      dbLinks.addAll(record.dblinks);
    if(record.pid != null)
      this.writeSingleLine("PR","Project:" + record.pid + ";",false);
    for(CrossRef link : dbLinks)
    {
    	if(link.getDb().equals("Project"))
    	{
    		this.writeSingleLine("PR","" + link + ";",false);
    		write("XX\n");
    		dbLinks.remove(link);
    		break;
    	}
    }
    //DT fields
  	printDates(record);
  	write("XX\n");
  	this.writeMultiLine("DE",record.definition,false);
  	write("XX\n");
  	this.writeMultiLine("KW",StringUtils.join(record.keywords,"; ") + ".",false);
  	write("XX\n");
    this.writeMultiLine("OS",record.organism,false);
    this.writeMultiLine("OC",StringUtils.join(record.taxonomy,"; ") + ".",false);	
    write("XX\n");
    this.writeMultiLine("OG",record.organelle,true);	
    if(record.organelle != null)
      write("XX\n");
    
    for(Reference reference : record.references)
    {
    	/*
     RN - reference number           (>=1 per entry)
     RC - reference comment          (>=0 per entry)
     RP - reference positions        (>=1 per entry)
     RX - reference cross-reference  (>=0 per entry)
     RG - reference group            (>=0 per entry)
     RA - reference author(s)        (>=0 per entry)
     RT - reference title            (>=1 per entry)
     RL - reference location         (>=1 per entry)
    	 */
      if(reference.id != -1)
       	this.writeSingleLine("RN","[" + reference.id + "]",false);
      this.writeMultiLine("RC",reference.remark,true);
    	for(RefLocation loc : reference.locations)
    		this.writeSingleLine("RP","" + (loc.getStart() + 1) + "-" + loc.getEnd(),false);    
    	for(CrossRef crossRef : reference.crossRefs)
    		this.writeSingleLine("RX","" + crossRef.getDb() + "; " + crossRef.getId() + ".",false);  
    	this.writeMultiLine("RG",reference.consrtm,true);
    	if(reference.authors != null)
    	  this.wrappedLine("RA",reference.authors + ";",this.getBaseIndent(),", ",false,false);
    	this.writeMultiLine("RT",(reference.title != null && !reference.title.equals("")) ? "\"" + reference.title + "\";" : ";",true);
    	if(reference.journal != null)
    	{
    	  for(String line : reference.journal.split("\\n"))
    	  	this.writeSingleLine("RL",line,false);
    	}
    	write("XX\n");
    }
  	
    if(record.dblinks != null)
    {
    	for(CrossRef crossRef : record.dblinks)
    		this.writeSingleLine("DR","" + crossRef.getDb() + "; " + crossRef.getId() + ".",false);  
    	write("XX\n");
    }
    if(record.comment != null)
    {
  	  for(String line : record.comment.split("\\n"))
  	  	this.writeSingleLine("CC",line,false);
  	  write("XX\n");
    }
    //AH + AS lines
    if(record.features.size() > 0)
    {
     	this.wrappedLine("FH   Key","Location/Qualifiers", QUALIFIER_INDENT," ",false,false);
     	write("FH\n");
      for(Feature feature : record.features)
        this.writeFeature(record,feature);
      write("XX\n");
    }    
    _sequence_line(record);
  	
  	this.writeSingleLine("SEGMENT",record.segment,true);
  	this.writeMultiLine("SOURCE",record.source,true);
  
    write("//\n\n");
    this.out.flush();
	}
}
