package lexent.resource;

/**
 * Lexical context around a target word.
 * 
 * @author Oren Melamud
 */

public class LocalContext {
	
	
	private String contextX;	// space delimited words observed to the left to the target word
	private String contextY;	// space delimited words observed to the right to the target word

	public String getContextX() {
		return contextX;
	}

	public void setContextX(String contextX) {
		this.contextX = contextX;
	}

	public String getContextY() {
		return contextY;
	}

	public void setContextY(String contextY) {
		this.contextY = contextY;
	}

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
