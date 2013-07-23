/**
 * 
 */
package test.lexent.data;

import java.io.IOException;

import org.junit.Test;

import lexent.data.Rte6ReportWriter;

/**
 * @author user
 *
 */
public class Rte6ReportWriterTest {
	
	@Test
	public void test() throws IOException {
		Rte6ReportWriter writer = new Rte6ReportWriter("results-path", "rule-applications-path");
		for (int i=0; i<2; i++) {
			writer.writeResult("D0901", "592", "APW_ENG_20050321.0478", "2");
			writer.writeRuleApp("car", "vehicle", "WordNet");
		}
		writer.close();	
	}

}
