package lexent.data;

import java.util.LinkedList;
import java.util.List;

import lexent.resource.LocalContext;

public class Sentence {
	
	public List<Word> words;
	
	// all the nouns in the sentence (can't get finer grained context since original order of words in sentence is not maintained in the input data)
	public LocalContext context;  
	
	public Sentence(String sent) {
		fromString(sent);
	}
	
	public void fromString(String sent) {
		
		String contextStr = "";
		
		words = new LinkedList<Word>();
		String[] entries = sent.substring(1, sent.length()-1).split(",");
				
		for (String entry : entries) {
			String[] tokens = entry.trim().split(":");
			if (tokens.length >= 3) {			
				Word word = new Word(tokens[0], tokens[2]);
				words.add(word);
				if (word.pos.equals(POS.Noun)) {
					contextStr += word.lemma + " ";
				}
			}
		}
		
		context = new LocalContext(contextStr.trim(), contextStr.trim());
	}
	
	@Override public String toString() {
		String str = "";
		for (Word word : words) {
			str += word.toString() + " ";			
		}
		
		str += "Context: " + context;
		return str;
	}

}
