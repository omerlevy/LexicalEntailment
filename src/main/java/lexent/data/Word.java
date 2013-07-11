/**
 * 
 */
package lexent.data;

import lexent.resource.LocalContext;

/**
 * @author user
 * 
 */
public class Word {

	public String lemma;
	public POS pos;
	
	public Word(String posStr, String lemma) {
		this.lemma = lemma;
		this.pos = POS.fromString(posStr);
	}

	@Override
	public String toString() {
		return pos + ":" + lemma;
	}

}
