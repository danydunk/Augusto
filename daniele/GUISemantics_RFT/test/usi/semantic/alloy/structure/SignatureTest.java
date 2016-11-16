package test.usi.semantic.alloy.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import src.usi.pattern.structure.Cardinality;
import src.usi.semantic.alloy.structure.Signature;

public class SignatureTest {

	static public Signature getSig1() throws Exception {

		final Signature sig = new Signature("sig", Cardinality.ONE, false, null, false);
		final Signature sig2 = new Signature("othersig", Cardinality.ONE, false, null, false);
		final Signature sig3 = new Signature("othersig2", Cardinality.ONE, false, null, false);

		sig.addBinaryRelation("rel1", sig2, Cardinality.LONE);
		sig.addTernaryRelation("rel2", sig2, sig3, Cardinality.SET, Cardinality.ONE);

		return sig;
	}

	static public Signature getSig2() throws Exception {

		final Signature sig2 = new Signature("othersig", Cardinality.ONE, false, null, false);
		final Signature sig3 = new Signature("othersig2", Cardinality.ONE, false, null, false);
		final List<Signature> l = new ArrayList<Signature>();
		l.add(sig2);
		final Signature sig = new Signature("sig2", Cardinality.ONE, true, l, false);

		sig.addBinaryRelation("rel1", sig2, Cardinality.LONE);
		sig.addTernaryRelation("rel2", sig2, sig3, Cardinality.SET, Cardinality.ONE);
		return sig;
	}

	@Test
	public void creationAndTostringTest() {

		try {
			final Signature sig1 = getSig1();
			final Signature sig2 = getSig2();

			String s = sig1.toString();
			final String separator = System.getProperty("line.separator");
			String[] lines = s.split(separator);
			assertEquals(4, lines.length);
			assertTrue("one sig sig {".equals(lines[0]));
			assertTrue("	rel1: lone othersig,".equals(lines[1]));
			assertTrue("	rel2: othersig2 -> one othersig,".equals(lines[2]));
			assertTrue("}".equals(lines[3]));

			s = sig2.toString();
			lines = s.split(separator);
			assertEquals(4, lines.length);
			assertTrue("abstract one sig sig2 extends othersig {".equals(lines[0]));
			assertTrue("	rel1: lone othersig,".equals(lines[1]));
			assertTrue("	rel2: othersig2 -> one othersig,".equals(lines[2]));
			assertTrue("}".equals(lines[3]));
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
