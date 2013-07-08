package lexent.main;

public class Results {
	
	public void update(boolean actual, boolean predicted) {
		if (actual) {
			if (predicted) {
				tp++;
			} else {
				fn++;
			}
		} else {
			if (predicted) {
				fp++;
			} else {
				tn++;
			}
		}
	}
	
	public double accuracy() {
		return (tp + tn) / (tp + fp + fn + tn);
	}
	
	public double precision() {
		return (tp) / (tp + fp);
	}
	
	public double recall() {
		return (tp) / (tp + fn);
	}
	
	public double f1() {
		double p = precision();
		double r = recall();
		return 2*p*r/(p+r);
	}
	
	
	private double tp = 0;
	private double fp = 0;
	private double fn = 0;
	private double tn = 0;
	
}
