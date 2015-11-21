package nl.wur.ssb.GenBankHandler.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

import nl.wur.ssb.GenBankHandler.data.location.Accession;
import nl.wur.ssb.GenBankHandler.data.location.AfterPosition;
import nl.wur.ssb.GenBankHandler.data.location.BeforePosition;
import nl.wur.ssb.GenBankHandler.data.location.ComplementLocation;
import nl.wur.ssb.GenBankHandler.data.location.CompoundLocation;
import nl.wur.ssb.GenBankHandler.data.location.ExactPosition;
import nl.wur.ssb.GenBankHandler.data.location.InBetweenPosition;
import nl.wur.ssb.GenBankHandler.data.location.Location;
import nl.wur.ssb.GenBankHandler.data.location.OneOfPosition;
import nl.wur.ssb.GenBankHandler.data.location.PairLocation;
import nl.wur.ssb.GenBankHandler.data.location.Position;
import nl.wur.ssb.GenBankHandler.data.location.RefLocation;
import nl.wur.ssb.GenBankHandler.data.location.ReferenceLocation;
import nl.wur.ssb.GenBankHandler.data.location.SoloLocation;
import nl.wur.ssb.GenBankHandler.data.location.WithinPosition;
import nl.wur.ssb.GenBankHandler.parser.ParseException;
import nl.wur.ssb.GenBankHandler.parser.ParserConsumer2;

public class RecordBuilder implements ParserConsumer2
{
	private Record rec; // current record
	private Reference curRef;
	private Feature curFeature;
	private Stack<Location> stack = new Stack<Location>();
	private ArrayList<Record> records = new ArrayList<Record>();
	private HashMap<String,Accession> accessions = new HashMap<String,Accession>();
	
	public void locus(String locus) throws Exception
	{
		if(rec != null)
			throw new ParseException("Previous record not closed");
		rec = new Record();
		rec.locus = locus;
	}
	
	// The size of the sequence within the record
	public void size(int size) throws Exception
	{
		if(rec.size != -1)
			throw new ParseException("record size can only be given once");
		rec.size = size;
	}
	
	// The date of submission of the record, in a form like '28-JUL-1998'
  public void date(Date date,String comment) throws Exception
	{
		rec.dates.add(new DateVersion(date,comment));
	}
	
	// The division this record is stored under in
	// GenBank (ie. PLN -> plants; PRI -> humans, primates; BCT -> bacteria...)
	public void data_file_division(String division) throws Exception
	{
		if(rec.data_file_division != null)
			throw new ParseException("data_file_division can only be given once");
		rec.data_file_division = division;
	}

	public void sequence(String sequence) throws Exception
	{
		if(rec.sequence != null)
			throw new ParseException("sequence can only be given once");
		rec.sequence = sequence;
		
	}

	public void contigLocation(String contigLocation) throws Exception
	{
		if(rec.contigLocation != null)
			throw new ParseException("contigLocation can only be given once");
		rec.contigLocation = contigLocation;		
	}
	
	public void baseCount(HashMap<String, Integer> baseCount) throws Exception
	{
		if(rec.baseCount != null)
			throw new ParseException("baseCount can only be given once");
		rec.baseCount = baseCount;			
	}

	public void originName(String originName) throws Exception
	{
		if(rec.originName != null)
			throw new ParseException("originName can only be given once");
		rec.originName = originName;				
	}

	public void wgs(String wgs) throws Exception
	{
		if(rec.wgs != null)
			throw new ParseException("wgs can only be given once");
		rec.wgs = wgs;			
	}

	public void addWgsScaffold(String scaffold) throws Exception
	{
		if(rec.wgsScaffold != null)
			throw new ParseException("wgsScaffold can only be given once");
		rec.wgsScaffold = scaffold;		
	}

	public void keywords(ArrayList<String> keywords) throws Exception
	{
		if(rec.keywords != null)
			throw new ParseException("keywords can only be given once");
		rec.keywords = keywords;	
	}

	public void accession(String accession,int version) throws Exception
	{
		Accession accessionObj = this.accessions.get(accession);
		if(accessionObj != null)
		{
			if(version != 0)
				accessionObj.setVersion(version);
		}
		else
		{
			accessionObj = new Accession(accession,version);
		  this.accessions.put(accession,accessionObj);
		  this.rec.accessions.add(accessionObj);
		}
	}

	public void versionSuffix(int suffixVersion) throws Exception
	{
		rec.accessions.get(rec.accessions.size() -1).setVersion(suffixVersion);		
	}

	public void gi(String gi) throws Exception
	{
		if(rec.gi != null)
			throw new ParseException("record can only have one gi");
		rec.gi = gi;		
	}

	public void definition(String definition) throws Exception
	{
		if(rec.definition != null)
			throw new ParseException("record can only have one definition");
		rec.definition = definition;			
	}

	public void nid(String nid) throws Exception
	{
		if(rec.nid != null)
			throw new ParseException("record can only have one nid");
		rec.nid = nid;				
	}

	public void pid(String pid) throws Exception
	{
		if(rec.pid != null)
			throw new ParseException("record can only have one pid");
		rec.pid = pid;		
	}

	public void db_source(String db_source) throws Exception
	{
		if(rec.db_source != null)
			throw new ParseException("record can only have one db_source");
		rec.db_source = db_source;		
	}

	public void segment(String segment) throws Exception
	{
		if(rec.segment != null)
			throw new ParseException("record can only have one segment");
		rec.segment = segment;		
	}

	public void source(String source) throws Exception
	{
		if(rec.source != null)
			throw new ParseException("record can only have one source");
		rec.source = source;			
	}

	public void dblink(String db, String identifier) throws Exception
	{
		//already filtered against duplicate
		if(rec.dblinks == null)
			rec.dblinks = new ArrayList<CrossRef>(); 
		rec.dblinks.add(new CrossRef(db,identifier));		
	}

	public void taxonomy(ArrayList<String> taxonomy) throws Exception
	{
		if(rec.taxonomy != null)
			throw new ParseException("record can only have one taxonomy set");
		rec.taxonomy = taxonomy;	
	}

	public void comment(String comment) throws Exception
	{
		if(rec.comment != null)
			throw new ParseException("record can only have one comment");
		rec.comment = comment;		
	}

	public void organism(String organism) throws Exception
	{
		if(rec.organism != null)
			throw new ParseException("record can only have one organism");
		rec.organism = organism;		
	}
	
	private void flushReference()
	{
		if(this.curRef != null)
		{
			this.rec.references.add(curRef);
			this.curRef = null;
		}
	}

	public void reference_num(int refNum) throws Exception
	{
		//duplicate already filtered
		this.flushReference();
		this.curRef = new Reference();
		this.curRef.id = refNum;		
	}

	public void refereceLocation(String type, ArrayList<Integer> start,ArrayList<Integer> end) throws Exception
	{
		if(this.curRef.locations != null)
			throw new ParseException("reference can only have one set of locations");
		this.curRef.locationRefType = type;
		this.curRef.locations = new ArrayList<RefLocation>();
		for(int i = 0;i < start.size();i++)
		{
		  this.curRef.locations.add(new RefLocation(start.get(i),end.get(i)));
		}
	}

	public void title(String title) throws Exception
	{
		if(this.curRef.title != null)
			throw new ParseException("reference can only have one title");
		this.curRef.title = title;		
	}

	public void journal(String journal) throws Exception
	{
		if(this.curRef.journal != null)
			throw new ParseException("reference can only have one journal");
		this.curRef.journal = journal;	
	}

	public void crossRef(String db,String id) throws Exception
	{
		this.curRef.crossRefs.add(new CrossRef(db,id));
	}

	public void authors(String authors) throws Exception
	{
		if(this.curRef.authors != null)
			throw new ParseException("reference can only have one set of authors");
		this.curRef.authors = authors;		
	}

	public void remark(String remark) throws Exception
	{
		if(this.curRef.remark != null)
			throw new ParseException("reference can only have one set of remark");
		this.curRef.remark = remark;			
	}

	public void consrtm(String consrtm) throws Exception
	{
		if(this.curRef.consrtm != null)
			throw new ParseException("reference can only have one set of remark");
		this.curRef.consrtm = consrtm;			
	}

	public void startFeatureTable() throws Exception
	{
		if(this.rec.features != null)
			throw new ParseException("record can only have one feature table");
		this.rec.features = new ArrayList<Feature>();	
	}

	public void startFeature(String key) throws Exception
	{
		if(curFeature != null)
			throw new ParseException("last feature not closed");
		curFeature = new Feature(key);
		this.rec.features.add(curFeature);
	}

	public void startLocation() throws Exception
	{
    if(this.curFeature == null)
    	throw new ParseException("location start before feature has started");
	}

	public void endLocation() throws Exception
	{
	  if(this.curFeature.location == null)
	  	throw new ParseException("feature should have location at the end");
	  if(stack.size() != 0)
	  	throw new ParseException("Stack != 0 at end op location parsing");
	}
	
	public void pushLocation(Location location) throws Exception
	{
		if(!stack.isEmpty())
			stack.peek().addLocation(location);
		stack.push(location);			
	}
	public void popLocation() throws Exception
	{
    if(stack.size() == 0)
    	throw new ParseException("location pop stack.size() == 0");
    this.curFeature.location = stack.pop();
	}

	public void startCompoundLocation(String command) throws Exception
	{
		pushLocation(new CompoundLocation(command));
	}

	public void endCompoundLocation() throws Exception
	{
		popLocation();
	}

	public void startComplement() throws Exception
	{
		pushLocation(new ComplementLocation());
	}

	public void endComplement() throws Exception
	{
		popLocation();
	}

	public void startSoloLoc() throws Exception
	{
		pushLocation(new SoloLocation());
	}

	public void endSoloLoc() throws Exception
	{
		popLocation();		
	}

	public void beginPair() throws Exception
	{
		pushLocation(new PairLocation());		
	}

	public void endPair() throws Exception
	{
		popLocation();		
	}

	public void startLocationRef(String ref) throws Exception
	{
		pushLocation(new ReferenceLocation(ref));		
		
	}

	public void endLocationRef() throws Exception
	{
		popLocation();			
	}
	
	private void pushPosition(Position pos) throws Exception
	{
		if(this.stack.size() == 0)
			throw new ParseException("Position outside of location");
		this.stack.peek().addPosition(pos);
	}
	
	public void exactLoc(int pos) throws Exception
	{
		pushPosition(new ExactPosition(pos));		
	}

	public void inbetweenLoc(int pos) throws Exception
	{
		pushLocation(new InBetweenPosition(pos));		
		popLocation();
	}

	public void beforePosition(int pos) throws Exception
	{
		pushPosition(new BeforePosition(pos));	
	}

	public void afterPosition(int pos) throws Exception
	{
		pushPosition(new AfterPosition(pos));			
	}

	public void withinPosition(int def, int start, int end) throws Exception
	{
		pushPosition(new WithinPosition(def,start,end));	
	}

	public void oneOf(int def, ArrayList<Integer> positions) throws Exception
	{
		pushPosition(new OneOfPosition(def,positions));	
		
	}

	public void featureQualifier(String key, String value) throws Exception
	{
		this.curFeature.qualifiers.add(new Qualifier(key,value));		
	}

	public void endFeature() throws Exception
	{
		if(this.curFeature == null)
			throw new ParseException("no open feature");
		this.curFeature = null;		
	}

	public void endFeatureTable() throws Exception
	{
		
	}

	public void recordEnd() throws Exception
	{
		this.flushReference();
		records.add(this.rec);
		this.curRef = null;
		this.rec = null;
	}
	public ArrayList<Record> getRecords()
	{
		return this.records;
	}
	
	public void residueType(ResidueType type) throws Exception
	{
		if(rec.residueType != null)
			throw new ParseException("residue_type can only be given once");
		rec.residueType = type;		
	}

	// The type of residues making up the sequence in this
	// record. Normally something like RNA, DNA or PROTEIN, but may be as
	// esoteric as 'ss-RNA circular'.
	public void strandType(String type) throws Exception
	{
		if(rec.strandType != null)
			throw new ParseException("residue_type can only be given once");
		rec.strandType = type;		
	}

	public void circular() throws Exception
	{
    rec.circular = true;
	}

	public void taxDivision(String division) throws Exception
	{
		if(rec.taxDivision != null)
			throw new ParseException("taxDivision can only be given once");
		rec.taxDivision = division;	
	}

	public void organelle(String organelle) throws Exception
	{
		if(rec.organelle != null)
			throw new ParseException("organelle can only be given once");
		rec.organelle = organelle;	
	}
}
