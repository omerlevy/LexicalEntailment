package test.lexent.resources.context;

import java.io.IOException;

import lexent.data.Word;
import lexent.resource.LexicalResourceException;
import lexent.resource.LocalContext;
import lexent.resource.context.ContextLexicalResource;

import org.junit.Test;

public class ContextLexicalResourceTest {

	@Test
	public void test() throws IOException, LexicalResourceException {
		
		String contextDir = null; //TODO give test path here
		
		String wordTopicFileName = contextDir + "/context_wordtopic.txt";
		String slotTopicFileName = contextDir + "/context_slottopic.txt";
		String twRuleFileName = contextDir + "/context_rules.txt";
		int maxRules = 250;
		double minScore = 0.01d;
		int topRelevantInferred = 20;

		ContextLexicalResource resource = new ContextLexicalResource(
				wordTopicFileName, slotTopicFileName, twRuleFileName, maxRules,
				minScore, topRelevantInferred);

		double prob;
		double prob1;
		double prob2;

		prob1 = resource.probEntails(wrap("acquire"), wrap("buy"), new LocalContext(
				"microsoft", "share"));
		System.out.printf("microsoft acquire company -> buy: %.2f\n", prob1);

		prob2 = resource.probEntails(wrap("acquire"), wrap("buy"), new LocalContext("baby",
				"skill"));
		System.out.printf("baby acquire skill -> buy: %.2f\n", prob2);

		assert (prob1 > prob2);

		prob1 = resource.probEntails(wrap("fight"), wrap("help"), new LocalContext(
				"aspirin", "headache"));
		System.out.printf("aspirin fight headache -> help: %.2f\n", prob1);

		prob2 = resource.probEntails(wrap("fight"), wrap("help"), new LocalContext(
				"israel", "terror"));
		System.out.printf("israel fight terror -> help: %.2f\n", prob2);

		assert (prob1 > prob2);

		prob = resource.probEntails(wrap("buy"), wrap("purchase"), new LocalContext("", ""));
		System.out.printf("buy -> purchase: %.2f\n", prob);
		assert (prob > 0);

		prob = resource.probEntails(wrap("buy"), wrap("buy"), new LocalContext("", ""));
		System.out.printf("buy -> buy: %.2f\n", prob);
		assert (prob == 1);

		prob = resource
				.probEntails(wrap("blahhhhh"), wrap("buy"), new LocalContext("", ""));
		System.out.printf("blahhhh -> buy: %.2f\n", prob);
		assert (prob == -1);

		prob = resource.probEntails(wrap("buy"), wrap("blahhhh"), new LocalContext("", ""));
		System.out.printf("buy -> blahhhh: %.2f\n", prob);
		assert (prob == -1);
	}
	
	private Word wrap(String str) {
		return new Word(str, "v");
	}

}
