package lexent.resource;

/**
 * A general interface for knowledge resources.
 * 
 * @author Omer Levy
 */
public interface LexicalResource {
	
	/**
	 * @param t A term from the text.
	 * @param h A term from the hypothesis.
	 * @return The probability that t entails h.
	 */
	public double probEntails(String t, String h);
	
}
