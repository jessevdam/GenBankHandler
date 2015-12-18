package nl.wur.ssb.GenBankHandler.data;

import java.util.HashMap;

public enum ResidueType
{
	DNA("BP"), PROTEIN("AA"), RIBOSOMAL_CODE("RC");
	
	private String value;
	private static HashMap<String, ResidueType> enumMap;
	static
	{
		enumMap = new HashMap<String, ResidueType>();
		for (ResidueType item : ResidueType.values())
		{
			enumMap.put(item.value,item);
		}
	}
	public static ResidueType fromString(String name)
	{
		return enumMap.get(name);
	}	
	
	private ResidueType(String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value;
	}
}
