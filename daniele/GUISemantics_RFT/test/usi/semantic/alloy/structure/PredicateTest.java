package test.usi.semantic.alloy.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import src.usi.semantic.alloy.structure.Predicate;
import src.usi.semantic.alloy.structure.Signature;

public class PredicateTest {

	static public Predicate getPredicate1() throws Exception {

		final Signature sig1 = SignatureTest.getSig1();
		final Signature sig2 = SignatureTest.getSig2();

		final LinkedHashMap<Signature, List<String>> inputs = new LinkedHashMap<>();
		final List<String> ls = new ArrayList<String>();
		ls.add("v");
		inputs.put(sig1, ls);
		final List<String> ls2 = new ArrayList<String>();
		ls2.add("v1");
		ls2.add("v2");
		inputs.put(sig2, ls2);
		final Predicate pred = new Predicate("go_fail_post", "List.contains.t' = List.contains.t",
				inputs);
		return pred;
	}

	@Test
	public void creationAndTostringTest() {

		try {
			final Predicate pred = getPredicate1();

			final String s = pred.toString();
			// System.out.println(s);
			final String separator = System.getProperty("line.separator");
			final String[] lines = s.split(separator);
			assertEquals(3, lines.length);
			assertTrue("pred go_fail_post [v1, v2: sig2, v: sig] {".equals(lines[0])
					|| "pred go_fail_post [v: sig, v1, v2: sig2] {".equals(lines[0]));
			assertTrue("	List.contains.t' = List.contains.t".equals(lines[1]));
			assertTrue("}".equals(lines[2]));

		} catch (final Exception e) {
			fail();
		}
	}
}
