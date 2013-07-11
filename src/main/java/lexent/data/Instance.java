package lexent.data;

import java.util.List;


public class Instance {
	
	public String	topic;
	public Hypothesis	hypo;
	public List<Text>	texts;
	
	@Override
	public String toString() {
		String str =  "Topic: " + topic + "\n" + hypo.toString();
		for (Text text : texts) {
			str += text.toString();
		}		
		return str;			
	}
	
}
