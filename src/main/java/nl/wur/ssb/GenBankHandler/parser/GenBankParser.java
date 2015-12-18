package nl.wur.ssb.GenBankHandler.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.wur.ssb.GenBankHandler.data.StrandMultiplicity;
import nl.wur.ssb.GenBankHandler.data.StrandType;
import nl.wur.ssb.GenBankHandler.util.Util;

import org.apache.commons.lang.StringUtils;


//http://www.ncbi.nlm.nih.gov/Sitemap/samplerecord.html
public class GenBankParser extends InsdcParser
{
	public GenBankParser(InputStream in,ParserConsumer2 consumer)
	{
		super(in,consumer);
	}
	
	public String getRecordStart() { return "LOCUS       ";} 
	public int getHeaderWidth() {return 12;}
	public Set<String> getFeatureStartMarkers() {return set("FEATURES             Location/Qualifiers", "FEATURES");} 
	public Set<String> getSequenceHeaders() { return set("CONTIG", "ORIGIN", "BASE COUNT", "WGS");}// trailing spaces removed
	public Set<String> getFeatureEndMarkers() { return set();}
	public int getFeatureQualifierIndent() { return 21;}
	public String getFeatureQualifierSpacer() {return String.format("%1$21s"," "); }
	private static String GENBANK_SPACER = String.format("%1$12s"," ");
 	
	public void parseBaseCount(String baseCounts) throws Exception
	{
    Matcher matcher = Pattern.compile("\\s*(\\d+)\\s*([a-zA-Z])").matcher(baseCounts);
    LinkedHashMap<String,Integer> counts = new LinkedHashMap<String,Integer>();
    String last = null;
    while(matcher.find())
    {
    	counts.put(matcher.group(2).toLowerCase(),Integer.parseInt(matcher.group(1)));
    	last = matcher.group(0);
    }
    if(last == null || !baseCounts.trim().endsWith(last))
    	throw new ParseException("Could not parse base counts: " + baseCounts);
    this.consumer.baseCount(counts);
	}

  public void feedVersion(String version)  throws Exception
  {
  	String tmp[] = version.split("\\.");
  	assert StringUtils.countMatches(version,".") == 1;
    assert tmp[1].matches("\\d+");
   	feedAccession2(tmp[0],Integer.parseInt(tmp[1]));
  }
  
  public void feedSource(String source)  throws Exception
  {
    //Note that some software (e.g. VectorNTI) may produce an empty
    //source (rather than using a dot/period as might be expected)
  	if(source.endsWith("."))
  		source = Util.i(source,0,-1);
  	this.consumer.source(source);
  }
	
  public void feedMiscLines(ArrayList<String> lines) throws Exception
  {
    // Deals with a few misc lines between the features and the sequence
    lines.add("");
    Iterator<String> line_iter = lines.iterator();
    try
    {
        while(line_iter.hasNext())
        {
        	  String line = line_iter.next();
            if(line.startsWith("BASE COUNT"))
            {
                line = i(line,10).trim();
                if(!line.equals(""))
                {
                    logger.debug("base_count = " + line);
                    parseBaseCount(line);
                }
            }
            if(line.startsWith("ORIGIN"))
            {
                line = i(line,6).trim();
                if(!line.equals(""))
                {
                    logger.debug("origin_name = " + line);
                    consumer.originName(line);
                }
            }
            if(line.startsWith("WGS "))
            {
                line = i(line,3).trim();
                consumer.wgs(line);
            }
            if(line.startsWith("WGS_SCAFLD"))
            {
                line = i(line,10).trim();
                consumer.addWgsScaffold(line);
            }
            if(line.startsWith("CONTIG"))
            {
                line = i(line,6).trim();
                String contig_location = line;
                while(true)
                {
                    line = line_iter.next();
                    if(line == null || line.equals(""))
                        break;
                    else if(i(line,0,this.getHeaderWidth()).equals(GENBANK_SPACER))
                        // Don't need to preseve the whitespace here.
                        contig_location += rtrim(i(line,this.getHeaderWidth()));
                    else if(line.startsWith("ORIGIN"))
                    {
                        // Strange, seen this in GenPept files via Entrez gbwithparts
                        line = i(line,6).trim();
                        if(!line.equals(""))
                            consumer.originName(line);
                        break;
                    }
                    else
                        throw new ParseException("Expected CONTIG continuation line, got:\n" + line);
                }
                consumer.contigLocation(contig_location);
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
    //returns a tuple containing a list of any misc strings, and the sequence
    assert this.getSequenceHeaders().contains(rtrim(this.i(line,0,this.getHeaderWidth()))): String.format("Eh? '%s'",this.line);

    ArrayList<String> misc_lines = new ArrayList<String>();
    while(getSequenceHeaders().contains(rtrim(this.i(line,0,this.getHeaderWidth()))) 
    		|| this.i(line,0,this.getHeaderWidth()).equals(GENBANK_SPACER) 
    		|| "WGS".equals(this.i(line,0,3)))
    {
        misc_lines.add(rtrim(this.line));
        this.line = this.in.readLine();
        if(this.line == null || this.line.equals(""))
            throw new ParseException("Premature end of file");
    }
    assert !this.getSequenceHeaders().contains(rtrim(this.i(line,0,this.getHeaderWidth()))) : String.format("Eh? '%s'",this.line);

    // Now just consume the sequence lines until reach the // marker
    // or a CONTIG line
    StringBuffer seq_lines = new StringBuffer();
    while(true)
    {
    	  if(this.line == null || this.line.equals(""))
    	  {
            logger.warn("Premature end of file in sequence data");
            line = "//";
            break;
    	  }
        line = rtrim(line);
        if(this.line.equals(""))
        {
            logger.warn("Blank line in sequence data");
            line = this.in.readLine();
            continue;
        }
        if(line.equals("//"))
            break;
        if(line.startsWith("CONTIG"))
            break;
        if(line.length() > 9 && !i(line,9,10).equals(" "))
        {
            // Some broken programs indent the sequence by one space too many
            // so try to get rid of that and test again.
            logger.warn("Invalid indentation for sequence line");
            line = i(line,1);
            if(line.length() > 9 && !i(line,9,10).equals(" "))
                throw new ParseException(String.format("Sequence line mal-formed, '%s'",line));
        }
        seq_lines.append(i(line,10).replace(" ", ""));//  # remove spaces later
        line = this.in.readLine();
    }
    // Seq("".join(seq_lines), this.alphabet)
    consumer.sequence(seq_lines.toString());
    return misc_lines;
	}
		
	public void parseHeaderLines(ArrayList<String> lines) throws Exception
	{
    // Following dictionary maps GenBank lines to the associated
    // consumer methods - the special cases like LOCUS where one
    // genbank line triggers several consumer calls have to be
    // handled individually.
    HashMap<String,String> consumer_dict = new HashMap<String,String>();
    consumer_dict.put("DEFINITION", "definition");
    consumer_dict.put("NID", "nid");
    consumer_dict.put("PID", "pid");
    consumer_dict.put("DBSOURCE", "db_source");
    consumer_dict.put("SEGMENT", "segment");
    consumer_dict.put("AUTHORS", "authors");
    consumer_dict.put("CONSRTM", "consrtm");
    consumer_dict.put("TITLE", "title");
    consumer_dict.put("JOURNAL", "journal");
    consumer_dict.put("REMARK", "remark");
    // We have to handle the following specially:
    // ORIGIN (locus, size, residue_type, data_file_division and date)
    // COMMENT (comment)
    // VERSION (version and gi)
    // REFERENCE (eference_num and reference_bases)
    // ORGANISM (organism and taxonomy)
    //lines = [_f for _f in lines if _f] //moved code to parserHeader empty lines are not added
    lines.add("");  // helps avoid getting StopIteration all the time
    Iterator<String> line_iter = lines.iterator();
    try
    {
        String line = line_iter.next();
        while(true)
        {
            if(line.equals(""))
                break;
            String line_type = i(line,0,this.getHeaderWidth()).trim();
            String data = i(line,this.getHeaderWidth()).trim();

            if(line_type.equals("VERSION"))
            {
                // Need to call consumer.version(), and maybe also consumer.gi() as well.
                // e.g.
                // VERSION     AC007323.5  GI:6587720
                while(data.indexOf("  ") != -1)
                    data = data.replace("  ", " ");
                if(data.indexOf(" GI:") == -1)
                    this.feedVersion(data);
                else
                {
                    logger.debug("Version [" + data.split(" GI:")[0] + "], gi [" + data.split(" GI:")[1] + "]");
                    this.feedVersion(data.split(" GI:")[0]);
                    consumer.gi(data.split(" GI:")[1]);   
                }
                // Read in the next line!
                line = line_iter.next();
            }
            else if(line_type.equals("ACCESSION"))
            {
            	feedAccession2(data,0);
            	line = line_iter.next();
            }
            else if(line_type.equals("PROJECT"))
            {
            	this.feedProject(data);
            	line = line_iter.next();
            }
            else if(line_type.equals("DBLINK"))
            {
            	this.feedDblink(data);
            	line = line_iter.next();
            }
            else if(line_type.equals("KEYWORDS"))
            {
            	this.feedKeywords(data);
            	line = line_iter.next();
            }
            else if(line_type.equals("SOURCE"))
            {
            	this.feedSource(data);
            	line = line_iter.next();
            }
            else if(line_type.equals("MEDLINE") || line_type.equals("PUBMED"))
            {
            	this.consumer.crossRef(line_type,data);
            	line = line_iter.next();
            }
            else if(line_type.equals("REFERENCE"))
            {
                logger.debug("Found reference [" + data + "]");
                // Need to call consumer.reference_num() and consumer.reference_bases()
                // e.g.
                // REFERENCE   1  (bases 1 to 86436)
                //
                // Note that this can be multiline, see Bug 1968, e.g.
                //
                // REFERENCE   42 (bases 1517 to 1696; 3932 to 4112; 17880 to 17975; 21142 to
                //             28259)
                //
                // For such cases we will call the consumer once only.
                data = data.trim();

                // Read in the next line, and see if its more of the reference:
                while(true)
                {
                    line = line_iter.next();
                    if(i(line,0,this.getHeaderWidth()).equals(GENBANK_SPACER))
                    {
                        // Add this continuation to the data string
                        data += " " + i(line,this.getHeaderWidth());
                        logger.debug("Extended reference text [" + data + "]");
                    }
                    else
                        // End of the reference, leave this text in the variable "line"
                        break;
                }

                // We now have all the reference line(s) stored in a string, data,
                // which we pass to the consumer
                while(data.indexOf("  ") != -1)
                    data = data.replace("  ", " ");
                if(data.indexOf(" ") == -1)
                {
                    logger.debug("Reference number \"" + data + "\"");
                    this.feedReferceNum(data);
                }
                else
                {
                    logger.debug("Reference number \"" + i(data,0,data.indexOf(" ")) + "\", \"" + i(data,data.indexOf(" ") + 1) + "\"");
                    this.feedReferceNum(i(data,0,data.indexOf(" ")));
                    this.feedReferenceBases(i(data,data.indexOf(" ") + 1));
                }
            }
            else if(line_type.equals("ORGANISM"))
            {
                // Typically the first line is the organism, and subsequent lines
                // are the taxonomy lineage.  However, given longer and longer
                // species names (as more and more strains and sub strains get
                // sequenced) the oragnism name can now get wrapped onto multiple
                // lines.  The NCBI say we have to recognise the lineage line by
                // the presence of semi-colon delimited entries.  In the long term,
                // they are considering adding a new keyword (e.g. LINEAGE).
                // See Bug 2591 for details.
                String organism_data = data;
                String lineage_data = "";
                while(true)
                {
                    line = line_iter.next();
                    if(i(line,0,this.getHeaderWidth()).equals(GENBANK_SPACER))
                    {
                        if(lineage_data != null || line.indexOf(";") != -1)
                            lineage_data += " " + i(line,this.getHeaderWidth());
                        else
                            organism_data += " " + i(line,this.getHeaderWidth()).trim();
                    }
                    else
                        // End of organism and taxonomy
                        break;
                }
                consumer.organism(organism_data);
                if(lineage_data.trim().equals(""))
                    logger.debug("Taxonomy line(s) missing or blank");
                this.feedTaxonomy(lineage_data.trim());
            }
            else if(line_type.equals("COMMENT"))
            {
                logger.debug("Found comment");
                // This can be multiline, and should call consumer.comment() once
                // with a list where each entry is a line.
                StringBuffer comment = new StringBuffer();
                comment.append(data);
                while(true)
                {
                    line = line_iter.next();
                    if(i(line,0,this.getHeaderWidth()).equals(GENBANK_SPACER))
                    {
                        data = i(line,this.getHeaderWidth());
                        comment.append("\n");
                        comment.append(data);
                        logger.info("Comment continuation [" + data + "]");
                    }
                    else
                        // End of the comment
                        break;
                }
                consumer.comment(comment.toString());
            }
            else if(consumer_dict.containsKey(line_type))
            {
                // Its a semi-automatic entry!
                // Now, this may be a multi line entry...
                while(true)
                {
                    line = line_iter.next();
                    if(i(line,0,this.getHeaderWidth()).equals(GENBANK_SPACER))
                        data += " " + i(line,this.getHeaderWidth());
                    else
                    {
                        // We now have all the data for this entry:
                        //getattr(consumer, consumer_dict[line_type])(data)
                    	  consumer.getClass().getMethod(consumer_dict.get(line_type),String.class).invoke(consumer,data.trim());
                        // End of continuation - return to top of loop!
                        break;
                    }
                }
            }
            else
            {
                logger.debug("Ignoring GenBank header line:\n" + line);
                // Read in next line
                line = line_iter.next();
            }
        }
    }
    catch(NoSuchElementException e)
    {
      throw new ParseException("Problem in header",e);
    }
	}
	
	private void feedDate(String date) throws Exception
	{
		consumer.date(dateFormat.parse(date),null);
	}
       	
	public void parseFirstLine() throws Exception
	{
    /*Scan over and parse GenBank LOCUS line (PRIVATE).

       This must cope with several variants, primarily the old and new column
       based standards from GenBank. Additionally EnsEMBL produces GenBank
       files where the LOCUS line is space separated rather that following
       the column based layout.

       We also try to cope with GenBank like files with partial LOCUS lines.
     */
     //#####################################
     //# LOCUS line                        #
     //#####################################
       int GENBANK_INDENT = this.getHeaderWidth();
      // String GENBANK_SPACER = String.format("%1$"+GENBANK_INDENT+"s"," ");
       assert line.startsWith("LOCUS       ") : "LOCUS line does not start correctly:\n" + line;

       // Have to break up the locus line, and handle the different bits of it.
       //There are at least two different versions of the locus line...
       if(set(" bp ", " aa ", " rc ").contains(i(line,29,33)) && i(line,55,62).equals("       "))
       {
           // Old... note we insist on the 55:62 being empty to avoid trying
           // to parse space separated LOCUS lines from Ensembl etc, see below.
           //
           //    Positions  Contents
           //    ---------  --------
           //    00:06      LOCUS
           //    06:12      spaces
           //    12:??      Locus name
           //    ??:??      space
           //    ??:29      Length of sequence, right-justified
           //    29:33      space, bp, space
           //    33:41      strand type
           //    41:42      space
           //    42:51      Blank (implies linear), linear or circular
           //    51:52      space
           //    52:55      The division code (e.g. BCT, VRL, INV)
           //    55:62      space
           //    62:73      Date, in the form dd-MMM-yyyy (e.g., 15-MAR-1991)
           //
           // assert(i(line,29,33) in [" bp ", " aa "," rc "] , //       "LOCUS line does not contain size units at expected position:\n" + line
           assert(i(line,41,42).equals(" ")) : "LOCUS line does not contain space at position 42:\n" + line;
           String isCircular = i(line,42,51).trim();
           assert(set("", "linear", "circular").contains(isCircular)) : "LOCUS line does not contain valid entry (linear, circular, ...):\n" + line;
           assert(i(line,51,52).equals(" ")) : "LOCUS line does not contain space at position 52:\n" + line;
           // assert(i(line,55,62).equals("       "), "LOCUS line does not contain spaces from position 56 to 62:\n" + line)
           if(!i(line,62,73).trim().equals(""))
           {
               assert(i(line,64,65).equals("-")) : "LOCUS line does not contain - at position 65 in date:\n" + line;
               assert(i(line,68,69).equals("-")) : "LOCUS line does not contain - at position 69 in date:\n" + line;
           }

           String name_and_length_str = i(line,GENBANK_INDENT,29);
           while(name_and_length_str.indexOf("  ") != -1)
               name_and_length_str = name_and_length_str.replace("  ", " ");
           String name_and_length[] = name_and_length_str.split(" ");
           assert(name_and_length.length <= 2) :  "Cannot parse the name and length in the LOCUS line:\n" + line;
           assert(name_and_length.length != 1) : "Name and length collide in the LOCUS line:\n" + line;
           // Should be possible to split them based on position, if
           // a clear definition of the standard exists THAT AGREES with
           // existing files.
           consumer.locus(name_and_length[0]);
           this.feedSize(Integer.parseInt(name_and_length[1]));
           // consumer.residue_type(i(line,33,41).trim())
           feedResidueType(i(line,29,33).trim());
           consumer.strandType(StrandType.fromStringChecked(i(line,33,41).trim()));
           
           if(isCircular.equals("circular"))
          	 consumer.circular();
           
           consumer.data_file_division(i(line,52,55));
           if(!i(line,62,73).trim().equals(""))
           {
          	 feedDate(i(line,62,73));
           }
       }
       else if(set(" bp ", " aa ", " rc ").contains(i(line,40,44)) && set("", "linear", "circular").contains(i(line,54,63).trim()))
       {
           /* New... linear/circular/big blank test should avoid EnsEMBL style
             LOCUS       NC_003479               1111 bp ss-DNA     circular VRL 10-FEB-2015
             012345678901234567890123456789012345678901234567890123456789012345678901234567890
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
      	   String residue = i(line,40,44).trim();
      	   String circular = i(line,54,63).trim();
      	   String strandTypePart1 = i(line,44,47).trim().toLowerCase();
      	   String strandTypePart2 = i(line,47,54).trim().toUpperCase();
           assert(set("bp", "aa", "rc").contains(residue)) : "LOCUS line does not contain size units at expected position:\n" + line;
           assert(set("", "ss-", "ds-", "ms-").contains(strandTypePart1)) : "LOCUS line does not have valid strand type (Single stranded, ...):\n" + line;
           assert(set("","DNA","RNA","tRNA","mRNA","uRNA","snRNA","cDNA").contains(strandTypePart2)) : "LOCUS line does not contain valid sequence type (DNA, RNA, ...):\n" + line;
           assert(i(line,54,55).equals(" ")) : "LOCUS line does not contain space at position 55:\n" + line;
           assert(set("", "linear", "circular").contains(circular)) : "LOCUS line does not contain valid entry (linear, circular, ...):\n" + line;
           assert(i(line,63,64).equals(" ")) : "LOCUS line does not contain space at position 64:\n" + line;
           assert(i(line,67,68).equals(" ")) : "LOCUS line does not contain space at position 68:\n" + line;
           if(!i(line,68,79).trim().equals(""))
           {
               assert(i(line,70,71).equals("-")) : "LOCUS line does not contain - at position 71 in date:\n" + line;
               assert(i(line,74,75).equals("-")) : "LOCUS line does not contain - at position 75 in date:\n" + line;
           }

           String name_and_length_str = i(line,GENBANK_INDENT,40);
           while(name_and_length_str.indexOf("  ") != -1)
           {
               name_and_length_str = name_and_length_str.replace("  ", " ");
           }
           String name_and_length[] = name_and_length_str.split(" ");
           assert(name_and_length.length <= 2) : "Cannot parse the name and length in the LOCUS line:\n" + line;
           assert(name_and_length.length != 1) : "Name and length collide in the LOCUS line:\n" + line;
           // Should be possible to split them based on position, if
           // a clear definition of the stand exists THAT AGREES with
           // existing files.
           consumer.locus(name_and_length[0]);
           this.feedSize(Integer.parseInt(name_and_length[1]));

           feedResidueType(residue);
           consumer.strandMultiplicity(StrandMultiplicity.fromStringChecked(strandTypePart1.replaceAll("-","").trim()));
           consumer.strandType(StrandType.fromStringChecked(strandTypePart2));
           
           if(circular.equals("circular"))
          	 consumer.circular();
           
           consumer.data_file_division(i(line,64,67));
           if(!i(line,68,79).trim().equals(""))
           {
          	 feedDate(i(line,68,79));
           }
       }
       else if( StringUtils.countMatches(i(line,GENBANK_INDENT).trim()," ") == 0)
       {
           // Truncated LOCUS line, as produced by some EMBOSS tools - see bug 1762
           //
           // e.g.
           //
           //    "LOCUS       U00096"
           //
           // rather than:
           //
           //    "LOCUS       U00096               4639675 bp    DNA     circular BCT"
           //
           //    Positions  Contents
           //    ---------  --------
           //    00:06      LOCUS
           //    06:12      spaces
           //    12:??      Locus name
           if(!i(line,GENBANK_INDENT).trim().equals(""))
               consumer.locus(i(line,GENBANK_INDENT).trim());
           else
               // Must just have just "LOCUS       ", is this even legitimate?
               // We should be able to continue parsing... we need real world testcases!
               logger.warn(String.format("Minimal LOCUS line found - is this correct?\n:%r",line));
           this.bogus("Minimal truncated locus line encountered");
       }
       else if(line.split("[\\s\\t\\n\\r\\f]+").length == 8 || line.split("[\\s\\t\\n\\r\\f]+").length == 7 &&  set("aa", "bp").contains(line.split("[\\s\\t\\n\\r\\f]+")[3]) && set("linear", "circular").contains(line.split("[\\s\\t\\n\\r\\f]+")[5]))
       {
           // Cope with invalidly spaced GenBank LOCUS lines like with date
           // LOCUS       AB070938          6497 bp    DNA     linear   BCT 11-OCT-2001
      	   // or without date (Header from RAST genbank files)
           // LOCUS       opera_scaffold_7        92652 bp    DNA     linear   UNK 
           String splitline[] = line.split("[\\s\\t\\n\\r\\f]+");
           consumer.locus(splitline[1]);
           this.feedSize(Integer.parseInt(splitline[2]));
           feedResidueType(splitline[3]);
           parseFullStrandType(splitline[4]);
           if(splitline[5].equals("circular"))
             consumer.circular();
           consumer.data_file_division(splitline[6]);
           if(splitline.length == 8)
             feedDate(splitline[7]);
           this.bogus(String.format("Attempting to parse malformed locus line:\n%s\n Found locus %s size %s residue_type %s\n Some fields may be wrong.",
          		 line, splitline[1], splitline[2], splitline[4]));
       }
       else if(line.split("[\\s\\t\\n\\r\\f]+").length == 7 && set("aa", "bp").contains(line.split("[\\s\\t\\n\\r\\f]+")[3]))
       {
           // Cope with EnsEMBL genbank files which use space separation rather
           // than the expected column based layout. e.g.
           // LOCUS       HG531_PATCH 1000000 bp DNA HTG 18-JUN-2011
           // LOCUS       HG531_PATCH 759984 bp DNA HTG 18-JUN-2011
           // LOCUS       HG506_HG1000_1_PATCH 814959 bp DNA HTG 18-JUN-2011
           // LOCUS       HG506_HG1000_1_PATCH 1219964 bp DNA HTG 18-JUN-2011
           // Notice that the "bp" can occur in the position expected by either
           // the old or the new fixed column standards (parsed above).
           String splitline[] = line.split("[\\s\\t\\n\\r\\f]+");
           consumer.locus(splitline[1]);
           this.feedSize(Integer.parseInt(splitline[2]));
           feedResidueType(splitline[3]);
           parseFullStrandType(splitline[4]);
           consumer.data_file_division(splitline[5]);
           feedDate(splitline[6]);
       }
       else if(line.split("[\\s\\t\\n\\r\\f]+").length >= 4 && set("aa", "bp").contains(line.split("[\\s\\t\\n\\r\\f]+")[3]))
       {
           // Cope with EMBOSS seqret output where it seems the locus id can cause
           // the other fields to overflow.  We just IGNORE the other fields!
      	   this.bogus(String.format("Malformed LOCUS line found - is this correct?\n:%r",line));
           consumer.locus(line.split("[\\s\\t\\n\\r\\f]")[1]);
           this.feedSize(Integer.parseInt(line.split("[\\s\\t\\n\\r\\f]")[2]));
       }
       else if(line.split("[\\s\\t\\n\\r\\f]+").length >= 4 &&  set("aa", "bp").contains(line.split("[\\s\\t\\n\\r\\f]+")[line.split("[\\s\\t\\n\\r\\f]+").length - 1]))
       {
           // Cope with pseudo-GenBank files like this:
           //   "LOCUS       RNA5 complete       1718 bp"
           // Treat everything between LOCUS and the size as the identifier.
           this.bogus(String.format("Malformed LOCUS line found - is this correct?\n:%r",line));
           consumer.locus(rsplit(i(line,5),"[\\s\\t\\n\\r\\f]+", 2)[0].trim());
           this.feedSize(Integer.parseInt(line.split("[\\s\\t\\n\\r\\f]+")[line.split("[\\s\\t\\n\\r\\f]+").length - 2]));
       }
       else
       {
           throw new ParseException("Did not recognise the LOCUS line layout:\n" + line);
       }
	}
	private void parseFullStrandType(String strandType) throws Exception
	{
		String tmp[] = strandType.trim().split("-");
		if(tmp.length == 2)
		{
			consumer.strandMultiplicity(StrandMultiplicity.fromStringChecked(tmp[0]));
			consumer.strandType(StrandType.fromStringChecked(tmp[1]));
		}
		else if(tmp.length == 1)
		{
			consumer.strandType(StrandType.fromStringChecked(tmp[0]));
		}
		else
		{
			throw new ParseException("could not parse strand multiplicity + strand type");
		}
	}

}
