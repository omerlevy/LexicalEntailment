package lexent.resource.context;

/**
 * Context sensitive inference resource for predicates.
 * Input files are described at: http://u.cs.biu.ac.il/~nlp/downloads/wt-rules.html
 * 
 * @author Oren Melamud
 */

import java.io.IOException;
import lexent.resource.LexicalResource;
import lexent.resource.LocalContext;

public class ContextLexicalResource implements LexicalResource {
	
	private ContextualizedRuleApplication app;
	
	/**
	 * Initialize resource
	 * @param wordTopicFileName	LDA word distributions - p(word|topic)
	 * @param slotTopicFileName	LDA topic mixtures - p(topic|slot	
	 * @param TwRuleFileName	Context sensitive rules
	 * @param maxRules			Maximum entailed candidates per entailing (the top maxRules per entailing predicate will be read)
	 * @param minScore			Minimum score of rule (rules with scores below that will not be read)
	 * @throws IOException
	 */
	public ContextLexicalResource(String wordTopicFileName, String slotTopicFileName, String TwRuleFileName, int maxRules, double minScore) throws IOException {
		System.out.print("Initializing ContextLexicalResource.\n");
		app = new ContextualizedRuleApplication(wordTopicFileName, slotTopicFileName, TwRuleFileName, maxRules, minScore);
		System.out.print("ContextLexicalResource is ready.\n");
	}

	public double probEntails(String t, String h, LocalContext context) {
		
		if (t.equals(h)) {
			return 1;
		} else {
			return app.calcContextSensitiveScore(t, h, context.getContextX(), context.getContextY());
		}
	}
	
	public static void unitTest(String contextDir) throws IOException {
		
		String wordTopicFileName = contextDir + "/context_wordtopic.txt";
		String slotTopicFileName = contextDir + "/context_slottopic.txt";
		String twRuleFileName = contextDir + "/context_rules.txt";
		int maxRules = 100;
		double minScore = 0.05d;
		
		ContextLexicalResource resource = new ContextLexicalResource(wordTopicFileName, slotTopicFileName, twRuleFileName, maxRules, minScore);
		
		double prob;
		double prob1;
		double prob2;
		
		
		prob1 = resource.probEntails("acquire", "buy", new LocalContext("microsoft", "share"));
		System.out.printf("microsoft acquire company -> buy: %.2f\n", prob1);
		
		prob2 = resource.probEntails("acquire", "buy", new LocalContext("baby", "skill"));
		System.out.printf("baby acquire skill -> buy: %.2f\n", prob2);
		
		assert(prob1 > prob2);
		
		
		prob1 = resource.probEntails("fight", "help", new LocalContext("aspirin", "headache"));
		System.out.printf("aspirin fight headache -> help: %.2f\n", prob1);
		
		prob2 = resource.probEntails("fight", "help", new LocalContext("israel", "terror"));
		System.out.printf("israel fight terror -> help: %.2f\n", prob2);
		
		assert(prob1 > prob2);
		
		
		prob = resource.probEntails("buy", "purchase", new LocalContext("", ""));
		System.out.printf("buy -> purchase: %.2f\n", prob);
		assert(prob > 0);
		
		prob = resource.probEntails("buy", "buy", new LocalContext("", ""));
		System.out.printf("buy -> buy: %.2f\n", prob);
		assert(prob == 1);
		
		
		prob = resource.probEntails("blahhhhh", "buy", new LocalContext("", ""));
		System.out.printf("blahhhh -> buy: %.2f\n", prob);
		assert(prob == -1);
		
		prob = resource.probEntails("buy", "blahhhh", new LocalContext("", ""));
		System.out.printf("buy -> blahhhh: %.2f\n", prob);
		assert(prob == -1);		
	}
	
	public static void main(String[] args) throws IOException {		
		unitTest(args[0]);		
	}

}
