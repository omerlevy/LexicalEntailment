/**
 * 
 */
package lexent.data;

/**
 * @author user
 *
 */
public class Text {
	
	public String docId;
	public int	sentId;
	public Sentence sent;
	
	public Boolean entails;	// Null if not annotated
	
	public Text(String textStr) {
		fromString(textStr);
	}
	

	public void fromString(String textStr) {
		String[] entries = textStr.split("\t");
		this.docId = entries[1];
		this.sentId = Integer.parseInt(entries[2]);
		this.sent = new Sentence(entries[3]);
		if (entries.length > 4) {
			this.entails = new Boolean(entries[4].equals("1")); 
		} else {
			this.entails = null;
		}
	}
	
	@Override
	public String toString() {
		return "Sent: docId=" + docId + " sentId=" + sentId + "\t" + sent.toString()+ "\tentails=" + entails + "\n";
	}
}
