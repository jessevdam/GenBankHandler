package nl.wur.ssb.GenBankHandler.data;

import java.util.HashMap;

public enum StrandType
{
	GENOMIC_DNA("genomic DNA"),
	GENOMIC_RNA("genomic RNA"),
	MRNA("mRNA"),
	TRNA("tRNA"),
	RRNA("rRNA"),
	OTHER("other"),
	RNA("RNA"),
	OTHER_RNA("other DNA"),
	TRANSCRIBED_RNA("transcribed RNA"),
	VIRAL_CRNA("viral cRNA"),
	UNASSIGNED("unassigned"),
	DNA("DNA"),
	UNASSIGNED_RNA("unassigned RNA"),
	SMALL_NUCLEAR_RNA("snRNA"),
	NONE("");
	
	private String value;
	private static HashMap<String, StrandType> enumMap;
	static
	{
		enumMap = new HashMap<String, StrandType>();
		for (StrandType item : StrandType.values())
		{
			enumMap.put(item.value,item);
		}
	}
	public static StrandType fromString(String name)
	{
		return enumMap.get(name);
	}	
	
	public static StrandType fromStringChecked(String name) throws Exception
	{
		if(!enumMap.containsKey(name))
			throw new Exception("Unknown strand type: " + name);
		return enumMap.get(name);
	}	
	
	private StrandType(String value)
	{
		this.value = value;
	}
	
  @Override
  public String toString() {
      return this.value;
  }
}
