package nl.wur.ssb.GenBankHandler.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import nl.wur.ssb.GenBankHandler.data.ResidueType;

public interface ParserConsumer2
{
  //==Header line info elements==
	//Start of new record
	//The name specified after the LOCUS keyword in the GenBank
  //record. This may be the accession number, or a clone id or something else.
  public void locus(String locus) throws Exception;//start of Record
  //The size of the sequence within the record
  public void size(int size) throws Exception;
  //its either dna(4 codes), codon(64 codes) coding or protein(~20 codes)
  public void residueType(ResidueType type) throws Exception;
  //The type of residues making up the sequence in this
  //record. Normally something like RNA, DNA or PROTEIN, but may be as
  //esoteric as 'ss-RNA circular'.
  public void strandType(String type) throws Exception;
  //Is the strand circular
  public void circular() throws Exception;
  //EMBL only taxonomic division
  public void taxDivision(String division) throws Exception;
  //The date of submission of the record, in a form like '28-JUL-1998'
  public void date(Date date,String comment) throws Exception;
  //The division this record is stored under in
  //GenBank (ie. PLN -> plants; PRI -> humans, primates; BCT -> bacteria...)
  public void data_file_division(String division) throws Exception;

  //the sequence
  public void sequence(String sequence) throws Exception;
  
  //==additonal footer info==
  //optional extra information of this contig
  public void contigLocation(String contigLocation) throws Exception;
  //only present in GenBank and all are obsolete
  //A count for each base (example: BASE COUNT          102 a          119 c          131 g           98 t)
  public void baseCount(HashMap<String, Integer> baseCounts) throws Exception;
  //A string specifying info about the origin of the sequence.
  public void originName(String name) throws Exception;
  public void wgs(String wgs) throws Exception;
  public void addWgsScaffold(String scaffold) throws Exception;  
  
  //==additional header stuff==
  //'KEYWORDS'
  public void keywords(ArrayList<String> keywords) throws Exception;
  //also defined by 'ACCESSION'
  public void accession(String val,int suffixVersion) throws Exception;
  // The NCBI gi identifier for the record.
  public void gi(String gi) throws Exception;

  //short description of the record
  public void definition(String val) throws Exception;  
  //nucleotide identifier (obsolete)
  public void nid(String val) throws Exception;
  //protein identifier (obsolete)
  public void pid(String val) throws Exception;
  //source DNA sequence of a protein sequence
  public void db_source(String val) throws Exception;
  //If the record is one of a series, this is info about which
  //segment this record is (something like '1 of 6').
  public void segment(String val) throws Exception;
  //The source of material where the sequence came from.
  //GenBank only
  public void source(String val) throws Exception; 
  //link to external database
  public void dblink(String db,String identifier) throws Exception;
  //A listing of the taxonomic classification of the organism,
  // starting general and getting more specific. 
  public void taxonomy(ArrayList<String> tax) throws Exception;
  //A comment of for the current record
  public void comment(String comment) throws Exception;
  //The genus and species of the organism (ie. 'Homo sapiens')
  public void organism(String org) throws Exception;
  //EMBL only organelle
  public void organelle(String organelle) throws Exception;
  
  //==for each reference==
  //the number of the reference
  public void reference_num(int refNum) throws Exception;
  //each reference location is given
  public void refereceLocation(String type, ArrayList<Integer> start,ArrayList<Integer> end) throws Exception;
  //the title of the reference
  public void title(String val) throws Exception;
  //the journal of the reference
  public void journal(String val) throws Exception;
  //cross reference for reference
  //FOR genbank pubmed id=PUBMED & midline_id=MEDLINE
  public void crossRef(String db,String id) throws Exception;
  //The authors of the reference
  public void authors(String val) throws Exception;
  //A remark on the reference
  public void remark(String val) throws Exception;
  //the consortium to which the authers belong to
  public void consrtm(String val) throws Exception;
  
  //The start of the feature table
  public void startFeatureTable() throws Exception;
  //Start of a feature with given key
  public void startFeature(String key) throws Exception;
  //Start of location 
  public void startLocation() throws Exception;
  //End of location
  public void endLocation() throws Exception;
  //If location is collection of location then this called at the begin
  public void startCompoundLocation(String command) throws Exception;
  //If location is collection of location then this called at the end
  public void endCompoundLocation() throws Exception;
  //If location is complement this is called
  public void startComplement() throws Exception;
  public void endComplement() throws Exception;
  
  //location item is solo
  public void startSoloLoc() throws Exception;
  public void endSoloLoc() throws Exception;
  //location item is a pair
  public void beginPair() throws Exception;
  public void endPair() throws Exception;
  
  //optionally a location as a reference
  public void startLocationRef(String ref) throws Exception;
  public void endLocationRef() throws Exception;
  
  //Position with exact location
  public void exactLoc(int pos) throws Exception;
  //A inbetween to nucleotides (for example cutting site of protease) pos is before cut site
  public void inbetweenLoc(int pos) throws Exception;
  //Before the given position
  public void beforePosition(int pos) throws Exception;
  //after the given position
  public void afterPosition(int pos) throws Exception;
  //somewhere between the given start and end position
  //def = single position representation
  public void withinPosition(int def,int start,int end) throws Exception;
  //oneOf the given positions
  //def = single position representation
  public void oneOf(int def,ArrayList<Integer> positions) throws Exception;
  //a qualifier
  public void featureQualifier(String key,String value) throws Exception; 
  //end of a feature
  public void endFeature() throws Exception;
  //end of the feature table
  public void endFeatureTable() throws Exception;
  //end of a record
  public void recordEnd() throws Exception;
}

