package nl.wur.ssb.GenBankHandler.data;

import java.util.ArrayList;

import nl.wur.ssb.GenBankHandler.data.location.RefLocation;

public class Reference
{
  //the number of the reference
	public int id = -1;
	//bases or residues or sites
	public String locationRefType;
  //set of location to which the reference applies to
	public ArrayList<RefLocation> locations;
  //the title of the reference
	public String title;
  //the journal of the reference
	public String journal;
  //cross references
	public ArrayList<CrossRef> crossRefs = new ArrayList<CrossRef>();
	//The authors of the reference
	public String authors;
  //A remark on the reference
	public String remark;
  //the consortium to which the authers belong to
	public String consrtm;
}
