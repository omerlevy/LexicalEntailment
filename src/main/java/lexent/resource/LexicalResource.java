package lexent.resource;

import lexent.data.Word;

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
	 * @return The probability that t entails h.
	 * @throws IllegalArgumentException If the resource cannot provide an estimate.
	 */
	public double probEntails(Word t, Word h, LocalContext context) throws LexicalResourceException;
	
	/**
	 * Get the name that identifies this resource (e.g. WordNet)
	 * @return
	 */
	public String getResourceName();
	
}
