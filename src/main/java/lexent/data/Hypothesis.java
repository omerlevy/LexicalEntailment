/**
 * 
 */
package lexent.data;

/**
 * @author user
 *
 */
public class Hypothesis {
	
	public int id;
	public Sentence sent;
	
	public Hypothesis(String hypoStr) {
		fromString(hypoStr);
	}
	
	public void fromString(String hypoStr) {
		String[] entries = hypoStr.split("\t");
		this.id = Integer.parseInt(entries[1]);
		this.sent = new Sentence(entries[2]);
	}
	
	@Override
	public String toString() {
		return "Hypo: Id=" + id + "\t" + sent.toString()+"\n";
	}
}
