package test.lexent.resources.esa;

import lexent.data.Word;
import lexent.resource.LexicalResourceException;
import lexent.resource.esa.EsaLexicalResource;

import org.junit.Test;

public class EsaLexicalResourceTest {
	
	@Test
	public void test_probEntails() throws LexicalResourceException {
		EsaLexicalResource esa = new EsaLexicalResource("../DKPRO_HOME/esaIndexesVector/en/wp");
		System.out.println(esa.probEntails(wrap("dog"), wrap("animal"), null));
		System.out.println(esa.probEntails(wrap("animal"), wrap("dog"), null));
		System.out.println(esa.probEntails(wrap("kidney"), wrap("organ"), null));
		System.out.println(esa.probEntails(wrap("organ"), wrap("kidney"), null));
		System.out.println(esa.probEntails(wrap("buy"), wrap("acquire"), null));
		System.out.println(esa.probEntails(wrap("acquire"), wrap("buy"), null));
		System.out.println(esa.probEntails(wrap("synthesis"), wrap("production"), null));
		System.out.println(esa.probEntails(wrap("production"), wrap("synthesis"), null));
	}
	
	private Word wrap(String str) {
		return new Word(str, "n");
	}
	
}
