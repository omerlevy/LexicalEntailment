package lexent.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Rte6DataReader {
	
	public List<Instance> read(String path) throws IOException {
		
		List<Instance> instances = new LinkedList<Instance>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		String line;
		String curTopic = "";
		Instance curInstance = null;
		while ((line = reader.readLine()) != null) {
			
			if (line.startsWith("Topic:")) {
				curTopic = line.split("\t")[1];
				continue;
			}
			
			if (line.startsWith("Hypo:")) {
				if (curInstance != null) {
					instances.add(curInstance);
				}
				curInstance = new Instance();
				curInstance.topic = curTopic;
				curInstance.hypo = new Hypothesis(line);
				curInstance.sents = new LinkedList<Text>();
				continue;
			}
			
			if (line.startsWith("Sent:")) {				
				Text text = new Text(line);
				curInstance.sents.add(text);
			}			
		}
		
		if (curInstance != null) {
			instances.add(curInstance);
		}
		
		reader.close();
		
		return instances;
	}
	
	public static void unitTest(String path) throws IOException {
		
		Rte6DataReader reader = new Rte6DataReader();
		List<Instance> instances = reader.read(path);
		
		for (Instance inst : instances) {
			System.out.print(inst);
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		unitTest(args[0]);
	}
	
}
