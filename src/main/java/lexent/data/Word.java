/**
 * 
 */
package lexent.data;

/**
 * @author user
 *
 */
public class Word {
	
	public String	lemma;
	public POS	pos;
	
	Word(String posStr, String lemma) {
		this.lemma = lemma;
		this.pos = POS.fromString(posStr);
	}
	
	@Override public String toString() {
		return pos + ":" + lemma;
	}

}
