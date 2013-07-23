package lexent.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lexent.data.Instance;
import lexent.data.Rte6DataReader;
import lexent.data.Rte6ReportWriter;
import lexent.data.Text;
import lexent.model.LexicalEntailmentModel;
import lexent.resource.LexicalResource;
import lexent.resource.context.ContextLexicalResource;
import lexent.resource.esa.EsaLexicalResource;
import lexent.resource.wordnet.WordnetLexicalResource;
import lexent.resource.yago.YagoLexicalResource;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 7) {
			System.exit(-1);
		}
		
		String trainPath = args[0];
		String testPath = args[1]; // TODO DEBUG
		String savePath = args[1];
		String contextModelPath = args[2];
		String esaModelPath = args[3];
		String yagoModelPath = args[4];
		String wordnetModelPath = args[5];
		String reportDir = args[6];
		
		Rte6DataReader reader = new Rte6DataReader();
		List<Instance> train = reader.read(trainPath);
		List<Instance> test = reader.read(testPath); // TODO DEBUG
		
		List<LexicalResource> resources = initResources(contextModelPath, esaModelPath, yagoModelPath, wordnetModelPath);
		LexicalEntailmentModel model = new LexicalEntailmentModel(resources);
		model.train(train, savePath);
		
		Rte6ReportWriter reportWriter = new Rte6ReportWriter(reportDir+"\\dev_results.txt", reportDir+"\\dev_rule_applications.txt");
		Results results = evaluate(model, train, reportWriter); // TODO DEBUG
		reportWriter.close();
		print(results); // TODO DEBUG
		
		reportWriter = new Rte6ReportWriter(reportDir+"\\test_results.txt", reportDir+"\\test_rule_applications.txt");
		evaluate(model, test, reportWriter); // We run this for reporting purposes. TODO DEBUG
		reportWriter.close();
		
	}
	
	private static List<LexicalResource> initResources(String contextModelPath, String esaModelPath, String yagoModelPath, String wordnetModelPath) throws IOException {
		List<LexicalResource> resources = new ArrayList<>();
		resources.add(new ContextLexicalResource(contextModelPath+"/context_wordtopic.txt", contextModelPath+"/context_slottopic.txt", contextModelPath+"/context_rules.txt", 250, 0.01, 10));
		resources.add(new EsaLexicalResource(esaModelPath));
		resources.add(new YagoLexicalResource(yagoModelPath));
		resources.add(new WordnetLexicalResource(wordnetModelPath)); 
		return resources;
	}
	
	private static Results evaluate(LexicalEntailmentModel model, List<Instance> test, Rte6ReportWriter reportWriter) throws Exception {
		Results results = new Results();
		for (Instance instance : test) {
			for (Text text : instance.texts) {
				if (text.entails != null) {
					results.update(text.entails, model.entails(text.sent, instance.hypo.sent, reportWriter));
				}
				if (reportWriter !=null) {
					reportWriter.writeResult(instance.topic, Integer.toString(instance.hypo.id), text.docId, Integer.toString(text.sentId));
				}
			}
		}
		return results;
	}
	
	private static void print(Results results) {
		System.out.println("Accuracy: \t" + results.accuracy());
		System.out.println("Precision: \t" + results.precision());
		System.out.println("Recall: \t" + results.recall());
		System.out.println("F1: \t\t" + results.f1());
	}
	
}
