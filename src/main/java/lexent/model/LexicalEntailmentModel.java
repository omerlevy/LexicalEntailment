package lexent.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lexent.data.Hypothesis;
import lexent.data.Instance;
import lexent.data.Sentence;
import lexent.data.Text;
import lexent.data.Word;
import lexent.resource.LexicalResource;
import lexent.resource.LexicalResourceException;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class LexicalEntailmentModel {
	
	/* --- Constructors --- */
	
	public LexicalEntailmentModel(List<LexicalResource> resources) {
		this.resources = resources;
	}
	
	
	/* --- Methods --- */

	public void train(List<Instance> train, String savePath) throws Exception {
		List<List<Double>> trainingData = generateTrainingData(train);
		learnModel(trainingData, savePath);
	}
	
	public boolean entails(Sentence text, Sentence hypo) throws Exception {
		if (model == null) {
			throw new NullPointerException("Model not instantiated!");
		}
		
		double answer = model.classifyInstance(new DenseInstance(1.0, toArray(generateFeatureVector(text, hypo))));
		return answer == TRUE;
	}
	
	private double probResourceEntails(LexicalResource resource, Sentence text, Sentence hypo) {
		double avgProb = 0.0;
		int numWords = 0;
		
		for (Word h : hypo.words) {
			double maxProb = -1.0;
			for (Word t : text.words) {
				try {
					double prob = resource.probEntails(t, h, text.context);
					maxProb = prob > maxProb ? prob : maxProb;
				} catch (LexicalResourceException e) {
					// Do nothing.
				}
			}
			if (maxProb >= 0.0) { // When averaging, include only words that were covered by resources.
				avgProb += maxProb;
				numWords++;
			}
		}
		
		if (numWords > 0) {
			return avgProb/numWords;
		} else {
			return 0.0;
		}
	}
	
	private List<List<Double>> generateTrainingData(List<Instance> train) {
		List<List<Double>> trainingData = new ArrayList<>();
		for (Instance instance : train) {
			Hypothesis hypo = instance.hypo;
			for (Text text : instance.texts) {
				List<Double> featureVector = generateFeatureVector(text.sent, hypo.sent);
				if (text.entails) {
					featureVector.add(TRUE);
				} else {
					featureVector.add(FALSE);
				}
				trainingData.add(featureVector);
			}
		}
		return trainingData;
	}
	
	private List<Double> generateFeatureVector(Sentence text, Sentence hypo) {
		List<Double> featureVector = new ArrayList<>();
		for (LexicalResource resource : resources) {
			featureVector.add(probResourceEntails(resource, text, hypo));
		}
		return featureVector;
	}
	
	private void learnModel(List<List<Double>> trainingData, String savePath) throws Exception {
		Instances instances = createWekaInstances();
		for (List<Double> instance : trainingData) {
			instances.add(new DenseInstance(1.0, toArray(instance)));
		}
		instances.setClassIndex(instances.numAttributes() - 1);
		
		saveInstances(instances, savePath);
		
		model = new J48();
		model.buildClassifier(instances);
	}
	
	private void saveInstances(Instances instances, String savePath) throws IOException {
		ArffSaver arff = new ArffSaver();
		arff.setInstances(instances);
		arff.setFile(new File(savePath));
		arff.writeBatch();
	}
	
	private Instances createWekaInstances() {
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (LexicalResource resource : resources) {
			attributes.add(new Attribute(resource.getClass().getSimpleName()));
		}
		attributes.add(new Attribute("class", Arrays.asList("false", "true")));
		return new Instances("train", attributes, 0);
	}
	
	private double[] toArray(List<Double> instance) {
		double[] array = new double[instance.size()];
		int i = 0;
		for (Double d : instance) {
			array[i] = d;
			i++;
		}
		return array;
	}
	
	
	/* --- Members --- */
	
	private List<LexicalResource> resources;
	
	private Classifier model;
	
	
	/* --- Constants --- */
	
	private static double TRUE = 1.0;
	
	private static double FALSE = 0.0;
	
}
