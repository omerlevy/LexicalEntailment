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
		app = new ContextualizedRuleApplication(wordTopicFileName, slotTopicFileName, TwRuleFileName, maxRules, minScore);
	}

	public double probEntails(String t, String h, LocalContext context) {
		return app.calcContextSensitiveScore(t, h, context.getContextX(), context.getContextY());
	}

}
