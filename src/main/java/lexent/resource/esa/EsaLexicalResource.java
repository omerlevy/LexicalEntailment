package lexent.resource.esa;

import java.io.File;
import java.util.Arrays;

import lexent.data.Word;
import lexent.resource.LexicalResource;
import lexent.resource.LexicalResourceException;
import lexent.resource.LocalContext;
import de.tudarmstadt.ukp.similarity.algorithms.api.SimilarityException;
import de.tudarmstadt.ukp.similarity.algorithms.vsm.InnerVectorProduct;
import de.tudarmstadt.ukp.similarity.algorithms.vsm.VectorComparator;
import de.tudarmstadt.ukp.similarity.algorithms.vsm.VectorNorm;
import de.tudarmstadt.ukp.similarity.algorithms.vsm.store.vectorindex.VectorIndexReader;

public class EsaLexicalResource implements LexicalResource {
	
	public EsaLexicalResource(String esaModelPath) {
		esa = new VectorComparator(new VectorIndexReader(new File(esaModelPath)));
		esa.setInnerProduct(InnerVectorProduct.LEFT_DICE);
		esa.setNormalization(VectorNorm.NONE);
		// TODO use Lili's sim func (balAPInc) instead
	}
	
	public double probEntails(Word _t, Word _h, LocalContext context) throws LexicalResourceException {
		String t = _t.lemma;
		String h = _h.lemma;
		try {
			return esa.getSimilarity(Arrays.asList(t), Arrays.asList(h));
		} catch (SimilarityException e) {
			throw new LexicalResourceException("Words not found: " + t + " " + h , e);
		}
	}
	
	
	@Override
	public String getResourceName() {
		return "ExplicitSemanticAnalysisLexicalResource";
	}
	
	private VectorComparator esa;
	
}
