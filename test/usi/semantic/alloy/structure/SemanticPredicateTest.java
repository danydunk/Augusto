package test.usi.semantic.alloy.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import src.usi.semantic.alloy.structure.Semantic_predicate;
import src.usi.semantic.alloy.structure.Signature;

public class SemanticPredicateTest {

	static public Semantic_predicate getSemanticPredicate() throws Exception {

		final Signature sig1 = SignatureTest.getSig1();
		final LinkedHashMap<Signature, List<String>> inputs = new LinkedHashMap<>();
		final List<String> ls = new ArrayList<String>();
		ls.add("v");
		inputs.put(sig1, ls);
		String content = "";
		content += "(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => filled_required_in_w_test [Current_window.is_in.t, t]";
		content += System.getProperty("line.separator");
		content += "(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (filled_required_test [t] and unique_test [t])";

		final Semantic_predicate out = new Semantic_predicate("predd", content, inputs);
		return out;
	}

	@Test
	public void creationAndTostringTest() {

		try {
			final Semantic_predicate pred = getSemanticPredicate();

			final String s = pred.toString();
			// System.out.println(s);
			final String separator = System.getProperty("line.separator");
			final String[] lines = s.split(separator);
			assertEquals(4, lines.length);
			assertTrue("pred predd [v: sig] {".equals(lines[0]));
			assertTrue("	(aw in Ok and Current_window.is_in.(T/prev[t])  in Form and (#aw.goes = 1 and aw.goes in Form)) => filled_required_in_w_test [Current_window.is_in.(T/prev[t]), (T/prev[t])]"
					.equals(lines[1]));
			assertTrue("	(aw in Ok and Current_window.is_in.(T/prev[t])  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (filled_required_test [T/prev[t]] and unique_test [T/prev[t]])"
					.equals(lines[2]));
			assertTrue("}".equals(lines[3]));

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
