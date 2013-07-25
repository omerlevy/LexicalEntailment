package lexent.resource.context;

/**
 * @author Oren Melamud
 * This class derives context sensitive rule application scores based on word-topic rules described at:
 * http://u.cs.biu.ac.il/~nlp/downloads/wt-rules.html
 *
 */

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class ContextualizedRuleApplication {
	
	
	private static final int TOP_RELEVANT = 10;	// default number of top relevant inferred predicates
		
	/*	Interactive utility to play with rule applications. */
	public static void main(String[] args) throws IOException {
		
		if (args.length < 5) {
			System.out.println("Usage: ContextualizedRuleApplication <word-prob-given-topic-filename> <topic-prob-given-slot-filename> <wt-rules-filename> <max-rules-per-lhs-predicate> <min-rule-score> <ignoreReverseRules y/n>");
			System.exit(1);
		}
		
		boolean ignoreReverseRules = args[5].equals("y");
		
		System.out.println("Topic Word Context Sensitive Predicate Inference Rule Application is initializing.");
		ContextualizedRuleApplication app = new ContextualizedRuleApplication(args[0], args[1], args[2], 
				Integer.parseInt(args[3]), Double.parseDouble(args[4]), ignoreReverseRules, TOP_RELEVANT);
		System.out.println("\nApplication is ready. \nPress q and enter to quit at any time.");
		
		int size = TOP_RELEVANT;
		Set<String> candidateSet = null;
		while(true) {
			System.out.println("\nEnter tuple <argumentX><tab><predicate><tab><argumentY>:");
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

			String lhsTuple = input.readLine();
			if (lhsTuple.equals("q")) {
				break;
			}
			if (lhsTuple.startsWith("size=")) {
				size = Integer.parseInt(lhsTuple.split("=")[1]);
				continue;
			}
			
			if (lhsTuple.startsWith("candidates=")) {
				String candidatesString = lhsTuple.split("=")[1];
				if (candidatesString.equals("null")) {
					candidateSet=null;
				}
				String[] tokens = candidatesString.split(" ");
				candidateSet = new HashSet<String>();
				for (String tok : tokens) {
					candidateSet.add(tok);
				}
				continue;
			}
			
			List<WTRule> dirtInferredResult = new LinkedList<WTRule>();		
			List<WTRule> wtInferredResult = new LinkedList<WTRule>();
			app.findTopInferred(lhsTuple,size,dirtInferredResult,wtInferredResult, candidateSet);
			System.out.println();
			app.printInferred("DIRT",lhsTuple, dirtInferredResult);
			System.out.println();
			app.printInferred("WT",lhsTuple, wtInferredResult);
		}
		
		System.out.println("\nApplication terminated.\n");
				
	}

	/* The elements of a topic-word rule set release. */
	private WordProbGivenTopic wordTable;	// p(word|topic)
	private TopicProbGivenSlot slotTable;	// p(topic|slot)
	private WTRuleSet ruleTable;			// context sensitive rules
	
	/* Config params. */
	private int topRelevantInferred;		// number of top ranking relevant inferred predicates per inferring predicate

	public ContextualizedRuleApplication(String wordTopicFileName, String slotTopicFileName, String wtRuleFileName, int maxRules, double minScore, boolean ignoreReverseRules, int topRelevantInferred) throws IOException {
		this.topRelevantInferred = topRelevantInferred;
		readRuleResources(wordTopicFileName, slotTopicFileName, wtRuleFileName, maxRules, minScore, ignoreReverseRules);		
	}

	/**
	 * Computes a word-topic context sensitive rank based score for a rule application.
	 * Rank based score equals the rank of hPred in tPred's top inferred predicates divided by topRelevantInferred.
	 * @param tPred: text predicate
	 * @param hPred: hypothesis predicate
	 * @param xContext: context words side X (space separated words)
	 * @param yContext: context words side Y (space separated words)
	 * @return score between 0 to 1, or -1 if predicates are not in the database or an error occurred.
	 */
	public double calcWTRankScore(String tPred, String hPred, String xContext, String yContext) {

		List<WTRule> dirtInferredResult = new LinkedList<WTRule>();		
		List<WTRule> wtInferredResult = new LinkedList<WTRule>();
		
		if (!isInDatabase(tPred, hPred)) {
			return -1;
		}
		
		if (xContext.equals("")) {
			xContext = "NotAvailable";			 
		}
		
		if (yContext.equals("")) {
			yContext = "NotAvailable";			 
		}

		findTopInferred(xContext + "\t" + tPred + "\t" + yContext, this.topRelevantInferred, dirtInferredResult, wtInferredResult, null);

		if (wtInferredResult.size() > 0 ) {
			return 1-((double) getRank(wtInferredResult, toPredTemplate(hPred)) / wtInferredResult.size());
		} else {
			return 0;
		}		
	}

	/**
	 * Computes a dirt-based (context insensitive score for a rule application).
	 * Rank based score equals the rank of hPred in tPred's top inferred predicates divided by topRelevantInferred.
	 * @param tPred: text predicate
	 * @param hPred: hypothesis predicate
	 * @return score between 0 to 1.
	 */
	public double calcDirtRankScore(String tPred, String hPred) {

		List<WTRule> dirtInferredResult = new LinkedList<WTRule>();		
		List<WTRule> wtInferredResult = new LinkedList<WTRule>();

		findTopInferred("NA\t" + tPred + "\tNA" , TOP_RELEVANT, dirtInferredResult, wtInferredResult, null);

		if (dirtInferredResult.size()>0) {
			return 1-((double) getRank(dirtInferredResult, toPredTemplate(hPred)) / dirtInferredResult.size());
		} else {
			return 0;
		}		

	}
	
	public boolean isInDatabase(String lhsPred, String rhsPred) {

		Map<String,WTRule> rulesLhs = ruleTable.getRulesPerGivenLhs(toPredTemplate(lhsPred));
		Map<String,WTRule> rulesRhs = ruleTable.getRulesPerGivenLhs(toPredTemplate(rhsPred));

		return ((rulesLhs != null) && (rulesRhs != null)); 
	}


	public void findTopInferred(String lhsTuple, int size, List<WTRule> dirtInferredResult, List<WTRule> wtInferredResult, Set<String> candidates){
		
		String[] tokens = lhsTuple.split("\t");
		if (tokens.length<3) {
			System.out.print("Invalid input.\n");
			return;
		}
		
		
		String argX = tokens[0];
		String lhsPredTemplate = toPredTemplate(tokens[1]);
		String argY = tokens[2];
		
		List<Double> TopicDistributionX = computeTopicDistribution(tokens[1]+":X", argX.split(" "));
		List<Double> TopicDistributionY = computeTopicDistribution(tokens[1]+":Y", argY.split(" "));
		
		PriorityQueue<WTRule> dirtInferred = new PriorityQueue<WTRule>(size, new DirtScoreComparator());
		PriorityQueue<WTRule> wtInferred = new PriorityQueue<WTRule>(size, new WTScoreComparator());
		
		Map<String,WTRule> rules = ruleTable.getRulesPerGivenLhs(lhsPredTemplate);
		if (rules == null) {
			return;
		}
		for (Map.Entry<String,WTRule> ruleEntry : rules.entrySet()) {
			WTRule rule = ruleEntry.getValue();
			if ((candidates != null) && (!candidates.contains(rule.rhsPredicate.split(" ")[1]))) {
				continue;
			}
						
			double wtScoreX = computeWTScore(TopicDistributionX, rule.wtScoresX);
			double wtScoreY = computeWTScore(TopicDistributionY, rule.wtScoresY);
			rule.wtScore = Math.sqrt(wtScoreX*wtScoreY);
			dirtInferred.add(rule);
			if (dirtInferred.size()>size) {
				dirtInferred.poll();
			}			
			wtInferred.add(rule);
			if (wtInferred.size()>size) {
				wtInferred.poll();
			}
		}
				
		while(!dirtInferred.isEmpty()) {
			dirtInferredResult.add(0,dirtInferred.poll());
		}
		while(!wtInferred.isEmpty()) {
			wtInferredResult.add(0,wtInferred.poll());
		}
		
	}
	
	protected void printInferred(String method, String lhsTuple, List<WTRule> rules) {		
		System.out.println("Top " + method + " substitutes for: " + lhsTuple + "\n");

		if (lhsTuple.split("\t").length != 3) {
			System.out.print("Invalid lhsTuple\n");
			return;
		}

		String xContext = lhsTuple.split("\t")[0];
		String yContext = lhsTuple.split("\t")[2];
		for (WTRule rule : rules) {	
//			for debug only:
//			double wtRankScore = calcWTRankScore(fromPredTemplate(rule.lhsPredicate), fromPredTemplate(rule.rhsPredicate), xContext, yContext);
//			double dirtRankScore = calcDirtRankScore(fromPredTemplate(rule.lhsPredicate), fromPredTemplate(rule.rhsPredicate));
//			System.out.println(rule.rhsPredicate + "\tdirt="+rule.dirtScore + "\tdirt_rank="+dirtRankScore + "\twt="+rule.wtScore + "\twt_rank="+wtRankScore);							
			System.out.println(rule.rhsPredicate + "\tdirt="+rule.dirtScore + "\twt="+rule.wtScore);
		}		
	}
	
	protected void readRuleResources(String wordTopicFileName, String slotTopicFileName, String wtRuleFileName, int maxRules, double minScore, boolean ignoreReverseRules) throws IOException {
		wordTable = new WordProbGivenTopic(wordTopicFileName);
		slotTable = new TopicProbGivenSlot(slotTopicFileName);
		ruleTable = new WTRuleSet(wtRuleFileName, maxRules, minScore, ignoreReverseRules);
	}
	
	protected int getTopicNum() {
		return wordTable.probabilities.size();
	}
	
	
	protected void pointwiseAdd(List<Double> a, List<Double> b) {
		for (int i=0; i<a.size(); i++){
			a.set(i, a.get(i) + b.get(i));
		}		
	}
	
	protected String toPredTemplate(String pred) {
		return "X " + pred + " Y";
	}
	
	protected String fromPredTemplate(String predTemplate) {
		return predTemplate.substring(2, predTemplate.length()-2);
	}
	
	
	
	protected List<Double> computeTopicDistribution(String slot, String[] args) {
		List<Double> topicDist = new ArrayList<Double>(Collections.nCopies(this.getTopicNum(), 0d));
		
		for (String arg : args) {
			pointwiseAdd(topicDist, computeTopicDistribution(slot, arg));
		}
		
		for (int t=0; t<this.getTopicNum(); t++){
			topicDist.set(t, topicDist.get(t)/args.length);
		}		
		return topicDist;
		
	}
	
	protected List<Double> computeTopicDistribution(String slot, String arg) {
		
		List<Double> topicDist = new ArrayList<Double>(this.getTopicNum());
		double normFactor = 0;
		for (int t=0; t<this.getTopicNum(); t++){
			double prob = slotTable.getProb(slot, t)*wordTable.getProb(arg, t);
			topicDist.add(prob);
			normFactor += prob;
		}

		for (int t=0; t<this.getTopicNum(); t++){
			topicDist.set(t, topicDist.get(t)/normFactor);
		}		
		return topicDist;		
	}
	
	protected double computeWTScore(List<Double> topicDist, Map<Integer, Double> wtScores) {
		
		double score = 0;
		for (int t=0; t<topicDist.size();t++) {
			if (wtScores.containsKey(t)) {
				score += topicDist.get(t)*wtScores.get(t);
			}
		}
		return score;
	}
	
	protected int getRank(List<WTRule> rules, String rhsPred) {
		
		int i=0;
		for (i=0; i<rules.size(); i++) {
			if (rules.get(i).rhsPredicate.equals(rhsPred)) {
				break;
			}
		}		
		return i;		
	}
	
	/*----------------*/
	/* Helper classes */
	/*----------------*/

	/* p(word|topic) */
	class WordProbGivenTopic {

		private List<Double> zeroCountProb;
		private List<Map<String, Double>> probabilities;

		public WordProbGivenTopic(String fileName) throws IOException {
			
			System.out.println("Reading file: " + fileName);

			BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line = reader.readLine();
			int topicNum = Integer.parseInt(line);

			zeroCountProb = new ArrayList<Double>(topicNum);
			probabilities = new ArrayList<Map<String, Double>>(topicNum);
			for (int i=0; i<topicNum; i++) {
				probabilities.add(new HashMap<String, Double>());
			}

			for (int i=0; i<topicNum; i++) {
				line = reader.readLine();
				String[] tokens = line.split("\t");
//				int topicId = Integer.parseInt(tokens[0]);
				double prob = Double.parseDouble(tokens[2]);
				zeroCountProb.add(prob);
			}


			while((line=reader.readLine())!=null){
				String[] tokens = line.split("\t");
				int topicId = Integer.parseInt(tokens[0]);
				String word = tokens[1];
				double prob = Double.parseDouble(tokens[2]);
				probabilities.get(topicId).put(word, prob);
			}
			
			reader.close();
		}
		
		public double getProb(String word, int topic) {
			if ((topic < 0) || (topic >= probabilities.size())) {
				return -1;
			}
			
			Map<String, Double> probMap = probabilities.get(topic);
			if (probMap.containsKey(word)) {
				return probMap.get(word);
			} else {
				return zeroCountProb.get(topic);
			}
		}
	}


	/* p(topic|slot) */
	class TopicProbGivenSlot {
		
		private Map<String, Double> zeroCountProb;
		private Map<String, Map<Integer, Double>> probabilities;
		
		public TopicProbGivenSlot(String fileName) throws IOException {
			
			System.out.println("Reading file: " + fileName);
			
			zeroCountProb = new HashMap<String, Double>();
			probabilities = new HashMap<String, Map<Integer, Double>>();
			
			BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line;
			while((line = reader.readLine())!=null) {
				String predicate = line.split(" ")[1];
				
				line = reader.readLine();
				readTopicDistLine(line, predicate+":X");
				
				line = reader.readLine();
				readTopicDistLine(line, predicate+":Y");
			}
			
			reader.close();
		}
		
		public double getProb(String slot, int topic) {
			
			if (probabilities.containsKey(slot)) {
				Map<Integer, Double> probDist = probabilities.get(slot);
				if (probDist.containsKey(topic)) {
					return probDist.get(topic);
				} 				
			}
			
			if (zeroCountProb.containsKey(slot)) {
				return zeroCountProb.get(slot);
			}
			
			return -1;
		}
		
		protected void readTopicDistLine(String line, String slotKey) {
			
			String[] slotXtokens = line.split("\t");
			String prob = slotXtokens[0].split("::")[1];
			zeroCountProb.put(slotKey, Double.parseDouble(prob));			
			
			Map<Integer, Double> probDistribution = new HashMap<Integer, Double>();				
			for (int i=1; i<slotXtokens.length; i++) {
				String[] entryTokens = slotXtokens[i].split("::");
				probDistribution.put(Integer.parseInt(entryTokens[0]), Double.parseDouble(entryTokens[1]));
			}
			probabilities.put(slotKey, probDistribution);	
		}

	}
	
	
	/* A context sensitive rule. */
	class WTRule {
		
		private String ruleType;
		private String lhsPredicate;
		private String rhsPredicate;
		private double dirtScore;
		private double dirtScoreX;
		private double dirtScoreY;
		private Map<Integer, Double> wtScoresX;
		private Map<Integer, Double> wtScoresY;		
		private double wtScore;	// context sensitive rule application score			
		
		public WTRule(BufferedReader reader) throws IOException {
			read(reader);
		}
		
		public void read(BufferedReader reader) throws IOException {
			
			String line = reader.readLine();
			if (line == null) {
				throw new EOFException();
			}
			String[] tokens = line.split("\t");			
			ruleType = tokens[0];
			lhsPredicate = tokens[1];
			rhsPredicate = tokens[2];		
			dirtScore = Double.parseDouble(tokens[3]);
			dirtScoreX = Double.parseDouble(tokens[4]);
			dirtScoreY = Double.parseDouble(tokens[5]);	
			
			wtScore = -1;
			wtScoresX = readWTScores(reader.readLine());
			wtScoresY = readWTScores(reader.readLine());
					
		}
		
		protected Map<Integer, Double> readWTScores(String line) {
			
			Map<Integer, Double> scores = new HashMap<Integer, Double>();
			if (line.equals("")) {
				return scores;
			}
			String[] tokens = line.split("\t");
			for (String token : tokens) {
				String[] subTokens = token.split("::");
				int topic = Integer.parseInt(subTokens[0]);
				double score = Double.parseDouble(subTokens[1]);
				scores.put(topic, score);
			}
			
			return scores;
		}
		
		@Override
		public String toString() {
			return "WTRule [ruleType=" + ruleType + ", lhsPredicate="
					+ lhsPredicate + ", rhsPredicate=" + rhsPredicate
					+ ", dirtScore=" + dirtScore + ", dirtScoreX=" + dirtScoreX
					+ ", dirtScoreY=" + dirtScoreY + ", wtScoresX=" + wtScoresX
					+ ", wtScoresY=" + wtScoresY
					+ "]";
		}
				
	}
	
	
	/* Compares rules according to their dirt score. */
	public class DirtScoreComparator implements Comparator<WTRule> {
		@Override
		public int compare(WTRule r1, WTRule r2) {
			return Double.compare(r1.dirtScore, r2.dirtScore);
		}		
	}
	
	/* Compares rules according to their context sensitive score. */
	public class WTScoreComparator implements Comparator<WTRule> {
		@Override
		public int compare(WTRule r1, WTRule r2) {
			return Double.compare(r1.wtScore, r2.wtScore);
		}		
	}

	
	/*	A rule set knowledge resource. */
	class WTRuleSet {
		
		// Maps between an lhs predicate to a map of rhs predicates and their respective rule
		private Map<String, Map<String,WTRule>> rules;
		
		boolean ignoreReverseRules;

		public WTRuleSet(String fileName, int maxRules, double minScore, boolean ignoreReverseRules) throws IOException {
			
			this.ignoreReverseRules = ignoreReverseRules;
			
			System.out.println("Reading file: " + fileName);

			rules = new HashMap<String, Map<String,WTRule>>();
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			int i=0;
			int j=0;
			String lastReadLhsPredicate = null;
			try {
				while (true) {
					WTRule rule = new WTRule(reader);
					if (this.ignoreReverseRules && (rule.rhsPredicate.startsWith("Y"))) {
						continue;
					}
					if (rule.dirtScore < minScore) {
						continue;
					}
					if (!rule.lhsPredicate.equals(lastReadLhsPredicate)) {
						j=0;
						lastReadLhsPredicate = rule.lhsPredicate;
					}
					if (j<maxRules) {
						Map<String,WTRule> rulesPerGivenLhs;
						if (rules.containsKey(rule.lhsPredicate)) {
							rulesPerGivenLhs = rules.get(rule.lhsPredicate);
						} else {
							rulesPerGivenLhs = new HashMap<String, WTRule>();
							rules.put(rule.lhsPredicate, rulesPerGivenLhs);
						}
						rulesPerGivenLhs.put(rule.rhsPredicate, rule);
						i++;
						if (i%10000==0) {
							System.out.println("Read " + i + " rules so far.");
						}
					}
					j++;
				}
			} catch (EOFException e) {

			} finally {
				reader.close();
			}
			System.out.println("Read " + i + " rules total.");
			
		}
		
		public Map<String,WTRule> getRulesPerGivenLhs(String lhsPredicate) {
			if (rules.containsKey(lhsPredicate)) {
				return rules.get(lhsPredicate);				
			} else {			
				return null;
			}
		}
		
		public WTRule getRule(String lhsPredicate, String rhsPredicate) {
						
			if (rules.containsKey(lhsPredicate)) {
				Map<String,WTRule> rulesPerGivenLhs = rules.get(lhsPredicate);
				if (rulesPerGivenLhs.containsKey(rhsPredicate)) {
					return rulesPerGivenLhs.get(rhsPredicate);
				}
			}
			
			return null;			
		}

	}

}
