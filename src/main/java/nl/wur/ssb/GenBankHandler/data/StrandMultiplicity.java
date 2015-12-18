package nl.wur.ssb.GenBankHandler.data;

import java.util.HashMap;

public enum StrandMultiplicity
{
	SINGLE("ss"),
	DOUBLE("ds"),
	MULTI("ms"),
	NONE("");
	
	private String value;
	private static HashMap<String, StrandMultiplicity> enumMap;
	static
	{
		enumMap = new HashMap<String, StrandMultiplicity>();
		for (StrandMultiplicity item : StrandMultiplicity.values())
		{
			enumMap.put(item.value,item);
		}
	}
	public static StrandMultiplicity fromString(String name)
	{
		return enumMap.get(name);
	}	
	
	public static StrandMultiplicity fromStringChecked(String name) throws Exception
	{
		if(!enumMap.containsKey(name))
			throw new Exception("Unknown strand type: " + name);
		return enumMap.get(name);
	}	
	
	private StrandMultiplicity(String value)
	{
		this.value = value;
	}
	
  @Override
  public String toString() {
      return this.value;
  }
  
  public String toPrefix()
  {
  	if(this == NONE)
  		return "";
  	return this.toString() + "-";
  }
}
