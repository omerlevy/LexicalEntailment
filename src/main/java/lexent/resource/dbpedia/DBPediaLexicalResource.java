package lexent.resource.dbpedia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import lexent.resource.LexicalResource;
import lexent.resource.LocalContext;

public class DBPediaLexicalResource implements LexicalResource {
	
	IDictionary dict = null;
	public double probEntails(String t, String h, LocalContext context) {
		
		if(dict == null)
			dict = GetDictionary();
		
		ISynset first = getFirstISynsetFromWordNounLemma(dict,t);
		if(first == null)
			return -1;
		first = getFirstISynsetFromWordNounLemma(dict,h);
		if(first == null)
			return -1;
		
		if(AreInSameSynset(t,h))
			 return 1;
		 
		ArrayList<String> listh = getHypernymsFromLemma(dict,h);
		ArrayList<String> listt = getHypernymsFromLemma(dict,t);
		for (String string : listt) {
			if(h.compareTo(string) == 0)
				return 0.75;
		}
		for (String string : listh) {
			if(t.compareTo(string) == 0)
				return 0.5;
		}
		for (String string : listh) {
			for (String string2 : listt) {
				if(string.compareTo(string2) == 0)
					return 0.25;
			}
		}
		// TODO Auto-generated method stub
		return 0;
	}
	public boolean AreInSameSynset( String lemma1, String lemma2)
	{
		if(dict == null)
			dict = GetDictionary();
		
		ISynset first = getFirstISynsetFromWordNounLemma(dict,lemma1);
		ArrayList<String> lemmas = getHypernymsWordsFromISynset(first);
		for (String string : lemmas) {
			if(string.compareTo(lemma2)==0)
			{
				return true;
			}
		}
		return false;
	}
	public static IDictionary GetDictionary()
	{
		//String path = "/media/qa-srv/D/Data/RESOURCES/WordNet/3.0/dict.wn.orig";
		String path = "\\\\qa-srv\\Data\\RESOURCES\\WordNet\\3.0\\dict.wn.orig";
		
		//String path = "C:\\Users\\Ú„Ô\\Desktop\\Thesis\\dict.wn.orig";
		URL url = null;
		try {
			url = new URL("file", null , path);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("e.getMessage(): " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("url: " + url);
		// construct the dictionary object and open it
		IDictionary dict = new Dictionary(url);
		try {
			dict.open ();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dict;
	}
	
	public static ArrayList<String> getHypernymsFromLemma(
			IDictionary dict, String lemma) {
		ArrayList<String> allReturnHypernyms  = new ArrayList<String>();
		
		ISynset first = getFirstISynsetFromWordNounLemma(dict,lemma);
		 if(first==null)
			 return allReturnHypernyms;
		
		ArrayList<ISynset> arrReturnHypernyms  = new ArrayList<ISynset>();
		arrReturnHypernyms.add(first);
		boolean flagThereIsMoreHypernym = true;
		while(flagThereIsMoreHypernym) 
		{
			int preAllReturnSize = allReturnHypernyms.size();
			arrReturnHypernyms=getHypernymISynsetsFromSynset(dict,arrReturnHypernyms);
			
			for (ISynset synset : arrReturnHypernyms) {
				ArrayList<String> lemmas = getHypernymsWordsFromISynset(synset);
				for (String string : lemmas) {
					if(allReturnHypernyms.contains(string)==false)
					{
						allReturnHypernyms.add(string);
					}
				}
			}
			int postAllReturnSize = allReturnHypernyms.size();
			if(preAllReturnSize==postAllReturnSize)
				flagThereIsMoreHypernym=false;
			//System.out.println("allReturnHypernyms.size: " + allReturnHypernyms.size() );
		}
		
		return allReturnHypernyms;
	}
	private static ArrayList<ISynset> getHypernymISynsetsFromSynset(
			IDictionary dict, ArrayList<ISynset> arrSynsets) {
		ArrayList<ISynset> returnAllSynsets = new ArrayList<ISynset>();
		for (ISynset iSynset : arrSynsets) {
			ArrayList<ISynset> returnArr = getHypernymISynsetsFromSynset(dict,iSynset);
			for (ISynset iSynset2 : returnArr) {
				if(returnAllSynsets.contains(iSynset2) == false)
					returnAllSynsets.add(iSynset2);
			}
		}
		return returnAllSynsets;
	}

	public synchronized static  ISynset getFirstISynsetFromWordNounLemma(IDictionary dict,String lemma)
	{
		// get the synset
		 IIndexWord idxWord = dict.getIndexWord (lemma, POS.NOUN);
		 if(idxWord==null) return null;
		 
		 List<IWordID> arrWordIds = idxWord.getWordIDs ();
		 ISynset synset = null;
		 
		 if(arrWordIds.size()<=0) return null;
		 
		 IWordID wordID = arrWordIds.get (0); // 1st meaning
		 IWord word = dict. getWord ( wordID );
		 synset = word. getSynset ();
		 
		 return synset;
	}
	public static ArrayList<String> getHypernymsWordsFromISynset(ISynset synset)
	{
		ArrayList<String> arrReturnHypernyms = new ArrayList<String>();
		List <IWord > words ;
		words = synset.getWords();
		for(Iterator <IWord > i = words. iterator (); i. hasNext ();){
			 String nextLemma = i.next (). getLemma ();
			 if(arrReturnHypernyms.contains(nextLemma)==false)
				 arrReturnHypernyms.add(nextLemma);
		 }
		return arrReturnHypernyms;
	}
	
	public synchronized static ArrayList<ISynset> getHypernymISynsetsFromSynset ( IDictionary dict, ISynset synset){
		
		ArrayList<ISynset> arrReturnHypernyms = new ArrayList<ISynset>();	
		
		 // get the hypernyms
		 List <ISynsetID > hypernyms =
		 synset . getRelatedSynsets ( Pointer . HYPERNYM );
		
		 // print out each h y p e r n y m s id and synonyms
		 for( ISynsetID sid : hypernyms ){
			 ISynset currentSynset = dict. getSynset (sid);
			 if(arrReturnHypernyms.contains(currentSynset) == false)
				 arrReturnHypernyms.add(currentSynset);
		 }
		
		 return arrReturnHypernyms;
	}

}
