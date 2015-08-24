package nl.wur.ssb.GenBankHandler.data;

import java.util.ArrayList;
import java.util.HashMap;

import nl.wur.ssb.GenBankHandler.data.location.Accession;

public class Record
{
  //The name specified after the LOCUS keyword in the GenBank
  //record. This may be the accession number, or a clone id or something else.
	public String locus;
  //The size of the sequence within the record
	public int size = -1;

  //its either dna(4 codes), codon(64 codes) coding or protein(~20 codes)
	public ResidueType residueType;
  //The type of residues making up the sequence in this
  //record. Normally something like RNA, DNA or PROTEIN, but may be as
  //esoteric as 'ss-RNA circular'.
	public String strandType;
	//is this sequence circular
  public boolean circular;	
	//The date of submission of the record, in a form like '28-JUL-1998'
	public ArrayList<DateVersion> dates = new ArrayList<DateVersion>();
	//EMBL only taxonomic division
	public String taxDivision;
  //The division this record is stored under in
  //GenBank (ie. PLN -> plants; PRI -> humans, primates; BCT -> bacteria...)
	public String data_file_division;
  //the sequence
	public String sequence;
  //optional extra information of this contig
	//TODO find its definition
	public String contigLocation;
  //should be obsolete
  //A count for each base (example: BASE COUNT          102 a          119 c          131 g           98 t)
	public HashMap<String, Integer> baseCount;
  //A string specifying info about the origin of the sequence.
	//TODO obsolete?
	public String originName;
	//TODO obsolete?
	public String wgs;
	//TODO obsolete?
	public String wgsScaffold;
	//All the keywords of the record
	public ArrayList<String> keywords;
	//List of all accession number including their sub versions
	public ArrayList<Accession> accessions = new ArrayList<Accession>();
	 // The NCBI gi identifier for the record.
	public String gi;
  //short description of the record
	public String definition;
	//nucleotide identifier 
	//TODO obsolete
	public String nid;
	//protein identifier 
	//TODO obsolete
	public String pid;
  //source DNA sequence of a protein sequence
	public String db_source;
  //If the record is one of a series, this is info about which
  //segment this record is (something like '1 of 6').
	public String segment;
	//The source of the physical material where the sequence was extracted from.
	public String source;
  //link to external databases and/or projects
	public ArrayList<CrossRef> dblinks;
  //A listing of the taxonomic classification of the organism,
  // starting general and getting more specific. 
	public ArrayList<String> taxonomy;
  //A comment of for the current record
	public String comment;
  //Textual representation of the genus and species of the organism (ie. 'Homo sapiens')
	public String organism;
	//EMBL only organelle
	public String organelle;
  //List of all references attached to the record
	public ArrayList<Reference> references = new ArrayList<Reference>();

	public ArrayList<Feature> features;
	
	public Record()
	{
		
	}
}
