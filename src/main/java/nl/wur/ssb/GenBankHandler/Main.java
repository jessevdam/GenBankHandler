package nl.wur.ssb.GenBankHandler;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


public class Main 
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
	
	
	protected static final Logger log = Logger.getLogger("nl.wur.ssb.GenBankHandler.Main");
	
    public static void main( String[] args )
    {
    	  BasicConfigurator.configure();
        System.out.println("asfea\naa sefa".replaceAll(" ","a"));
        log.info("yep");
        System.out.println("test".substring(0,4));
        System.out.println("saef.afsefa".split("\\.")[1]);
        System.out.println("saef: afsefa".replaceFirst(": ",":"));
        System.out.println(StringUtils.join("j b c g\nx\tm".split("[\\s\\t\\n\\r\\f]")));
        System.out.println( _solo_bond.matcher("join(bond(284),bond(305),bond(309),bond(305))").replaceAll("$1"));
        System.out.println(StringUtils.join("test   bla    goed".split("[\\s\\t\\n\\r\\f]+"),","));
        System.out.println("123456789");
        System.out.println(String.format("%-4s%4s%19s","i","i","a") + "e");
       
        testRegExp();
        //assert(false);
    }
}
