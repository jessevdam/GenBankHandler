package nl.wur.ssb.GenBankHandler.parser;

import java.util.ArrayList;
import java.util.regex.Pattern;

import nl.wur.ssb.GenBankHandler.util.Util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class LocationParser
{
	private static final String _solo_location = "[<>]?\\d+";
	private static final String _pair_location = "[<>]?\\d+\\.\\.[<>]?\\d+";
	private static final String _between_location = "\\d+\\^\\d+";

	private static final String _within_position = "\\(\\d+\\.\\d+\\)";
	private static final Pattern _re_within_position = Pattern.compile(_within_position);
	private static final String _within_location = String.format("([<>]?\\d+|%s)\\.\\.([<>]?\\d+|%s)",_within_position, _within_position);
	
	private static final String _oneof_position = "one\\-of\\(\\d+(,\\d+)+\\)";
	private static final Pattern _re_oneof_position = Pattern.compile(_oneof_position);
	private static final String _oneof_location = String.format("([<>]?\\d+|%s)\\.\\.([<>]?\\d+|%s)",_oneof_position, _oneof_position);
	
	private static final String _simple_location = "\\d+\\.\\.\\d+";
	private static final Pattern _re_simple_location = Pattern.compile(String.format("^%s$",_simple_location));
	private static final Pattern _re_simple_compound = Pattern.compile(String.format("^(join|order|bond)\\(%s(,%s)*\\)$",_simple_location, _simple_location));
	private static final String _complex_location = String.format("([a-zA-z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9]+)?\\:)?(%s|%s|%s|%s|%s)",_pair_location, _solo_location, _between_location, _within_location, _oneof_location);
	private static final Pattern _re_complex_location = Pattern.compile(String.format("^%s$",_complex_location));
	private static final String _possibly_complemented_complex_location = String.format("(%s|complement\\(%s\\))",_complex_location, _complex_location);
	private static final Pattern _re_complex_compound = Pattern.compile(String.format("^(join|order|bond)\\(%s(,%s)*\\)$",_possibly_complemented_complex_location, _possibly_complemented_complex_location));
	private static final Pattern _solo_bond = Pattern.compile(String.format("bond\\((%s)\\)",_solo_location));
	
	public static void testRegExp()
	{
			assert _re_within_position.matcher("(3.9)").matches();
			assert Pattern.compile(_within_location).matcher("(3.9)..10").matches();
			assert Pattern.compile(_within_location).matcher("26..(30.33)").matches();
			assert Pattern.compile(_within_location).matcher("(13.19)..(20.28)").matches();
	
			assert _re_oneof_position.matcher("one-of(6,9)").matches();
			assert Pattern.compile(_oneof_location).matcher("one-of(6,9)..101").matches();
			assert Pattern.compile(_oneof_location).matcher("one-of(6,9)..one-of(101,104)").matches();
			assert Pattern.compile(_oneof_location).matcher("6..one-of(101,104)").matches();

			assert !_re_oneof_position.matcher("one-of(3)").matches();
			assert _re_oneof_position.matcher("one-of(3,6)").matches();
			assert _re_oneof_position.matcher("one-of(3,6,9)").matches();
	
			assert _re_simple_location.matcher("104..160").matches();
			assert !_re_simple_location.matcher("68451760..68452073^68452074").matches();
			assert !_re_simple_location.matcher("<104..>160").matches();
			assert !_re_simple_location.matcher("104").matches();
			assert !_re_simple_location.matcher("<1").matches();
			assert !_re_simple_location.matcher(">99999").matches();
			assert !_re_simple_location.matcher("join(104..160,320..390,504..579)").matches();
			assert !_re_simple_compound.matcher("bond(12,63)").matches();
			assert _re_simple_compound.matcher("join(104..160,320..390,504..579)").matches();
			assert _re_simple_compound.matcher("order(1..69,1308..1465)").matches();
			assert !_re_simple_compound.matcher("order(1..69,1308..1465,1524)").matches();
			assert !_re_simple_compound.matcher("join(<1..442,992..1228,1524..>1983)").matches();
			assert !_re_simple_compound.matcher("join(<1..181,254..336,422..497,574..>590)").matches();
			assert !_re_simple_compound.matcher("join(1475..1577,2841..2986,3074..3193,3314..3481,4126..>4215)").matches();
			assert !_re_simple_compound.matcher("test(1..69,1308..1465)").matches();
			assert !_re_simple_compound.matcher("complement(1..69)").matches();
			assert !_re_simple_compound.matcher("(1..69)").matches();
			assert _re_complex_location.matcher("(3.9)..10").matches();
			assert _re_complex_location.matcher("26..(30.33)").matches();
			assert _re_complex_location.matcher("(13.19)..(20.28)").matches();
			assert _re_complex_location.matcher("41^42").matches();  // between
			assert _re_complex_location.matcher("AL121804:41^42").matches();
			assert _re_complex_location.matcher("AL121804:41..610").matches();
			assert _re_complex_location.matcher("AL121804.2:41..610").matches();
			assert _re_complex_location.matcher("one-of(3,6)..101").matches();
			assert _re_complex_compound.matcher("join(153490..154269,AL121804.2:41..610,AL121804.2:672..1487)").matches();
			assert !_re_simple_compound.matcher("join(153490..154269,AL121804.2:41..610,AL121804.2:672..1487)").matches();
			assert _re_complex_compound.matcher("join(complement(69611..69724),139856..140650)").matches();

			// Trans-spliced example from NC_016406, note underscore in reference name:
			assert _re_complex_location.matcher("NC_016402.1:6618..6676").matches();
			assert _re_complex_location.matcher("181647..181905").matches();
			assert _re_complex_compound.matcher("join(complement(149815..150200),complement(293787..295573),NC_016402.1:6618..6676,181647..181905)").matches();
			assert !_re_complex_location.matcher("join(complement(149815..150200),complement(293787..295573),NC_016402.1:6618..6676,181647..181905)").matches();
			assert !_re_simple_compound.matcher("join(complement(149815..150200),complement(293787..295573),NC_016402.1:6618..6676,181647..181905)").matches();
			assert !_re_complex_location.matcher("join(complement(149815..150200),complement(293787..295573),NC_016402.1:6618..6676,181647..181905)").matches();
			assert !_re_simple_location.matcher("join(complement(149815..150200),complement(293787..295573),NC_016402.1:6618..6676,181647..181905)").matches();


			assert _solo_bond.matcher("bond(196)").matches();
			assert _solo_bond.matcher("bond(196)").find();
			assert _solo_bond.matcher("join(bond(284),bond(305),bond(309),bond(305))").find();
			assert _solo_bond.matcher("join(bond(284),bond(305),bond(309),bond(305))").replaceAll("$1").equals("join(284,305,309,305)");
	}
	
	private static final Logger logger = Logger.getLogger("nl.wur.ssb.GenBankHandler.LocationParser");

	private ParserConsumer2 consumer;
	private int size;
	public LocationParser(ParserConsumer2 consumer)
	{
		this.consumer = consumer;
	}
	public void setSequenceSize(int size)
	{
		this.size = size;
	}
	
	private ArrayList<String> splitCompoundLoc(String compound_loc)
	{
    /*Split a tricky compound location string (PRIVATE).

    >>> list(_split_compound_loc("123..145"))
    ['123..145']
    >>> list(_split_compound_loc("123..145,200..209"))
    ['123..145', '200..209']
    >>> list(_split_compound_loc("one-of(200,203)..300"))
    ['one-of(200,203)..300']
    >>> list(_split_compound_loc("complement(123..145),200..209"))
    ['complement(123..145)', '200..209']
    >>> list(_split_compound_loc("123..145,one-of(200,203)..209"))
    ['123..145', 'one-of(200,203)..209']
    >>> list(_split_compound_loc("123..145,one-of(200,203)..one-of(209,211),300"))
    ['123..145', 'one-of(200,203)..one-of(209,211)', '300']
    >>> list(_split_compound_loc("123..145,complement(one-of(200,203)..one-of(209,211)),300"))
    ['123..145', 'complement(one-of(200,203)..one-of(209,211))', '300']
    >>> list(_split_compound_loc("123..145,200..one-of(209,211),300"))
    ['123..145', '200..one-of(209,211)', '300']
    >>> list(_split_compound_loc("123..145,200..one-of(209,211)"))
    ['123..145', '200..one-of(209,211)']
    >>> list(_split_compound_loc("complement(149815..150200),complement(293787..295573),NC_016402.1:6618..6676,181647..181905"))
    ['complement(149815..150200)', 'complement(293787..295573)', 'NC_016402.1:6618..6676', '181647..181905']
    */
		ArrayList<String> toRet = new ArrayList<String>();
    if(compound_loc.indexOf("one-of(") != -1)
    {
        // Hard case
        while(compound_loc.indexOf(",") != -1)
        {
            assert !compound_loc.startsWith(",");
            assert !compound_loc.startsWith("..");
            int i = compound_loc.indexOf(",");
            String part = Util.i(compound_loc,0,i);
            compound_loc = Util.i(compound_loc,i); // includes the comma
            while(StringUtils.countMatches(part,"(") > StringUtils.countMatches(part,")"))
            {
                assert part.indexOf("one-of(") != -1 : "" + part + " : " + compound_loc;
                i = compound_loc.indexOf(")");
                part += Util.i(compound_loc,0,i + 1);
                compound_loc = Util.i(compound_loc,i + 1);
            }
            if(compound_loc.startsWith(".."))
            {
                i = compound_loc.indexOf(",");
                if(i == -1)
                {
                    part += compound_loc;
                    compound_loc = "";
                }
                else
                {
                    part += Util.i(compound_loc,0,i);
                    compound_loc = Util.i(compound_loc,i);  // includes the comma
                }
            }
            while(StringUtils.countMatches(part,"(") > StringUtils.countMatches(part,")"))
            {
                assert StringUtils.countMatches(part,"one-of(") == 2;
                i = compound_loc.indexOf(")");
                part += Util.i(compound_loc,0,i + 1);
                compound_loc = Util.i(compound_loc,i + 1);
            }
            if(compound_loc.startsWith(","))
                compound_loc = Util.i(compound_loc,1);
            assert !part.equals("");
            toRet.add(part);
        }
        if(!compound_loc.equals(""))
            toRet.add(compound_loc);       
    }
    else
    {
        // Easy case
    	for(String part : compound_loc.split(","))
        toRet.add(part);
    }
    return toRet;
	}
	
	//offset = -1 if position is begin
	public void feedPos(String pos_str, int offset) throws Exception
	{
    /*Build a Position object (PRIVATE).

    For an end position, leave offset as zero (default):

    >>> _pos("5")
    ExactPosition(5)

    For a start position, set offset to minus one (for Python counting):

    >>> _pos("5", -1)
    ExactPosition(4)

    This also covers fuzzy positions:

    >>> p = _pos("<5")
    >>> p
    BeforePosition(5)
    >>> print(p)
    <5
    >>> int(p)
    5

    >>> _pos(">5")
    AfterPosition(5)

    By default assumes an end position, so note the integer behaviour:

    >>> p = _pos("one-of(5,8,11)")
    >>> p
    OneOfPosition(11, choices=[ExactPosition(5), ExactPosition(8), ExactPosition(11)])
    >>> print(p)
    one-of(5,8,11)
    >>> int(p)
    11

    >>> _pos("(8.10)")
    WithinPosition(10, left=8, right=10)

    Fuzzy start positions:

    >>> p = _pos("<5", -1)
    >>> p
    BeforePosition(4)
    >>> print(p)
    <4
    >>> int(p)
    4

    Notice how the integer behaviour changes too!

    >>> p = _pos("one-of(5,8,11)", -1)
    >>> p
    OneOfPosition(4, choices=[ExactPosition(4), ExactPosition(7), ExactPosition(10)])
    >>> print(p)
    one-of(4,7,10)
    >>> int(p)
    4

    */
    if(pos_str.startsWith("<"))
    	this.consumer.beforePosition(Integer.parseInt(Util.i(pos_str,1)) + offset);
    else if(pos_str.startsWith(">"))
      this.consumer.afterPosition(Integer.parseInt(Util.i(pos_str,1)) + offset);
    else if(_re_within_position.matcher(pos_str).matches())
    {
        String tmp[] = Util.i(pos_str,1,-1).split("\\.");
        int s = Integer.parseInt(tmp[0]) + offset;
        int e = Integer.parseInt(tmp[1]) + offset;
        int def = e;
        if(offset == -1)
            def = s;
        consumer.withinPosition(def, s, e);
    }
    else if(_re_oneof_position.matcher(pos_str).matches())
    {
        assert pos_str.startsWith("one-of(");
        assert pos_str.endsWith(")");
        ArrayList<Integer> items = new ArrayList<Integer>();
        for(String pos : Util.i(pos_str,7,-1).split(","))
        {
        	items.add(Integer.parseInt(pos) + offset);
        }
        int def = 0;
                		
        if(offset == -1)
        {
        	def = Integer.MAX_VALUE;
        	for(int item : items)
        		def = Math.min(def,item);
        }
        else
        {
        	for(int item : items)
        		def = Math.max(def,item);
        }
        consumer.oneOf(def, items);
    }
    else
        consumer.exactLoc(Integer.parseInt(pos_str) + offset);
	}
	
	public void feedLoc(String loc_str) throws Exception
	{
    /*FeatureLocation from non-compound non-complement location (PRIVATE).

    Simple examples,

    >>> _loc("123..456", 1000, +1)
    FeatureLocation(ExactPosition(122), ExactPosition(456), strand=1)
    >>> _loc("<123..>456", 1000, strand = -1)
    FeatureLocation(BeforePosition(122), AfterPosition(456), strand=-1)

    A more complex location using within positions,

    >>> _loc("(9.10)..(20.25)", 1000, 1)
    FeatureLocation(WithinPosition(8, left=8, right=9), WithinPosition(25, left=20, right=25), strand=1)

    Notice how that will act as though it has overall start 8 and end 25.

    Zero length between feature,

    >>> _loc("123^124", 1000, 0)
    FeatureLocation(ExactPosition(123), ExactPosition(123), strand=0)

    The expected sequence length is needed for a special case, a between
    position at the start/end of a circular genome:

    >>> _loc("1000^1", 1000, 1)
    FeatureLocation(ExactPosition(1000), ExactPosition(1000), strand=1)

    Apart from this special case, between positions P^Q must have P+1==Q,

    >>> _loc("123^456", 1000, 1)
    Traceback (most recent call last):
       ...
    ValueError: Invalid between location '123^456'
    */
    String tmp[] = loc_str.split("\\.\\.");
    if(tmp.length == 1)
    {
        if(loc_str.indexOf("^") != -1)
        {
          // A between location like "67^68" (one based counting) is a
          // special case (note it has zero length). In python slice
          // notation this is 67:67, a zero length slice.  See Bug 2622
          // Further more, on a circular genome of length N you can have
          // a location N^1 meaning the junction at the origin. See Bug 3098.
          // NOTE - We can imagine between locations like "2^4", but this
          // is just "3".  Similarly, "2^5" is just "3..4"
        	if(this.size == -1)
        		throw new ParseException("can not parse inbetween location if sequence length is unknown");
          tmp = loc_str.split("\\^");
          int s = Integer.parseInt(tmp[0]);
          int e = Integer.parseInt(tmp[1]);
          if(s + 1 != e && (!(s == size && e == 1)))
            throw new ParseException("Invalid between location " + loc_str);
          this.consumer.inbetweenLoc(s);
        }
        else
        {
        	this.consumer.startSoloLoc();
        	this.feedPos(loc_str,0);
        	this.consumer.endSoloLoc();
        }
    }
    else
    {
  	  consumer.beginPair();
  	  this.feedPos(tmp[0],-1);
  	  this.feedPos(tmp[1],0);
  	  consumer.endPair();
    }
	}
  
	public void feedLocation(String location) throws Exception
	{
		if(this.size == -1)
			throw new ParseException("Size of master sequence still unknown at definition of location");
		consumer.startLocation();
    /*Parse out location information from the location string.

    This uses simple Python code with some regular expressions to do the
    parsing, and then translates the results into appropriate objects.
    */
    // clean up newlines and other whitespace inside the location before
    // parsing - locations should have no whitespace whatsoever
    String location_line = StringUtils.join(location.split("[\\s\\t\\n\\r\\f]"));

    // Older records have junk like replace(266,"c") in the
    // location line. Newer records just replace this with
    // the number 266 and have the information in a more reasonable
    // place. So we'll just grab out the number and feed this to the
    // parser. We shouldn't really be losing any info this way.
    if(location_line.indexOf("replace") != -1)
        location_line = Util.i(location_line,8, location_line.indexOf(","));

    int strand = 1;
    // Handle top level complement here for speed
    if(location_line.startsWith("complement("))
    {
        assert location_line.endsWith(")");
        location_line = Util.i(location_line,11,-1);
        strand = -1;
        consumer.startComplement();
    }

    while(true)
    {
	    // Special case handling of the most common cases for speed
	    if(_re_simple_location.matcher(location_line).matches())
	    {
	        // e.g. "123..456"
	    	  String tmp[] = location_line.split("\\.\\.");
	    	  consumer.beginPair();
	    	  consumer.exactLoc(Integer.parseInt(tmp[0]) - 1);
	    	  consumer.exactLoc(Integer.parseInt(tmp[1]));
	    	  consumer.endPair();
	        break;
	    }
	    //"bond\([<>]?\d+\)
	    if(_solo_bond.matcher(location_line).find())
	    {
	        // e.g. bond(196)
	        // e.g. join(bond(284),bond(305),bond(309),bond(305))
	        logger.warn("Dropping bond qualifier in feature location");
	        location_line = _solo_bond.matcher(location_line).replaceAll("$1");
	    }
	    if(_re_simple_compound.matcher(location_line).matches())
	    {
	        // e.g. join(123..456,480..500)
	        int i = location_line.indexOf("(");
	        // cur_feature.location_operator = location_line[:i]
	        // we can split on the comma because these are simple locations
	        this.consumer.startCompoundLocation(Util.i(location_line,0,i));
	        for(String part : Util.i(location_line,i + 1,-1).split(","))
	        {
	        	String tmp[] = part.split("\\.\\.");
	      	  consumer.beginPair();
	      	  consumer.exactLoc(Integer.parseInt(tmp[0]) - 1);
	      	  consumer.exactLoc(Integer.parseInt(tmp[1]));
	      	  consumer.endPair();
	        }
	        this.consumer.endCompoundLocation();
	        // s = cur_feature.sub_features[0].location.start
	        // e = cur_feature.sub_features[-1].location.end
	        // cur_feature.location = SeqFeature.FeatureLocation(s,e, strand)
	        // TODO - Remove use of sub_features
	        //We do not reverse the sub locations in the consumer
	        // if(strand == -1)
	        //    cur_feature.location = SeqFeature.CompoundLocation([f.location for f in sub_features[::-1]], operator=location_line[:i])
	        //else
	        //    cur_feature.location = SeqFeature.CompoundLocation([f.location for f in sub_features], operator=location_line[:i])
	        break;
	    }
	
	    // Handle the general case with more complex regular expressions
	    if(_re_complex_location.matcher(location_line).matches())
	    {
	        // e.g. "AL121804.2:41..610"
	    	  boolean isLocRef = false;
	        if(location_line.indexOf(":") != -1)
	        {
	        	String tmp[] = location_line.split(":");
	        	consumer.startLocationRef(tmp[0]);
	        	location_line = tmp[1];
	        	isLocRef = true;
	 
	        }
	        feedLoc(location_line);
	        if(isLocRef)
	        	consumer.endLocationRef();
	        break;
	    }
	
	    if(_re_complex_compound.matcher(location_line).matches())
	    {
	        int i = location_line.indexOf("(");
	        // cur_feature.location_operator = location_line[:i]
	        // Can't split on the comma because of positions like one-of(1,2,3)
	        for(String part : splitCompoundLoc(Util.i(location_line,i + 1,-1)))
	        {
	        	  int part_strand = strand;
	            if(part.startsWith("complement("))
	            {
	                assert part.endsWith(")");
	                part = Util.i(part,11,-1);
	                if(strand != -1)
	                	throw new ParseException("Illegal double complement detected");
	                part_strand = -1;
	                consumer.startComplement();                
	            }
	            String ref = null;
	            if(part.indexOf(":") != -1)
	            {
	            	String tmp[] = part.split(":");
	            	ref = tmp[0];
	            	part = tmp[1];
	              this.consumer.startLocationRef(ref);
	            }
	            feedLoc(part);
	            if(ref != null)
	            	this.consumer.endLocationRef();
	            if(part_strand == -1)
	            	consumer.endComplement();
	        }
	        
	        break;
	    }
	    logger.warn("Couldn't parse feature location: " + location_line);
	    break;
    }
    if(strand == -1)
    	consumer.endComplement();
    consumer.endLocation();
    // Not recognised
    if(location_line.indexOf("order") != -1 && location_line.indexOf("join") != -1)
        // See Bug 3197
        throw new ParseException("Combinations of 'join' and 'order' within the same location (nested operators) are illegal:\n" + location_line);
    // This used to be an error....    
	}
}
