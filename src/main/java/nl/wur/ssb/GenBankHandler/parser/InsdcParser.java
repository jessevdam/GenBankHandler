package nl.wur.ssb.GenBankHandler.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import nl.wur.ssb.GenBankHandler.data.QualifierValue;
import nl.wur.ssb.GenBankHandler.data.ResidueType;
import nl.wur.ssb.GenBankHandler.data.TextQualifierValue;
import nl.wur.ssb.GenBankHandler.util.Util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public abstract class InsdcParser
{
	protected BufferedReader in;
	protected ParserConsumer2 consumer;
	protected static final Logger logger = Logger.getLogger("nl.wur.ssb.GenBankHandler.InsdcParser");
	protected String line;
	private String lastKey = null;
  private String lastValue = null;
  private boolean allowBogus = true;
  public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy",Locale.ENGLISH);
  
	private HashSet<String> dblinks = new HashSet<String>();
	private HashSet<String> refNums = new HashSet<String>();
	private boolean keywordsPassed = false;
	private boolean taxPassed = false;
	private LocationParser locationParser;	
	
	public InsdcParser(InputStream in,ParserConsumer2 consumer)
	{
		this.in = new BufferedReader(new InputStreamReader(in));
		this.consumer = consumer;
		this.locationParser = new LocationParser(consumer);
	}
	
	/* Warns if a bogus or obsolete element is found 
	 * produces error if bogus or obsolete element is not allowed*/
	public void bogus(String msg) throws Exception
	{
		logger.warn(msg);
		if(!this.allowBogus)
			throw new ParseException(msg);
	}
	
	public abstract String getRecordStart();
	/*
	  GENBANK version 1: locus size residue_type data_file_division date
    GENBANK version 2: locus size residue_type data_file_division date
    GENBANK versiio 3: locus
    GENBANK version 4: locus size residue_type data_file_division date
    GENBANK version 5: locus size residue_type data_file_division date
    GENBANK version 6: locus size 

    EMBL NEW:   locus size residue_type data_file_division (accession version suffix?) 
    EMBL old:   locus size residue_type data_file_division
    EMBL patent:lcous - residue_type data_file_division
	 */
	public abstract void parseFirstLine() throws Exception;
	public abstract void parseHeaderLines(ArrayList<String> lines) throws Exception;
	public abstract ArrayList<String> parseFooter() throws Exception;
	public abstract void feedMiscLines(ArrayList<String> lines) throws Exception;
	
	public abstract int getHeaderWidth();
	public abstract Set<String> getFeatureStartMarkers(); 
	public abstract Set<String> getSequenceHeaders();
	public abstract Set<String> getFeatureEndMarkers();
	public abstract int getFeatureQualifierIndent();
	public abstract String getFeatureQualifierSpacer();
	
	public boolean parse(boolean do_features) throws Exception
	{
		this.reset();
		line = this.find_start();
		if(line == null)
		{
			//consumer.end();
			return false;
		}
		this.parseFirstLine();
		this.parseHeaderLines(this.parseHeader());
		
    //# Features (common to both EMBL and GenBank):
   	this.parseFeatures(!do_features);  //ignore the data

    // Footer and sequence
    ArrayList<String> misc_lines = this.parseFooter();
    this.feedMiscLines(misc_lines);
    
    // Calls to consumer.base_number() do nothing anyway
    this.consumer.recordEnd();

    assert this.line.equals("//");
    // And we are done
  		
		return true;
	}
	
	public void feedLocation(String location) throws Exception
	{
		this.locationParser.feedLocation(location);
	}
	
	//TODO rename
  public void feedAccession2(String val,int version) throws Exception
  {
  	for(String acc : val.replaceAll("\\n"," ").replaceAll(";"," ").trim().split("[\\s\\t\\n\\r\\f]"))
  	{
  		if(!acc.equals(""))
 			  consumer.accession(acc,version);
  	}
  }
  
  public void feedProject(String val)  throws Exception
  {
  	val = val.replaceAll("GenomeProject","Project");
  	for(String elem : val.split("[\\s\\t\\n\\r\\f]"))
  	{
  		if(!elem.equals(""))
  			this.feedDblink(elem);
  	}
  }
  
  public void feedDblink(String val)  throws Exception
  {
  	if(!dblinks.contains(val))
  	{
  		String tmp[] = val.split(":");
  		consumer.dblink(tmp[0].trim(),tmp[1].trim());
  	}
  	dblinks.add(val);  	
  }

  public void feedKeywords(String keyword_string)  throws Exception
  {
    //Split a string of keywords into a nice clean list.
  	if(keywordsPassed)
  		throw new ParseException("Keywords can only be given once per record");
  	keywordsPassed = true; 
    if(keyword_string.endsWith("."))
    	keyword_string = Util.i(keyword_string,0,-1);

    HashSet<String> allKeywords = new HashSet<String>();
    for(String keyword : keyword_string.split(";"))
    {
    	keyword = keyword.trim();
    	if(!keyword.equals(""))
    	{
    		allKeywords.add(keyword);
    	}
    }    
    this.consumer.keywords(new ArrayList<String>(allKeywords));
  }
   
  public void feedTaxonomy(String tax)  throws Exception
  {
  	if(this.taxPassed)
  		throw new ParseException("taxonomy line be given only once");
    this.taxPassed = true;
    //Split a string with taxonomy info into a list.
  	HashSet<String> allTax = new LinkedHashSet<String>();
    if(tax.endsWith("."))
        tax = Util.i(tax,0,-1);

    for(String item : tax.split(";"))
    {
    	item = item.trim();
    	if(!item.equals(""))
    	{
    		allTax.add(item);
    	}
    }
    this.consumer.taxonomy(new ArrayList<String>(allTax));
  }
  
	public void feedSize(int size) throws Exception
	{
		this.locationParser.setSequenceSize(size);
		this.consumer.size(size);				
	}
	

	public void feedReferceNum(String refNum) throws Exception
	{
		if(refNums.contains(refNum))
			throw new ParseException("Same reference number repeated");
		refNums.add(refNum);
		this.consumer.reference_num(Integer.parseInt(refNum));			
	}
	
	protected void feedResidueType(String residue) throws Exception
	{
    if(residue.equals("aa"))
      consumer.residueType(ResidueType.PROTEIN);
    else if(residue.equals("bp"))
      consumer.residueType(ResidueType.DNA);
    else if(residue.equals("rc"))
   	  consumer.residueType(ResidueType.RIBOSOMAL_CODE);
    else 
   	  throw new ParseException("Residue type not recoqnized:" + residue);
	}
	
	public void reset()
	{
		this.keywordsPassed = false;
		this.taxPassed = false;
		this.refNums.clear();
		this.dblinks.clear();
		this.locationParser.setSequenceSize(-1);
	}
  
	public void feedReferenceBases(String refBase) throws Exception
	{
    /*Attempt to determine the sequence region the reference entails.

    Possible types of information we may have to deal with:

    (bases 1 to 86436)
    (sites)
    (bases 1 to 105654; 110423 to 111122)
    1  (residues 1 to 182)
    */
    // first remove the parentheses or other junk
    String ref_base_info = Util.i(refBase,1,-1);
    String type = "";

    // parse if we've got 'bases' and 'to'
    if(ref_base_info.indexOf("bases") != -1 && ref_base_info.indexOf("to") != -1)
    {
        // get rid of the beginning 'bases'
        ref_base_info = Util.i(ref_base_info,5);
        type = "bases";
    }
    else if(ref_base_info.indexOf("residues") != -1 && ref_base_info.indexOf("to") != -1)
    {
        int residues_start = ref_base_info.indexOf("residues");
        // get only the information after "residues"
        ref_base_info = Util.i(ref_base_info,residues_start + "residues ".length());
        type = "residues";
    }
    // make sure if we are not finding information then we have
    // the string 'sites' or the string 'bases'
    else if(ref_base_info.equals("sites"))
    	type = "sites";    	
    else if(ref_base_info.trim().equals("bases"))
      type = "bases";
    else
      throw new ParseException("Could not parse base info " + ref_base_info);
    
    String all_base_info[] = ref_base_info.split(";");

    ArrayList<Integer> startSites = new ArrayList<Integer>();
    ArrayList<Integer> endSites = new ArrayList<Integer>();
    for(String base_info : all_base_info)
    {
      if(base_info.contains("to"))
      {
    	  String tmp[] = base_info.split("to");
        String start = tmp[0];
        String end = tmp[1]; 
        startSites.add(Integer.parseInt(start.trim()) - 1);
        endSites.add(Integer.parseInt(end.trim()));
      }
    }
    this.consumer.refereceLocation(type,startSites,endSites);
	}
  
	public void parseFeatures(boolean skip) throws Exception
	{
    /*Return list of tuples for the features (if present)

    Each feature is returned as a tuple (key, location, qualifiers)
    where key and location are strings (e.g. "CDS" and
    "complement(join(490883..490885,1..879))") while qualifiers
    is a list of two string tuples (feature qualifier keys and values).

    Assumes you have already read to the start of the features table.
    */
    if(!this.getFeatureStartMarkers().contains(rtrim(this.line)))
    {
        logger.debug("Didn't find any feature table");
    }

    while(this.getFeatureStartMarkers().contains(rtrim(this.line)))
        this.line = this.in.readLine();

    if(!skip)
    	consumer.startFeatureTable();
    String line = this.line;
    while(true)
    {
        if(line == null || line.equals(""))
            throw new ParseException("Premature end of line during features table");
        if(this.getSequenceHeaders().contains(rtrim(i(line,0,this.getHeaderWidth()))))
        {
            logger.debug("Found start of sequence");
            break;
        }
        line = rtrim(line);
        if(line.equals("//"))
            throw new ParseException("Premature end of features table, marker '//' found");
        if(this.getFeatureEndMarkers().contains(line))
        {
            logger.debug("Found end of features");
            line = this.in.readLine();
            break;
        }
        if(i(line,2,this.getFeatureQualifierIndent()).trim().equals(""))
        {
            //This is an empty feature line between qualifiers. Empty
            //feature lines within qualifiers are handled below (ignored).
            line = this.in.readLine();
            continue;
        }
        if(line.length() < this.getFeatureQualifierIndent())
        {
            logger.warn("line too short to contain a feature: " + line);
            line = this.in.readLine();
            continue;
        }

        if(skip)
        {
            line = this.in.readLine();
            while(i(line,0,this.getFeatureQualifierIndent()).equals(this.getFeatureQualifierSpacer()))
                line = this.in.readLine();
        }
        else
        {
        	  ArrayList<String> feature_lines = new ArrayList<String>();
        	  String feature_key = "";
            // Build up a list of the lines making up this feature:
            if(!i(line,this.getFeatureQualifierIndent(),this.getFeatureQualifierIndent() +1).equals(" ") &&
            	  i(line,this.getFeatureQualifierIndent()).indexOf(" ") != -1)
            {
                // The feature table design enforces a length limit on the feature keys.
                // Some third party files (e.g. IGMT's EMBL like files) solve this by
                // over indenting the location and qualifiers.
                String temp[] = i(line,2).trim().split("[\\s\\t\\n\\r\\f]", 1);
                feature_key = temp[0];
                line = temp[1];
                feature_lines.add(line);
                logger.warn(String.format("Overindented %s feature?",feature_key));
            }
            else
            {
                feature_key = i(line,2,this.getFeatureQualifierIndent()).trim();
                feature_lines.add(i(line,this.getFeatureQualifierIndent()));
            }
            line = this.in.readLine();
            // cope with blank lines in the midst of a feature
            while(i(line,0,this.getFeatureQualifierIndent()).equals(this.getFeatureQualifierSpacer()) ||
            		(!line.equals("") && rtrim(line).equals("")))
            {
                // Use strip to remove any harmless trailing white space AND and leading
                // white space (e.g. out of spec files with too much indentation)
                feature_lines.add(i(line,this.getFeatureQualifierIndent()).trim());
                line = this.in.readLine();
            }
            this.parse_feature(feature_key, feature_lines);
        }
    }
    this.line = line;
    if(!skip)
    	consumer.endFeatureTable();    	
	}
	

	
  public void parse_feature(String feature_key, ArrayList<String> lines) throws Exception
  {
    /*Expects a feature as a list of strings, returns a tuple (key, location, qualifiers)

    For example given this GenBank feature::

         CDS             complement(join(490883..490885,1..879))
                         /locus_tag="NEQ001"
                         /note="conserved hypothetical [Methanococcus jannaschii];
                         COG1583:Uncharacterized ACR; IPR001472:Bipartite nuclear
                         localization signal; IPR002743: Protein of unknown
                         function DUF57"
                         /codon_start=1
                         /transl_table=11
                         /product="hypothetical protein"
                         /protein_id="NP_963295.1"
                         /db_xref="GI:41614797"
                         /db_xref="GeneID:2732620"
                         /translation="MRLLLELKALNSIDKKQLSNYLIQGFIYNILKNTEYSWLHNWKK
                         EKYFNFTLIPKKDIIENKRYYLIISSPDKRFIEVLHNKIKDLDIITIGLAQFQLRKTK
                         KFDPKLRFPWVTITPIVLREGKIVILKGDKYYKVFVKRLEELKKYNLIKKKEPILEEP
                         IEISLNQIKDGWKIIDVKDRYYDFRNKSFSAFSNWLRDLKEQSLRKYNNFCGKNFYFE
                         EAIFEGFTFYKTVSIRIRINRGEAVYIGTLWKELNVYRKLDKEEREFYKFLYDCGLGS
                         LNSMGFGFVNTKKNSAR"

    Then should give input key="CDS" and the rest of the data as a list of strings
    lines=["complement(join(490883..490885,1..879))", ..., "LNSMGFGFVNTKKNSAR"]
    where the leading spaces and trailing newlines have been removed.

    Returns tuple containing: (key as string, location string, qualifiers as list)
    as follows for this example:

    key = "CDS", string
    location = "complement(join(490883..490885,1..879))", string
    qualifiers = list of string tuples:

    [('locus_tag', '"NEQ001"'),
     ('note', '"conserved hypothetical [Methanococcus jannaschii];\nCOG1583:..."'),
     ('codon_start', '1'),
     ('transl_table', '11'),
     ('product', '"hypothetical protein"'),
     ('protein_id', '"NP_963295.1"'),
     ('db_xref', '"GI:41614797"'),
     ('db_xref', '"GeneID:2732620"'),
     ('translation', '"MRLLLELKALNSIDKKQLSNYLIQGFIYNILKNTEYSWLHNWKK\nEKYFNFT..."')]

    In the above example, the "note" and "translation" were edited for compactness,
    and they would contain multiple new line characters (displayed above as \n)

    If a qualifier is quoted (in this case, everything except codon_start and
    transl_table) then the quotes are NOT removed.

    Note that no whitespace is removed.
    */
    // Skip any blank lines
  	this.consumer.startFeature(feature_key);
  	
    ArrayList<String> temp = new ArrayList<String>();
    for(String line : lines)
    {
    	if(!line.equals(""))
    		temp.add(line);
    }
    lines = temp;
    Iterator<String> iterator = lines.iterator();
    try
    {
        line = iterator.next();

        String feature_location = line.trim();
        while(feature_location.endsWith(","))
        {
            // Multiline location, still more to come!
            line = iterator.next();
            feature_location += line.trim();
        }
        if(StringUtils.countMatches(feature_location,"(") > StringUtils.countMatches(feature_location,")"))
        {
            // Including the prev line in warning would be more explicit,
            // but this way get one-and-only-one warning shown by default:
            logger.warn("Non-standard feature line wrapping (didn't break on comma)?");
            while(feature_location.endsWith(",") || StringUtils.countMatches(feature_location,"(") > StringUtils.countMatches(feature_location,")"))
            {
                line = iterator.next();
                feature_location += line.trim();
            }
        }
        
        this.feedLocation(feature_location);

        lastKey = null;
        lastValue = null;
        
        while(iterator.hasNext())
        {
        	  line = iterator.next();
            // check for extra wrapping of the location closing parentheses
        	  // should not be possible with StringUtils.countMatches(feature_location,"(") > StringUtils.countMatches(feature_location,")")
            //if(line_number == 0 && line.startsWith(")"))
            //    feature_location += line.trim();
            //else
        	  if(line.startsWith("/"))
            {
                // New qualifier
                int i = line.indexOf("=");
                String key = i(line,1,i);  // does not work if i==-1
                String value = i(line,i + 1);  // we ignore 'value' if i==-1
                if(i == -1)
                {
                    // Qualifier with no key, e.g. /pseudo
                    key = i(line,1);
                    putQualifierSet(key, null);
                }
                else if(value.equals(""))
                {
                    // ApE can output /note=
                	putQualifierSet(key, "");
                }
                else if(value.equals("\""))
                {
                    // One single quote
                    logger.debug(String.format("Single quote %s:%s",key, value));
                    // DO NOT remove the quote...
                    putQualifierSet(key, value);
                }
                else if(i(value,0,1).equals("\""))
                {
                    // Quoted...
                    while(!value.endsWith("\""))
                    	value = value + "\n" + iterator.next();
                    // DO NOT remove the quotes...
                    putQualifierSet(key, value);
                }
                else
                {
                    // Unquoted
                    // if debug : print("Unquoted line %s:%s" % (key,value))
                	putQualifierSet(key, value);
                }
            }
            else
            {
                // Unquoted continuation
                assert lastKey != null;
                //assert key == qualifiers[-1][0];
                // if debug : print("Unquoted Cont %s:%s" % (key, line))
                if(lastValue == null) 
                    throw new ParseException("Incorrect qualifier continuation");
                this.lastValue = this.lastValue + "\n" + line;
            }
        }
        //flush last qualifier
        putQualifierSet(null,null);
        this.consumer.endFeature();
    }
    catch(NoSuchElementException e)
    {
        // Bummer
        throw new ParseException(String.format("Problem with '%s' feature:\n%s",feature_key, StringUtils.join(lines,"\n")));
    }
  }
  
  private void putQualifierSet(String key,String value) throws Exception
  {
  	if(this.lastKey != null)
  	{
  		if(this.lastValue != null)
  		{
  			this.lastValue = this.lastValue.replaceAll("\\n"," ");
  			if(this.lastKey.equals("translation"))
  				this.lastValue = this.lastValue.replaceAll(" ","");
  			if(this.lastValue != null && this.lastValue.startsWith("\"") && this.lastValue.endsWith("\""))
  		    consumer.featureQualifier(this.lastKey, new TextQualifierValue(i(this.lastValue,1,-1)));
  			else
  			  consumer.featureQualifier(this.lastKey, new QualifierValue(this.lastValue));
  		}
  		else
  			consumer.featureQualifier(this.lastKey, null);
  	}
  	this.lastKey = key;
  	this.lastValue = value;
  }
	
	public ArrayList<String> parseHeader() throws Exception
	{
    /*Return list of strings making up the header

    New line characters are removed.

    Assumes you have just read in the ID/LOCUS line.
    */
    assert i(line,0,this.getHeaderWidth()).equals(this.getRecordStart()) : "Not at start of record";

    ArrayList<String> header_lines = new ArrayList<String>();
    while(true)
    {
        line = in.readLine();
        if(line == null)
            throw new ParseException("Premature end of line during sequence data");
        line = rtrim(line);
        if(this.getFeatureStartMarkers().contains(line))
        {
            logger.debug("Found feature table");
            break;
        }
        // if line[:self.HEADER_WIDTH]==self.FEATURE_START_MARKER[:self.HEADER_WIDTH]:
        //    if self.debug : print("Found header table (?)")
        //    break
        if(this.getSequenceHeaders().contains(rtrim(i(line,0,this.getHeaderWidth())) ))
        {
            logger.debug("Found start of sequence");
            break;
        }
        if(line.equals("//"))
            throw new ParseException("Premature end of sequence data marker \"//\" found");
        if(!line.equals(""))
          header_lines.add(line);
    }
    return header_lines;
	}

	
	public String find_start() throws IOException
	{
		String line = null;
		while((line = this.in.readLine()) != null)
		{
		  if(line.startsWith(this.getRecordStart()))
		  	return line;
		 // line = line.trim();
		 // if(line.equals("//" || line.equals("")))
		}
		return null;
	}
	
	public Set<String> set(String... values) 
	{
    return Util.set(values);
  }
	
	protected String[] rsplit(String in,String regex,int max)
	{
		return Util.rsplit(in,regex,max);
	}
	
	protected String rtrim(String s) {
    return Util.rtrim(s);
  }
	
	protected String rtrim(String s,char character) {
    return Util.rtrim(s,character);
  }

	protected String ltrim(String s) {
    return Util.ltrim(s);
  }
	
	protected String ltrim(String s,char character) {
    return Util.ltrim(s,character);
  }
	protected String i(String s,int begin,int end)
	{
		return Util.i(s,begin,end);
	}
	protected String i(String s,int begin)
	{
		return Util.i(s,begin);
	}
}
