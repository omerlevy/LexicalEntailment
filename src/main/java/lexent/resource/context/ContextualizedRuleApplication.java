package lexent.resource.context;

/**
 * @author Oren Melamud
 * This class computes context sensitive rule application scores based on word-topic rules described at:
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

/**
 * 
 */


public class ContextualizedRuleApplication {
	
	
	/* Helper classes */
	/*----------------*/


	/* p(word|topic) */
	class WordProbGivenTopic {

		List<Double> zeroCountProb;
		List<Map<String, Double>> probabilities;

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
				int topicId = Integer.parseInt(tokens[0]);
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
		}
		
		double getProb(String word, int topic) {
			if ((topic < 0) || (topic >= probabilities.size())) {
				return -1;
			}
			
			Map<String, Double> probMap = probabilities.get(topic);
			if (probMap.containsKey(word)) {
				return probMap.get(word);
			} else {
//				System.out.println("Using zero-count prob for word: " + word + " in topic: " + topic);
				return zeroCountProb.get(topic);
			}
		}
	}


	/* p(topic|slot) */
	class TopicProbGivenSlot {
		
		Map<String, Double> zeroCountProb;
		Map<String, Map<Integer, Double>> probabilities;
		
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
	class TwRule {
		
		String ruleType;
		String lhsPredicate;
		String rhsPredicate;
		double dirtScore;
		double dirtScoreX;
		double dirtScoreY;
		Map<Integer, Double> twScoresX;
		Map<Integer, Double> twScoresY;		
		double twScore;	// context sensitive rule application score
		
	
		
		public TwRule(BufferedReader reader) throws IOException {
			read(reader);
		}
		
		void read(BufferedReader reader) throws IOException {
			
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
			
			twScore = -1;
			twScoresX = readTwScores(reader.readLine());
			twScoresY = readTwScores(reader.readLine());
					
		}
		
		Map<Integer, Double> readTwScores(String line) {
			
			Map<Integer, Double> scores = new HashMap<Integer, Double>();
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
			return "TwRule [ruleType=" + ruleType + ", lhsPredicate="
					+ lhsPredicate + ", rhsPredicate=" + rhsPredicate
					+ ", dirtScore=" + dirtScore + ", dirtScoreX=" + dirtScoreX
					+ ", dirtScoreY=" + dirtScoreY + ", twScoresX=" + twScoresX
					+ ", twScoresY=" + twScoresY
					+ "]";
		}
				
	}
	
	
	/* Compares rules according to their dirt score. */
	public class DirtScoreComparator implements Comparator<TwRule> {
		@Override
		public int compare(TwRule r1, TwRule r2) {
			return Double.compare(r1.dirtScore, r2.dirtScore);
		}		
	}
	
	/* Compares rules according to their context sensitive score. */
	public class TwScoreComparator implements Comparator<TwRule> {
		@Override
		public int compare(TwRule r1, TwRule r2) {
			return Double.compare(r1.twScore, r2.twScore);
		}		
	}

	
	/*	A rule set knowledge resource. */
	class TwRuleSet {
		
		private Map<String, Map<String,TwRule>> rules;

		public TwRuleSet(String fileName, int maxRules, double minScore) throws IOException {
			
			System.out.println("Reading file: " + fileName);

			rules = new HashMap<String, Map<String,TwRule>>();
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			int i=0;
			int j=0;
			String lastReadLhsPredicate = null;
			try {
				while (true) {

					TwRule rule = new TwRule(reader);
//					if ((rule.dirtScore < minScore) || (rule.rhsPredicate.startsWith("Y"))) {
					if (rule.dirtScore < minScore) {
						continue;
					}
					if (!rule.lhsPredicate.equals(lastReadLhsPredicate)) {
						j=0;
						lastReadLhsPredicate = rule.lhsPredicate;
					}
					if (j<maxRules) {
						Map<String,TwRule> rulesPerGivenLhs;
						if (rules.containsKey(rule.lhsPredicate)) {
							rulesPerGivenLhs = rules.get(rule.lhsPredicate);
						} else {
							rulesPerGivenLhs = new HashMap<String, TwRule>();
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

			}
			System.out.println("Read " + i + " rules total.");
		}
		
		public Map<String,TwRule> getRulesPerGivenLhs(String lhsPredicate) {
			if (rules.containsKey(lhsPredicate)) {
				return rules.get(lhsPredicate);				
			} else {			
				return null;
			}
		}
		
		public TwRule getRule(String lhsPredicate, String rhsPredicate) {
						
			if (rules.containsKey(lhsPredicate)) {
				Map<String,TwRule> rulesPerGivenLhs = rules.get(lhsPredicate);
				if (rulesPerGivenLhs.containsKey(rhsPredicate)) {
					return rulesPerGivenLhs.get(rhsPredicate);
				}
			}
			
			return null;			
		}

	}
	
	
	
	
	/* The rule application functions. */
	/*---------------------------------*/
	private static final int TOP_RELEVANT = 10;
	
	
	/*	Interactive utility to play with rule applications. */
	public static void main(String[] args) throws IOException {
		
		if (args.length < 5) {
			System.out.println("Usage: ContextualizedRuleApplication <word-prob-given-topic-filename> <topic-prob-given-slot-filename> <tw-rules-filename> <max-rules-per-lhs-predicate> <min-rule-score> <lhs-tuple>");
			System.exit(1);
		}
		
		System.out.println("Topic Word Context Sensitive Predicate Inference Rule Application is initializing.");
		ContextualizedRuleApplication app = new ContextualizedRuleApplication(args[0], args[1], args[2], 
				Integer.parseInt(args[3]), Double.parseDouble(args[4]));
		
		int size = TOP_RELEVANT;
		Set<String> candidateSet = null;
		while(true) {
			System.out.println("Enter tuple:");
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
			
			List<TwRule> dirtInferredResult = new LinkedList<TwRule>();		
			List<TwRule> twInferredResult = new LinkedList<TwRule>();
			app.findTopInferred(lhsTuple,size,dirtInferredResult,twInferredResult, candidateSet);
			System.out.println();
			app.printInferred("DIRT",lhsTuple, dirtInferredResult);
			System.out.println();
			app.printInferred("TW",lhsTuple, twInferredResult);
		}
				

//		app.unitTest();
	}

	protected void unitTest(){
		
		System.out.println(wordTable.getProb("sunday", 0));
		System.out.println(slotTable.getProb("abbreviate:X", 2));
		System.out.println(slotTable.getProb("absorb:Y", 2));
		System.out.println(ruleTable.getRule("X collapse Y", "X fall Y"));
		
	}


	/* The elements of a topic-word rule set release. */
	private WordProbGivenTopic wordTable;
	private TopicProbGivenSlot slotTable;
	private TwRuleSet ruleTable;

	public ContextualizedRuleApplication(String wordTopicFileName, String slotTopicFileName, String TwRuleFileName, int maxRules, double minScore) throws IOException {
		readRuleResources(wordTopicFileName, slotTopicFileName, TwRuleFileName, maxRules, minScore);
		
	}

	/**
	 * Computes a context sensitive score for a rule application
	 * @param tPred: text predicate
	 * @param hPred: hypothesis predicate
	 * @param xContext: context words side X (space separated words)
	 * @param yContext: context words side Y (space separated words)
	 * @return score between 0 to 1, or -1 if predicates are not in the database or an error occurred.
	 */
	public double calcContextSensitiveScore(String tPred, String hPred, String xContext, String yContext) {

		List<TwRule> dirtInferredResult = new LinkedList<TwRule>();		
		List<TwRule> twInferredResult = new LinkedList<TwRule>();
		
		if (!isInDatabase(tPred, hPred)) {
			return -1;
		}

		findTopInferred(xContext + "\t" + tPred + "\t" + yContext, TOP_RELEVANT, dirtInferredResult, twInferredResult, null);

		if (twInferredResult.size() > 0 ) {
			return 1-((double) getRank(twInferredResult, toPredTemplate(hPred)) / twInferredResult.size());
		} else {
			return 0;
		}		
	}

	/**
	 * Computes a dirt-based (context insensitive score for a rule application).
	 * @param tPred: text predicate
	 * @param hPred: hypothesis predicate
	 * @return score between 0 to 1.
	 */
	public double calcDirtScore(String tPred, String hPred) {

		List<TwRule> dirtInferredResult = new LinkedList<TwRule>();		
		List<TwRule> twInferredResult = new LinkedList<TwRule>();

		findTopInferred("NA\t" + tPred + "\tNA" , TOP_RELEVANT, dirtInferredResult, twInferredResult, null);

		if (dirtInferredResult.size()>0) {
			return 1-((double) getRank(dirtInferredResult, toPredTemplate(hPred)) / dirtInferredResult.size());
		} else {
			return 0;
		}		

	}
	
	
	protected void printInferred(String method, String lhsTuple, List<TwRule> rules) {		
		System.out.println("Top " + method + " substitutes for: " + lhsTuple + "\n");
		
		String xContext = lhsTuple.split("\t")[0];
		String yContext = lhsTuple.split("\t")[2];
		for (TwRule rule : rules) {
			
			double twRankScore = calcContextSensitiveScore(fromPredTemplate(rule.lhsPredicate), fromPredTemplate(rule.rhsPredicate), xContext, yContext);
			double dirtRankScore = calcDirtScore(fromPredTemplate(rule.lhsPredicate), fromPredTemplate(rule.rhsPredicate));
			
			System.out.println(rule.rhsPredicate + "\tdirt="+rule.dirtScore + "\tdirt_rank="+dirtRankScore + "\ttw="+rule.twScore + "\ttw_rank="+twRankScore);						
		}		
	}
	
	protected void readRuleResources(String wordTopicFileName, String slotTopicFileName, String TwRuleFileName, int maxRules, double minScore) throws IOException {
		wordTable = new WordProbGivenTopic(wordTopicFileName);
		slotTable = new TopicProbGivenSlot(slotTopicFileName);
		ruleTable = new TwRuleSet(TwRuleFileName, maxRules, minScore);
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
	
	protected double computeTwScore(List<Double> topicDist, Map<Integer, Double> twScores) {
		
		double score = 0;
		for (int t=0; t<topicDist.size();t++) {
			if (twScores.containsKey(t)) {
				score += topicDist.get(t)*twScores.get(t);
			}
		}
		return score;
	}
	
	protected int getRank(List<TwRule> rules, String rhsPred) {
		
		int i=0;
		for (i=0; i<rules.size(); i++) {
			if (rules.get(i).rhsPredicate.equals(rhsPred)) {
				break;
			}
		}		
		return i;		
	}
	
	public boolean isInDatabase(String lhsPred, String rhsPred) {
		
		Map<String,TwRule> rulesLhs = ruleTable.getRulesPerGivenLhs(toPredTemplate(lhsPred));
		Map<String,TwRule> rulesRhs = ruleTable.getRulesPerGivenLhs(toPredTemplate(rhsPred));
		
		return ((rulesLhs != null) && (rulesRhs != null)); 
		
	}
	
	
	public void findTopInferred(String lhsTuple, int size, List<TwRule> dirtInferredResult, List<TwRule> twInferredResult, Set<String> candidates){
		
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
		
		PriorityQueue<TwRule> dirtInferred = new PriorityQueue<TwRule>(size, new DirtScoreComparator());
		PriorityQueue<TwRule> TwInferred = new PriorityQueue<TwRule>(size, new TwScoreComparator());
		
		Map<String,TwRule> rules = ruleTable.getRulesPerGivenLhs(lhsPredTemplate);
		if (rules == null) {
			return;
		}
		for (Map.Entry<String,TwRule> ruleEntry : rules.entrySet()) {
			TwRule rule = ruleEntry.getValue();
			if ((candidates != null) && (!candidates.contains(rule.rhsPredicate.split(" ")[1]))) {
				continue;
			}
			
			/* Ignoring rule with reverse order of slots. */
			if (rule.rhsPredicate.split(" ")[0].equals("Y")) {
				continue;
			}
			
			double twScoreX = computeTwScore(TopicDistributionX, rule.twScoresX);
			double twScoreY = computeTwScore(TopicDistributionY, rule.twScoresY);
			rule.twScore = Math.sqrt(twScoreX*twScoreY);
			dirtInferred.add(rule);
			if (dirtInferred.size()>size) {
				dirtInferred.poll();
			}			
			TwInferred.add(rule);
			if (TwInferred.size()>size) {
				TwInferred.poll();
			}
		}
				
		while(!dirtInferred.isEmpty()) {
			dirtInferredResult.add(0,dirtInferred.poll());
		}
		while(!TwInferred.isEmpty()) {
			twInferredResult.add(0,TwInferred.poll());
		}
		
	}

}
