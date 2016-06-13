package nl.wur.ssb.GenBankHandler.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;

import nl.wur.ssb.GenBankHandler.data.ResidueType;
import nl.wur.ssb.GenBankHandler.data.StrandType;
import nl.wur.ssb.GenBankHandler.util.Util;

import org.apache.commons.lang.StringUtils;

public class EmblParser extends InsdcParser
{
	public EmblParser(InputStream in,ParserConsumer2 consumer)
	{
		super(in,consumer);
	}
	
	public String getRecordStart() { return "ID   ";} 
	public int getHeaderWidth() {return 5;}
	public Set<String> getFeatureStartMarkers() {return set("FH   Key             Location/Qualifiers", "FH");} 
	public Set<String> getSequenceHeaders() { return set("SQ", "CO");}
	public Set<String> getFeatureEndMarkers() { return set("XX");}
	public int getFeatureQualifierIndent() { return 21;}
	public String getFeatureQualifierSpacer() {return String.format("FT%1$19s"," "); }
  String EMBL_SPACER = String.format("%1$5s"," ");
  private boolean feededSeqLengthAndResidue = false;
	
	public void feedMiscLines(ArrayList<String> lines) throws Exception
  {
    lines.add("");
    Iterator<String> line_iter = lines.iterator();
    try
    {
    	  while(line_iter.hasNext())
    	  {
    	  	  String line = line_iter.next();
            if(line.startsWith("CO   "))
            {
                line = i(line,5).trim();
                String contig_location = line;
                while(true)
                {
                    line = line_iter.next();
                    if(line == null || line.equals(""))
                        break;
                    else if(line.startsWith("CO   "))
                    {
                        // Don't need to preseve the whitespace here.
                        contig_location += i(line,5).trim();
                    }
                    else
                        throw new ParseException("Expected CO (contig) continuation line, got:\n" + line);
                }
                //TODO not fully parsed
                consumer.contigLocation(contig_location);
            }
            if(line.startsWith("SQ   Sequence "))
            {
                // e.g.
                // SQ   Sequence 219 BP; 82 A; 48 C; 33 G; 45 T; 11 other;
                //
                // Or, EMBL-bank patent, e.g.
                // SQ   Sequence 465 AA; 3963407aa91d3a0d622fec679a4524e0; MD5;
            	  String tmp[] = i(line,14).trim().split(";");
                this.feedSeqLengthAndResidue(tmp[0]);
                

                LinkedHashMap<String,Integer> counts = new LinkedHashMap<String,Integer>();
                for(int i = 1;i < tmp.length;i++)
                {
                	String part = tmp[i].trim();
                	if(!part.equals(""))
                	{
                		String tmp2[] = part.split(" ");
                		assert tmp2.length == 2 : "invalid base count" + part;
                		counts.put(tmp2[1].toLowerCase(),Integer.parseInt(tmp2[0]));
                	}
                }
                this.consumer.baseCount(counts);
            }
    	  }
    }
    catch(NoSuchElementException e)
    {
      throw new ParseException("Problem in misc lines before sequence",e);
    }
  }

	
	public ArrayList<String> parseFooter() throws Exception
  {
    /*returns a tuple containing a list of any misc strings, and the sequence*/
    assert this.getSequenceHeaders().contains(rtrim(this.i(line,0,this.getHeaderWidth()))) : String.format("Eh? '%s'",this.line);

    // Note that the SQ line can be split into several lines...
    ArrayList<String> misc_lines = new ArrayList<String>();
    while(this.getSequenceHeaders().contains(rtrim(this.i(line,0,this.getHeaderWidth()))))
    {
        misc_lines.add(this.line);
        this.line = this.in.readLine();
        if(line == null || line.equals(""))
            throw new ParseException("Premature end of file");
        this.line = rtrim(this.line);
    }

    assert this.i(line,0,this.getHeaderWidth()).equals(EMBL_SPACER) || this.line.trim().equals("//") : String.format("Unexpected content after SQ or CO line: %r",this.line);

    StringBuffer seq_lines = new StringBuffer();
    while(true)
    {
        if(line == null)
            throw new ParseException("Premature end of file in sequence data");
        String line = this.line.trim();
        if(line.equals(""))
        	 throw new ParseException("Blank line in sequence data");
        if(line.equals("//"))
            break;
        assert this.i(this.line,0,this.getHeaderWidth()).equals(EMBL_SPACER) : this.i(line,0,Math.min(line.length() - 1,1024));
        // Remove tailing number now, remove spaces later
        seq_lines.append(rsplit(line,"[\\s\\t\\n\\r\\f]", 1)[0].replaceAll(" ",""));
        this.line = this.in.readLine();
    }
    consumer.sequence(seq_lines.toString());
    return misc_lines;
  }
	
	public void parseHeaderLines(ArrayList<String> lines)  throws Exception
	{
    HashMap<String,String> consumer_dict = new HashMap<String,String>();
    consumer_dict.put("SV", "version");  // SV line removed in June 2006, now part of ID line
    consumer_dict.put("DE", "definition");
    consumer_dict.put("RG", "consrtm");  // optional consortium
    consumer_dict.put("RL", "journal");
    consumer_dict.put("OS", "organism");
    consumer_dict.put("CC", "comment");
    consumer_dict.put("RC" , "remark");
    consumer_dict.put("OG" , "organelle");
    // "XX" : splitter
    //TODO
    //  consumer_dict.put( "OG" , organelle);
    //  consumer_dict.put( "AH" , assembly header );
    //  consumer_dict.put( "AS" , assembly information);
    // We have to handle the following specially:
    // RX (depending on reference type...)
    for(int i = 0;i < lines.size();)
    {
    	  String line = lines.get(i++);
        String line_type = i(line,0,this.getHeaderWidth()).trim();
        String data = i(line,this.getHeaderWidth()).trim();
        if(!set("ID","AC","PR","DT","RX","DR").contains(line_type))
        {
          while(i < lines.size() && i(lines.get(i),0,this.getHeaderWidth()).trim().equals(line_type))
          {
          	line = lines.get(i++);
        	  if(set("CC","RL").contains(line_type))
        	  	data += "\n" + i(line,this.getHeaderWidth()).trim();
        	  else
        	  	data += " " + i(line,this.getHeaderWidth()).trim();
          }
        }
        if(line_type.equals("XX"))
            continue;
        else if(line_type.equals("RN"))
        {
            // Reformat reference numbers for the GenBank based consumer
            // e.g. "[1]" becomes "1"
            if(data.startsWith("[") && data.endsWith("]"))
                data = i(data,1,-1);
            this.feedReferceNum(data);
        }
        else if(line_type.equals("AC"))
        {
        	this.feedAccession2(data,0);
        }
        else if(line_type.equals("OC"))
        {
        	this.feedTaxonomy(data);
        }
        else if(line_type.equals("RP"))
        {
            // Reformat reference numbers for the GenBank based consumer
            // e.g. "1-4639675" becomes "(bases 1 to 4639675)"
            // and "160-550, 904-1055" becomes "(bases 160 to 550; 904 to 1055)"
            // Note could be multi-line, and end with a comma
          	//parts = [bases.replace("-", " to ").strip() for bases in data.split(",") if bases.strip()]
        	  ArrayList<String> parts = new ArrayList<String>();
        	  for(String bases : data.split(","))
        	  {
        	  	bases = bases.trim();
        	  	if(bases.equals(""))
        	  		continue;
        	  	parts.add(bases.replaceAll("-", " to ").trim()); 
        	  }           		 
            this.feedReferenceBases(String.format("(bases %s)",StringUtils.join(parts,"; ")));
        }
        else if(line_type.equals("RT"))
        {
            // Remove the enclosing quotes and trailing semi colon.
            // Note the title can be split over multiple lines.
            if(data.startsWith("\""))
                data = i(data,1);
            if(data.endsWith(";"))
              data = i(data,0,-1);
            if(data.endsWith("\""))
                data = i(data,0,-1);
            if(data.equals(""))
            	data = null;
            consumer.title(data);
        }
        else if(line_type.equals("RX"))
        {
            // EMBL support three reference types at the moment:
            // - PUBMED    PUBMED bibliographic database (NLM)
            // - DOI       Digital Object Identifier (International DOI Foundation)
            // - AGRICOLA  US National Agriculture Library (NAL) of the US Department
            //             of Agriculture (USDA)
            //
            // Format:
            // RX  resource_identifier; identifier.
            //
            // e.g.
            // RX   DOI; 10.1016/0024-3205(83)90010-3.
            // RX   PUBMED; 264242.
            //
            // Currently our reference object only supports PUBMED and MEDLINE
            // (as these were in GenBank files?).
        	  String tmp[] = data.split(";", 2);
            String key = tmp[0];
            String value = tmp[1];
            if(value.endsWith("."))
                value = i(value,0,-1);
            value = value.trim();
            consumer.crossRef(key,value);
        }
        else if(line_type.equals("CC"))
        {
           consumer.comment(data);
        }
        else if(line_type.equalsIgnoreCase("DR"))
        {
            // Database Cross-reference, format:
            // DR   database_identifier; primary_identifier; secondary_identifier.
            //
            // e.g.
            // DR   MGI; 98599; Tcrb-V4.
            //
            // TODO - How should we store any secondary identifier?
            String parts[] = rtrim(data,'.').split(";");
            // Turn it into "database_identifier:primary_identifier" to
            // mimic the GenBank parser. e.g. "MGI:98599"
            this.feedDblink(String.format("%s:%s",parts[0].trim(),parts[1].trim()));
        }
        else if(line_type.equals("RA"))
        {
            // Remove trailing ; at end of authors list
            consumer.authors(rtrim(data,';'));
        }
        else if(line_type.equals("PR"))
        {
            // Remove trailing ; at end of the project reference
            // In GenBank files this corresponds to the old PROJECT
            // line which is being replaced with the DBLINK line.
            this.feedProject(rtrim(data,';'));
        }
        else if(line_type.equals("KW"))
        {
            this.feedKeywords(data);
        }
        else if(line_type.equals("DT"))
        {

        	  String tmp[] = data.split(" ",2);
        	  Date date = dateFormat.parse(tmp[0]);
        	  String comment = null;
        	  if(tmp.length == 2)
        	  	comment = Util.i(tmp[1].trim(),1,-1);
        	  //TODO further parse comment
        	  this.consumer.date(date,comment);
        }
        else if(consumer_dict.containsKey(line_type))
        {
            // Its a semi-automatic entry!
            //getattr(consumer, consumer_dict[line_type])(data)
        	  consumer.getClass().getMethod(consumer_dict.get(line_type),String.class).invoke(consumer,data);
        }
        else
        {
            logger.debug(String.format("Ignoring EMBL header line:\n%s",line));
        }
    }
	}
	
	public void parseFirstLine() throws Exception
	{
    assert rtrim(i(line,0,this.getHeaderWidth())).equals("ID");
    if(StringUtils.countMatches(i(line,this.getHeaderWidth()),";") == 6)
    {
        // Looks like the semi colon separated style introduced in 2006
        this.feedFirstLineNew();
    }
    else if(StringUtils.countMatches(i(line,this.getHeaderWidth()),";") == 3)
    {
        if(rtrim(line).endsWith(" SQ"))
        {
            //EMBL-bank patent data
            this.feedFirstLinePatents();
        }
        else
        {
            //Looks like the pre 2006 style
            this.feedFirstLineOld();
        }
    }
    else
        throw new ParseException("Did not recognise the ID line layout:\n" + line);
	}
	
  public void feedFirstLineOld() throws Exception
  {
    // Expects an ID line in the style before 2006, e.g.
    // ID   SC10H5 standard; DNA; PRO; 4870 BP.
    // ID   BSUB9999   standard; circular DNA; PRO; 4214630 BP.
  	String header = rtrim(i(line,0,this.getHeaderWidth()));
    assert header.equals("ID");
    String fields[] = i(line,this.getHeaderWidth()).split(";");
    /*
    The tokens represent:

       0. Primary accession number
       (space sep)
       1. ??? (e.g. standard)
       (semi-colon)
       2. Topology and/or Molecule type (e.g. "circular DNA" or "DNA")
       3. Taxonomic division (e.g. "PRO")
       4. Sequence length (e.g. "4639675 BP.")
    */
    consumer.locus(fields[0].split("\\s+")[0]);  // Should we also call the accession consumer?
    this.feedSeqLengthAndResidue(fields[3].trim());
    String tmp = fields[1];
    if(tmp.indexOf("circular") != -1)
    	consumer.circular();
    tmp = tmp.replaceAll("circular","").replaceAll("linear","").trim();
    consumer.strandType(StrandType.fromStringChecked(tmp));
    consumer.data_file_division(fields[2]);
  }
  
  public void feedFirstLinePatents() throws Exception
  {
    // Either Non-Redundant Level 1 database records,
    // ID <accession>; <molecule type>; <non-redundant level 1>; <cluster size L1>
    // e.g. ID   NRP_AX000635; PRT; NR1; 15 SQ
    //
    // Or, Non-Redundant Level 2 database records:
    // ID <L2-accession>; <molecule type>; <non-redundant level 2>; <cluster size L2>
    // e.g. ID   NRP0000016E; PRT; NR2; 5 SQ
    String fields[] = rtrim(i(line,this.getHeaderWidth())).substring(0,-3).split(";");
    assert fields.length == 4;
    consumer.locus(fields[0]);
    if(fields[1].trim().equals("PRT"))
      consumer.residueType(ResidueType.PROTEIN);
    else
    	consumer.residueType(ResidueType.DNA);
    consumer.data_file_division(fields[2]);
    this.bogus("EMBL patent header format detected");
    // TODO - Record cluster size?
  }
	
  public void feedFirstLineNew() throws Exception
  {
    // Expects an ID line in the style introduced in 2006, e.g.
    // ID   X56734; SV 1; linear; mRNA; STD; PLN; 1859 BP.
    // ID   CD789012; SV 4; linear; genomic DNA; HTG; MAM; 500 BP.
    assert rtrim(i(line,0,this.getHeaderWidth())).equals("ID");
    ArrayList<String> fields = new ArrayList<String>();
    for(String data : i(line,this.getHeaderWidth()).trim().split(";"))
        fields.add(data.trim());
    assert fields.size() == 7;
    /*
    The tokens represent:

       0. Primary accession number
       1. Sequence version number
       2. Topology: 'circular' or 'linear'
       3. Molecule type (e.g. 'genomic DNA')
       4. Data class (e.g. 'STD')
       5. Taxonomic division (e.g. 'PRO')
       6. Sequence length (e.g. '4639675 BP.')
    */

    consumer.locus(fields.get(0));

    this.feedSeqLengthAndResidue(fields.get(6));

    // Based on how the old GenBank parser worked, merge these two:
    String circular = fields.get(2).trim();
    assert(set("", "linear", "circular").contains(circular)) : "LOCUS line does not contain valid entry (linear, circular, ...):\n" + line;
    if(circular.equals("circular"))
      consumer.circular();

    consumer.strandType(StrandType.fromStringChecked(fields.get(3).trim()));
    consumer.taxDivision(fields.get(5)); 
    consumer.data_file_division(fields.get(4));
       

    // TODO - How to deal with the version field?  At the moment the consumer
    // will try and use this for the ID which isn't ideal for EMBL files.
    int suffixVersion = 0;
    String version_parts[] = fields.get(1).split("[\\s\\t\\n\\r\\f]+");
    if(version_parts.length == 2 && version_parts[0].equals("SV") && version_parts[1].matches("\\d+"))
    	suffixVersion = Integer.parseInt(version_parts[1]);
    
    // Call the accession consumer now, to make sure we record
    // something as the record.id, in case there is no AC line
    this.feedAccession2(fields.get(0),suffixVersion);
  }

	public void feedSeqLengthAndResidue(String text)  throws Exception
	{
		if(feededSeqLengthAndResidue)
			return;
		feededSeqLengthAndResidue = true;
    String length_parts[] = text.split("[\\s\\t\\n\\r\\f]");
    assert length_parts.length == 2 : "Invalid sequence length string " + text;    
    String residueType = length_parts[1].replaceAll("\\.","").toLowerCase();
    this.feedResidueType(residueType);
    this.feedSize(Integer.parseInt(length_parts[0]));
	}
	
	public void reset()
	{
		super.reset();
		feededSeqLengthAndResidue = false;
	}
}
