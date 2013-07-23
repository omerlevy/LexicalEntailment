/**
 * 
 */
package lexent.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author user
 *
 */
public class Rte6ReportWriter {
	
	BufferedWriter resultsWriter;
	BufferedWriter ruleAppWriter;
	
	private static final String students = "Omer Levy\t036927671\tEden Shalom Erez\t037559200\tOren Melamud\t025688763\n";
	
	public Rte6ReportWriter(String resultPath, String ruleAppPath) throws IOException {
		resultsWriter = new BufferedWriter(new FileWriter(new File(resultPath)));
		resultsWriter.write(students);
		ruleAppWriter = new BufferedWriter(new FileWriter(new File(ruleAppPath)));
		ruleAppWriter.write(students);
	}
	
	public void close() throws IOException {
		resultsWriter.close();
		ruleAppWriter.close();
	}
	
	public void writeResult(String topicId, String hypoId, String docId, String sentId) throws IOException {
		resultsWriter.write(topicId + "\t" + hypoId + "\t" + docId + "\t" + sentId + "\n");
	}
	
	public void writeRuleApp(String fromWord, String toWord, String resourceName) throws IOException {
		ruleAppWriter.write(fromWord + "\t" + toWord + "\t" + resourceName + "\n");
	}

}
