package nl.wur.ssb.GenBankHandler.util;

import java.util.HashSet;
import java.util.Set;

public class Util
{	
	public static Set<String> set(String... values) 
	{
    return new HashSet<String>(java.util.Arrays.asList(values));
  }
	
	public static String[] rsplit(String in,String regex,int max)
	{
		String res[] = in.split(regex);
		if(res.length <= max + 1)
			return res;
		String toRet[] = new String[max + 1];
		StringBuffer buf = new StringBuffer();
		for(int i = 0;i < res.length - max;i++)
		{
			buf.append(res[i]);
		}
		toRet[0] = buf.toString();
		System.arraycopy(res,res.length - max,toRet,1,max);
		return toRet;
	}
	
	public static String rtrim(String s) {
    int i = s.length()-1;
    while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
        i--;
    }
    return i(s,0,i+1);
  }
	
	public static String rtrim(String s,char character) {
    int i = s.length()-1;
    while (i >= 0 && s.charAt(i) == character) {
        i--;
    }
    return i(s,0,i+1);
  }

	public static String ltrim(String s) {
    int i = 0;
    while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
        i++;
    }
    return i(s,i);
  }
	
	public static String ltrim(String s,char character) {
    int i = 0;
    while (i < s.length() && s.charAt(i) == character) {
        i++;
    }
    return i(s,i);
  }
	public static String i(String s,int begin,int end)
	{
		int len = s.length();
		if(begin < 0)
			begin += len;
		if(end < 0)
			end += len;
		if(begin < 0)
			begin = 0;
		if(end < 0)
			end = 0;
		if(begin >= len)
			return "";
		if(end > len)
			end = len;
		if(end <= begin)
			return "";
		return s.substring(begin,end);		
	}
	public static String i(String s,int begin)
	{
		return i(s,begin,s.length());
	}
}
