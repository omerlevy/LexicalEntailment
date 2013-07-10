package lexent.resource.context;

/**
 * Context sensitive inference resource for predicates.
 * Input files are described at: http://u.cs.biu.ac.il/~nlp/downloads/wt-rules.html
 * 
 * @author Oren Melamud
 */

import java.io.IOException;

import lexent.data.POS;
import lexent.data.Word;
import lexent.resource.LexicalResource;
import lexent.resource.LexicalResourceException;
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
	 * @param topRelevantInferred	The top inferred candidates that are taken under consideration
	 * @throws IOException
	 */
	public ContextLexicalResource(String wordTopicFileName, String slotTopicFileName, String TwRuleFileName, int maxRules, double minScore, int topRelevantInferred) throws IOException {
		System.out.print("Initializing ContextLexicalResource.\n");
		app = new ContextualizedRuleApplication(wordTopicFileName, slotTopicFileName, TwRuleFileName, maxRules, minScore, topRelevantInferred);
		System.out.print("ContextLexicalResource is ready.\n");
	}

	public double probEntails(Word _t, Word _h, LocalContext context) throws LexicalResourceException {
		
		if (_t.pos.equals(POS.Verb) && _h.pos.equals(POS.Verb)) {
			String t = _t.lemma;
			String h = _h.lemma;
			if (t.equals(h)) {
				return 1;
			} else {
				double score = app.calcContextSensitiveScore(t, h, context.getContextX(), context.getContextY());
				if (score < 0) {
					throw new LexicalResourceException("Unknown words: " + _t + " " + _h);
				}
				return score;
			}
		} else {
			throw new LexicalResourceException("Non-verbs: " + _t + " " + _h);
		}
		
	}
	
}
