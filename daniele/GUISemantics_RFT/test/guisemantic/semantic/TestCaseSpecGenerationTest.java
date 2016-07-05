package test.guisemantic.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import usi.guipattern.Cardinality;
import usi.guisemantic.FunctionalitySemantics;
import usi.guisemantic.alloy.entity.Fact;
import usi.guisemantic.alloy.entity.Function;
import usi.guisemantic.alloy.entity.Predicate;
import usi.guisemantic.alloy.entity.Signature;
import test.guisemantic.alloy.entity.SignatureTest;

public class TestCaseSpecGenerationTest {

	public FunctionalitySemantics getFunctSemantics() throws Exception {
		final List<Signature> sigs = new ArrayList<>();
		final Signature window = new Signature("Window", null, true, null, false);
		final Signature aw = new Signature("Action_widget", null, true, null, false);
		final Signature iw = new Signature("Input_widget", null, true, null, false);
		sigs.add(window);
		sigs.add(aw);
		sigs.add(iw);

		final Signature sig1 = SignatureTest.getSig1();
		final Signature sig2 = SignatureTest.getSig2();
		final List<Signature> l = new ArrayList<Signature>();
		l.add(window);
		final Signature sig3 = new Signature("sig3", Cardinality.ONE, true, l, false);
		sigs.add(sig1);
		sigs.add(sig2);
		sigs.add(sig3);

		final LinkedHashMap<Signature, List<String>> in1 = new LinkedHashMap<>();
		final LinkedHashMap<Signature, List<String>> in2 = new LinkedHashMap<>();
		final LinkedHashMap<Signature, List<String>> in3 = new LinkedHashMap<>();

		final List<String> ls = new ArrayList<String>();
		ls.add("v");
		in1.put(sig1, ls);
		in2.put(sig1, ls);
		in3.put(sig1, ls);

		in1.put(sig2, ls);
		in2.put(sig2, ls);
		in3.put(sig2, ls);

		in1.put(window, ls);
		in2.put(window, ls);
		in1.put(aw, ls);

		final Predicate click = new Predicate("click", "", in2);
		final Predicate fill = new Predicate("fill", "", in1);
		final Predicate go = new Predicate("go", "", in2);
		final List<Predicate> preds = new ArrayList<>();
		preds.add(click);
		preds.add(fill);
		preds.add(go);

		final Predicate click_s = new Predicate("click_semantics",
				"(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => filled_required_in_w_test [Current_window.is_in.t, t]"
						+ System.getProperty("line.separator")
						+ "(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (filled_required_test [t] and unique_test [t])",
				in3);

		// System.out.println(click_s.getContent());
		final Predicate fill_s = new Predicate("fill_semantics", "", in2);
		final Predicate go_s = new Predicate("go_semantics", "", in3);
		preds.add(click_s);
		preds.add(fill_s);
		preds.add(go_s);

		final List<Fact> f = new ArrayList<>();
		final List<Function> fun = new ArrayList<>();
		final List<String> s = new ArrayList<>();

		return new FunctionalitySemantics(sigs, f, preds, fun, s);
	}

	@Test
	public void test() {
		try {
			final FunctionalitySemantics fs = this.getFunctSemantics();
			assertEquals(6, fs.getRun_commands().size());
			assertEquals(
					"run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form))) and (not (filled_required_in_w_test [Current_window.is_in.t, t]))} } for 10",
					fs.getRun_commands().get(0));
			assertEquals(
					"run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form))) and (filled_required_in_w_test [Current_window.is_in.t, t])} } for 10",
					fs.getRun_commands().get(1));
			assertEquals(
					"run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (not (unique_test [t]) and not (filled_required_test [t]))} } for 10",
					fs.getRun_commands().get(2));
			assertEquals(
					"run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (unique_test [t] and not (filled_required_test [t]))} } for 10",
					fs.getRun_commands().get(3));
			assertEquals(
					"run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (not (unique_test [t]) and filled_required_test [t])} } for 10",
					fs.getRun_commands().get(4));
			assertEquals(
					"run {System and {one t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and ((aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0)) and (unique_test [t] and filled_required_test [t])} } for 10",
					fs.getRun_commands().get(5));
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
