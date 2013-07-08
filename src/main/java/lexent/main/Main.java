package lexent.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lexent.data.Instance;
import lexent.data.Rte6DataReader;
import lexent.model.LexicalEntailmentModel;
import lexent.resource.LexicalResource;
import lexent.resource.context.ContextLexicalResource;
import lexent.resource.esa.EsaLexicalResource;
import lexent.resource.yago.YagoLexicalResource;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			System.exit(-1);
		}
		
		String trainPath = args[0];
		String testPath = args[1];
		String contextModelPath = args[2];
		String esaModelPath = args[3];
		String yagoModelPath = args[4];
		
		Rte6DataReader reader = new Rte6DataReader();
		List<Instance> train = reader.read(trainPath);
		List<Instance> test = reader.read(testPath);
		
		List<LexicalResource> resources = initResources(contextModelPath, esaModelPath, yagoModelPath);
		LexicalEntailmentModel model = new LexicalEntailmentModel(resources);
		model.train(train);
		
		Results results = evaluate(model, test);
		print(results);
	}
	
	private static List<LexicalResource> initResources(String contextModelPath,
			String esaModelPath, String yagoModelPath) throws IOException {
		List<LexicalResource> resources = new ArrayList<>();
		resources.add(new ContextLexicalResource(contextModelPath));
		resources.add(new EsaLexicalResource(esaModelPath));
		resources.add(new YagoLexicalResource(yagoModelPath));
		return resources;
	}
	
	private static Results evaluate(LexicalEntailmentModel model, List<Instance> test) {
		Results results = new Results();
		for (Instance instance : test) {
			results.update(instance.entails, model.entails(instance.t, instance.h));
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
