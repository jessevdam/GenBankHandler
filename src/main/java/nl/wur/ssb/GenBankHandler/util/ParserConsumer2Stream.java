package nl.wur.ssb.GenBankHandler.util;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import nl.wur.ssb.GenBankHandler.data.QualifierValue;
import nl.wur.ssb.GenBankHandler.data.ResidueType;
import nl.wur.ssb.GenBankHandler.data.StrandMultiplicity;
import nl.wur.ssb.GenBankHandler.data.StrandType;
import nl.wur.ssb.GenBankHandler.parser.ParserConsumer2;

import org.apache.commons.lang.StringUtils;

//(public\svoid\s([a-zA-Z0-9_]*)\(([^\)]*)\)(\sthrows Exception)?\s*\{)[^\}]*}
//\1\n     writeln("\2:" + \3);\n  }

public class ParserConsumer2Stream implements ParserConsumer2
{
	private Writer out;
	
	public ParserConsumer2Stream(OutputStream output)
	{
		out = new OutputStreamWriter(new BufferedOutputStream(output));
	}
	
	public ParserConsumer2Stream(Writer output)
	{
		out = output;
	}
	
	private void writeln(String line) throws Exception
	{
  	this.out.write(line + "\n");
	  this.out.flush();
	}

	public void locus(String locus) throws Exception
	{
     writeln("locus: " + locus);
  }

	public void size(int size) throws Exception
	{
     writeln("size: " + size);
  }

	public void residue_type(String type) throws Exception
	{
     writeln("residue_type: " + type);
  }

  public void date(Date date,String comment) throws Exception
	{
     writeln("date: date = " + date + " comment = " + comment);
  }

	public void data_file_division(String division) throws Exception
	{
     writeln("data_file_division: " + division);
  }

	public void sequence(String sequence) throws Exception
	{
     writeln("sequence: " + sequence);
  }

	public void contigLocation(String contigLocation) throws Exception
	{
     writeln("contigLocation: " + contigLocation);
  }

	public void baseCount(HashMap<String, Integer> baseCounts) throws Exception
	{
		String temp = "";
		for(String base : baseCounts.keySet())
		{
		  temp += "[" + base + " = " + baseCounts.get(base) + "] ";
		}
		writeln("baseCount: " + temp);
	}
	
	public void originName(String name) throws Exception
	{
     writeln("originName: " + name);
  }

	public void wgs(String wgs) throws Exception
	{
     writeln("wgs: " + wgs);
  }

	public void addWgsScaffold(String scaffold) throws Exception
	{
     writeln("addWgsScaffold: " + scaffold);
  }

	public void keywords(ArrayList<String> keywords) throws Exception
	{
		writeln("keywords: " + StringUtils.join(keywords,","));		
	}

	public void accession(String val,int version) throws Exception
	{
     writeln("accession: " + val + " version = " + version);
  }

	public void versionSuffix(int suffixVersion) throws Exception
	{
     writeln("versionSuffix: " + suffixVersion);
  }

	public void gi(String gi) throws Exception
	{
     writeln("gi: " + gi);
  }

	public void definition(String val) throws Exception
	{
     writeln("definition: " + val);
  }

	public void nid(String val) throws Exception
	{
     writeln("nid: " + val);
  }

	public void pid(String val) throws Exception
	{
     writeln("pid: " + val);
  }

	public void db_source(String val) throws Exception
	{
     writeln("db_source: " + val);
  }

	public void segment(String val) throws Exception
	{
     writeln("segment: " + val);
  }

	public void source(String val) throws Exception
	{
     writeln("source: " + val);
  }

	public void consrtm(String val) throws Exception
	{
     writeln("consrtm: " + val);
  }

	public void dblink(String db, String identifier) throws Exception
	{
     writeln("dblink:  db = " + db + " iden = " + identifier);
  }

	public void remark(String val) throws Exception
	{
     writeln("remark: " + val);
  }

	public void taxonomy(ArrayList<String> tax) throws Exception
	{
     writeln("taxonomy: " + StringUtils.join(tax,","));
  }

	public void comment(String comment) throws Exception
	{
     writeln("comment: " + comment);
  }

	public void organism(String org) throws Exception
	{
     writeln("organism: " + org);
  }

	public void reference_num(int refNum) throws Exception
	{
     writeln("reference_num: " + refNum);
  }

	public void refereceLocation(String type, ArrayList<Integer> start,ArrayList<Integer> end) throws Exception
	{
		 String tmp = "";
		 for(int i = 0;i < start.size();i++)
		 {
			 tmp += "(" + start.get(i) + " to " + end.get(i) + "),";
		 }
     writeln("refereceLocation:  type = " + type + " positions = " + Util.i(tmp,0,-1));
  }

	public void title(String val) throws Exception
	{
     writeln("title: " + val);
  }

	public void journal(String val) throws Exception
	{
     writeln("journal: " + val);
  }

	public void crossRef(String db,String id) throws Exception
	{
     writeln("crossRef: db = " + db + " id = " + id);
  }

	public void authors(String val) throws Exception
	{
     writeln("authors: " + val);
  }

	public void startFeatureTable() throws Exception
	{
     writeln("startFeatureTable");
  }

	public void startFeature(String key) throws Exception
	{
     writeln("startFeature: " + key);
  }

	public void startLocation() throws Exception
	{
     writeln("startLocation");
  }

	public void endLocation() throws Exception
	{
     writeln("endLocation");
  }

	public void startCompoundLocation(String command) throws Exception
	{
     writeln("startCompoundLocation: " + command);
  }

	public void endCompoundLocation() throws Exception
	{
     writeln("endCompoundLocation");
  }

	public void startComplement() throws Exception
	{
     writeln("startComplement");
  }

	public void endComplement() throws Exception
	{
     writeln("endComplement");
  }

	public void startSoloLoc() throws Exception
	{
     writeln("startSoloLoc");
  }
 
	public void endSoloLoc() throws Exception
	{
     writeln("endSoloLoc");
  }

	public void beginPair() throws Exception
	{
     writeln("beginPair");
  }

	public void endPair() throws Exception
	{
     writeln("endPair");
  }

	public void startLocationRef(String ref) throws Exception
	{
		writeln("startLocationRef: " + ref);
	}

	public void endLocationRef() throws Exception
	{
		writeln("endLocationRef");
	}	

	public void exactLoc(int pos) throws Exception
	{
     writeln("exactLoc: " + pos);
  }

	public void inbetweenLoc(int pos) throws Exception
	{
     writeln("inbetweenLoc: " + pos);
  }

	public void beforePosition(int pos) throws Exception
	{
     writeln("beforePosition: " + pos);
  }

	public void afterPosition(int pos) throws Exception
	{
     writeln("afterPosition: " + pos);
  }

	public void withinPosition(int def, int start, int end) throws Exception
	{
     writeln("withinPosition: def = " + def + " start = " + start + " end = " + end);
  }

	public void oneOf(int def, ArrayList<Integer> positions) throws Exception
	{
     writeln("oneOf: def = " + def + " posisitons = (" + StringUtils.join(positions,",") + ")");
  }

	public void featureQualifier(String key, QualifierValue value) throws Exception
	{
     writeln("featureQualifier: " + key + " = " + value);
  }

	public void endFeature() throws Exception
	{
     writeln("endFeature");
  }

	public void endFeatureTable() throws Exception
	{
     writeln("endFeatureTable");
  }

	public void recordEnd() throws Exception
	{
     writeln("recordEnd");
  }

	public void residueType(ResidueType type) throws Exception
	{
		writeln("residueType: " + type);		
	}

	public void strandType(StrandType type) throws Exception
	{	
		writeln("strandType: " + type);				
	}

	public void circular() throws Exception
	{
		writeln("circular");			
	}

	public void taxDivision(String division) throws Exception
	{
		writeln("division: " + division);		
	}

	public void organelle(String organelle) throws Exception
	{
		writeln("organelle: " + organelle);		
	}

	public void strandMultiplicity(StrandMultiplicity multiplicity) throws Exception
	{
		writeln("strand multiplicity: " + multiplicity);				
	}
}
