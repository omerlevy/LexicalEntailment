/**
 * 
 */
package lexent.data;

/**
 * @author user
 *
 */
public enum POS {
	
	Verb, Noun, Adjective, Adverb, Pronoun, Cardinality, Other, Na;
	
	static public POS fromString(String posStr) {
		
		switch (posStr) {
		case "v": return POS.Verb;
		case "n": return POS.Noun;
		case "a": return POS.Adjective;
		case "r": return POS.Adverb;
		case "p": return POS.Pronoun;
		case "car": return POS.Cardinality;
		case "o": return POS.Other;
		default: return POS.Na;
		
		}
	}

}
