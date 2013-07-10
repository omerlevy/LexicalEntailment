package test.lexent.data;

import java.util.List;

import lexent.data.Instance;
import lexent.data.Rte6DataReader;

import org.junit.Test;

public class Rte6DataReaderTest {
	
	@Test
	public void test() throws Exception {
		String path = ""; //TODO give test path here
		
		Rte6DataReader reader = new Rte6DataReader();
		List<Instance> instances = reader.read(path);
		
		for (Instance inst : instances) {
			System.out.print(inst);
		}
	}
	
}
