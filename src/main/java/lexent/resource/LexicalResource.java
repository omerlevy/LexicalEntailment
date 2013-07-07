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
	 * @param context	terms observed around the target term t
	 * @return The probability that t entails h, or -1 if the resource cannot provide an estimate.
	 */
	public double probEntails(String t, String h, LocalContext context);
	
}
