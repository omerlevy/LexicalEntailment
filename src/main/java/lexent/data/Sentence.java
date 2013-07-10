package lexent.data;

import java.util.LinkedList;
import java.util.List;

public class Sentence {
	
	public List<Word> words;
	
	public Sentence(String sent) {
		fromString(sent);
	}
	
	public void fromString(String sent) {
		
		words = new LinkedList<Word>();
		String[] entries = sent.substring(1, sent.length()-1).split(",");
		for (String entry : entries) {
			String[] tokens = entry.trim().split(":");
			if (tokens.length >= 3) {			
				Word word = new Word(tokens[0], tokens[2]);
				words.add(word);
			}
		}
	}
	
	@Override public String toString() {
		String str = "";
		for (Word word : words) {
			str += word.toString() + " ";			
		}
		return str;
	}

}
