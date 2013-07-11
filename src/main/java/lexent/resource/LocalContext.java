package lexent.resource;

/**
 * Lexical context around a target word.
 * 
 * @author Oren Melamud
 */

public class LocalContext {
	
	
	public String contextX;	// space delimited words observed to the left to the target word
	public String contextY;	// space delimited words observed to the right to the target word

	
	/**
	 * 
	 * @param contextX	space delimited words observed to the left to the target word
	 * @param contextY	space delimited words observed to the right to the target word
	 */
	public LocalContext(String contextX, String contextY) {
		this.contextX = contextX;
		this.contextY = contextY;
	}

}
