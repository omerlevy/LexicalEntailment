package lexent.resource.yago;

import java.util.ArrayList;


import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.tdb.TDBFactory;

import lexent.resource.LexicalResource;
import lexent.resource.LocalContext;

public class YagoLexicalResource implements LexicalResource {
	
	public double probEntails(String t, String h, LocalContext context) {
		
		String directory = "\\\\qa-srv\\E\\cygwin\\home\\eden\\yago2core_20110315_jena\\yago2core_20110315_jena\\" ;
        
    	Dataset dataset = TDBFactory.createDataset(directory) ;
        
          
        //ArrayList<String>  array = NLPAPI.GetResultsFromRight("event", dataset);
        ArrayList<String> arrayLeft = GetResultsFromLeft(t, dataset);
        for (String string : arrayLeft) {
			if(string.compareTo(h)==0)
			{
				dataset.close();
				return 1;
			}
			
		}
        ArrayList<String> arrayRight = GetResultsFromRight(h, dataset);
        for (String string : arrayLeft) {
        	for (String string2 : arrayRight) {
    			if(string.compareTo(string2)==0)
    			{
    				dataset.close();
    				return 0.5;
    			}
    		}
		}
        //GetResultsFromQuery(dataset,"<http://www.mpii.de/yago/resource/Albert_Einstein>","","\"Albert Einstein\"");
        dataset.close();
        
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static  ArrayList<String>  GetResultsFromLeft(String strName, Dataset dataset)
	{
		ArrayList<String> arrEntityNames = GetMainEntityNames(dataset, strName);
		ArrayList<String> arrLeftFirst = GetResultsFromLeftFirst(arrEntityNames, dataset);
		ArrayList<String> arrLeftNext = GetResultsFromLeftNext(arrEntityNames, dataset);
		ArrayList<String> AddArrayList = AddArrayList(arrLeftFirst, arrLeftNext);
		return GetReadableNames(dataset,AddArrayList,false);
		
	}
	public static  ArrayList<String>  GetResultsFromRight(String strName, Dataset dataset)
	{
		ArrayList<String> arrEntityNames = GetMainEntityNames(dataset, strName);
		ArrayList<String> arrLeftFirst = GetResultsFromRightNext(arrEntityNames, dataset);
		ArrayList<String> arrLeftNext = GetResultsFromRightLast(arrEntityNames, dataset);
		ArrayList<String> AddArrayList = AddArrayList(arrLeftFirst, arrLeftNext);
		return GetReadableNames(dataset,AddArrayList, false);
	}

	private static String GetReadableString(String str)
	{
		str = str.replace("_", " ").replace("\"", "");
		str = str.replace("wikicategory ", "");
		str = str.replace("geoclass ", "");
		return str;
	}
	public static ArrayList<String> GetReadableNames(Dataset dataset, ArrayList<String> strResults, boolean IsAllExpended)
	{
		ArrayList<String> arrResult = new ArrayList<String>();
		for (String strResult : strResults) {
			boolean IsExpended = IsAllExpended;
			if(strResult.startsWith("http"))
			{
				String strFromUrl = ReturnTheLastStringFromPath(strResult);
				strFromUrl = GetReadableString(strFromUrl);
				if(strFromUrl.startsWith("wordnet"))
				{
					IsExpended = true;
				}
				else
				{
					if(arrResult.contains(strFromUrl) == false)
						arrResult.add(strFromUrl);
				}
			}
			//System.out.println(strResult);
			if(strResult.indexOf("\"") < 0 && strResult.indexOf("`") < 0 && IsExpended)
			{
				ArrayList<String> MainEntityResult = GetMainEntityNames(dataset, strResult);
				for (String string : MainEntityResult) {
					String strFromUrl = string;
					if(strFromUrl.startsWith("http"))
					{
						strFromUrl = ReturnTheLastStringFromPath(strFromUrl);
					}
					strFromUrl = GetReadableString(strFromUrl);
					if(arrResult.contains(strFromUrl) == false && strFromUrl.startsWith("wordnet") == false )
						arrResult.add(strFromUrl);
				}
			}
		}
		
		return arrResult;
		
	}
	
	public static ArrayList<String>  GetMainEntityNames( Dataset dataset, String strName)
	{
		strName = GetCurrectFormatForQuery(strName);
		ArrayList<String> arrResult = new ArrayList<String>();
		ArrayList<String> returnResult = new ArrayList<String>();
		ArrayList<String> arrResulthasPreferredNameFromLeft = GetResultsFromQuery(dataset,strName,"<http://www.mpii.de/yago/resource/hasPreferredName>","");
		ArrayList<String> arrResulthasPreferredNameFromRight = GetResultsFromQuery(dataset,"","<http://www.mpii.de/yago/resource/hasPreferredName>",strName);
		ArrayList<String> arrResulthasPreferredMeaningFromLeft = GetResultsFromQuery(dataset,strName,"<http://www.mpii.de/yago/resource/hasPreferredMeaning>","");
		ArrayList<String> arrResulthasPreferredMeaningFromRight = GetResultsFromQuery(dataset,"","<http://www.mpii.de/yago/resource/hasPreferredMeaning>",strName);
		arrResult = AddArrayList(arrResulthasPreferredNameFromLeft,arrResult);
		arrResult = AddArrayList(arrResulthasPreferredNameFromRight,arrResult);
		arrResult = AddArrayList(arrResulthasPreferredMeaningFromLeft,arrResult);
		arrResult = AddArrayList(arrResulthasPreferredMeaningFromRight,arrResult);
		
		returnResult = AddArrayList(arrResult,returnResult);
		for (String string : arrResult) {
			strName = string;
			strName = GetCurrectFormatForQuery(strName);
			ArrayList<String> tmpResult = new ArrayList<String>();
			ArrayList<String> tmpResulthasPreferredNameFromLeft = GetResultsFromQuery(dataset,strName,"<http://www.mpii.de/yago/resource/hasPreferredName>","");
			ArrayList<String> tmpResulthasPreferredNameFromRight = GetResultsFromQuery(dataset,"","<http://www.mpii.de/yago/resource/hasPreferredName>",strName);
			ArrayList<String> tmpResulthasPreferredMeaningFromLeft = GetResultsFromQuery(dataset,strName,"<http://www.mpii.de/yago/resource/hasPreferredMeaning>","");
			ArrayList<String> tmpResulthasPreferredMeaningFromRight = GetResultsFromQuery(dataset,"","<http://www.mpii.de/yago/resource/hasPreferredMeaning>",strName);
			tmpResult = AddArrayList(tmpResulthasPreferredNameFromLeft,tmpResult);
			tmpResult = AddArrayList(tmpResulthasPreferredNameFromRight,tmpResult);
			tmpResult = AddArrayList(tmpResulthasPreferredMeaningFromLeft,tmpResult);
			tmpResult = AddArrayList(tmpResulthasPreferredMeaningFromRight,tmpResult);
			returnResult = AddArrayList(tmpResult,returnResult);
		}
		return returnResult;
		
	}
	
	public static  ArrayList<String>  GetResultsFromLeftFirst(ArrayList<String> array, Dataset dataset)
	{
		ArrayList<String> arrResult = new ArrayList<String>();
		String strS = "";
		String strP = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
		String strO = "";
		for (String string : array) {
			strS = GetCurrectFormatForQuery(string);
			ArrayList<String> tmpResult = GetResultsFromQuery(dataset,strS,strP,strO);
			arrResult = AddArrayList(tmpResult,arrResult);
		}
		return arrResult;
	}
	public static  ArrayList<String>  GetResultsFromRightLast(ArrayList<String> array, Dataset dataset)
	{
		ArrayList<String> arrResult = new ArrayList<String>();
		String strS = "";
		String strP = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
		String strO = "";
		for (String string : array) {
			strO = GetCurrectFormatForQuery(string);
			ArrayList<String> tmpResult = GetResultsFromQuery(dataset,strS,strP,strO);
			arrResult = AddArrayList(tmpResult,arrResult);
		}
		return arrResult;
	}
	public static  ArrayList<String>  GetResultsFromLeftNext(ArrayList<String> array, Dataset dataset)
	{
		ArrayList<String> arrResult = new ArrayList<String>();
		String strS = "";
		String strP = "<http://www.w3.org/2000/01/rdf-schema#subClassOf>";
		String strO = "";
		for (String string : array) {
			strS = GetCurrectFormatForQuery(string);
			ArrayList<String> tmpResult = GetResultsFromQuery(dataset,strS,strP,strO);
			arrResult = AddArrayList(tmpResult,arrResult);
		}
		return arrResult;
	}
	public static  ArrayList<String>  GetResultsFromRightNext(ArrayList<String> array, Dataset dataset)
	{
		ArrayList<String> arrResult = new ArrayList<String>();
		String strS = "";
		String strP = "<http://www.w3.org/2000/01/rdf-schema#subClassOf>";
		String strO = "";
		for (String string : array) {
			strO = GetCurrectFormatForQuery(string);
			ArrayList<String> tmpResult = GetResultsFromQuery(dataset,strS,strP,strO);
			arrResult = AddArrayList(tmpResult,arrResult);
		}
		return arrResult;
	}
	
	private static ArrayList<String>  GetResultsFromQuery(Dataset dataset, String s, String p, String o)
	{
		ArrayList<String> arrResult = new ArrayList<String>();
		String sparqlQueryString2 = "SELECT distinct ?miss  WHERE { " ;
		if(s.length() > 0)
			sparqlQueryString2 += " " + s + " ";
		else
			sparqlQueryString2 += " ?miss ";
		if(p.length() > 0)
			sparqlQueryString2 += " " + p + " ";
		else
			sparqlQueryString2 += " ?miss ";
		if(o.length() > 0)
			sparqlQueryString2 += " " + o + " ";
		else
			sparqlQueryString2 += " ?miss ";
		
		sparqlQueryString2 += "}";
		//System.out.println(sparqlQueryString2);
		
		Query query2 = QueryFactory.create(sparqlQueryString2) ;
        QueryExecution qexec2 = QueryExecutionFactory.create(query2, dataset) ;
        ResultSet results2 = qexec2.execSelect() ;
        for ( ; results2.hasNext() ; )
        {
            QuerySolution soln = results2.nextSolution() ;
            String strr = soln.get("miss").toString() ;
            //System.out.println(strr);
            arrResult.add(strr);
        }
        qexec2.close() ;
        return arrResult;
	        
	}
	public static ArrayList<String> AddArrayList(ArrayList<String> fromArray, ArrayList<String> toArray)
	{
		for (String string : fromArray) {
			if(toArray.contains(string) == false)
				toArray.add(string);
		}
		return toArray;
	}
	private static String GetCurrectFormatForQuery(String strResult){
		if(strResult.startsWith("\"") || strResult.startsWith("<"))
			return strResult;
		if(strResult.startsWith("http"))
			return "<" + strResult + ">";
		return "\"" + strResult + "\"";
	}
	private static String ReturnTheLastStringFromPath(String strPath)
	{
		int index = strPath.indexOf("/");
		while(index >= 0)
		{
			strPath =strPath.substring(index+1);
			index = strPath.indexOf("/");
		}
		return strPath;//ReturnReadableString(strPath);
	}
}
