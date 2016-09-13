package test.gui.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import test.gui.semantic.alloy.entity.SignatureTest;
import usi.gui.functionality.GUIFunctionality_validate;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.semantic.FunctionalitySemantics;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.structure.GUI;

public class TestCaseSpecGenerationTest {

	public FunctionalitySemantics getFunctSemantics() throws Exception {

		final List<Signature> sigs = new ArrayList<>();
		final Signature window = new Signature("Window", null, true, null, false);
		final Signature aw = new Signature("Action_widget", null, true, null, false);
		final Signature iw = new Signature("Input_widget", null, true, null, false);
		final Signature sw = new Signature("Selectable_widget", null, true, null, false);
		sigs.add(window);
		sigs.add(aw);
		sigs.add(iw);
		sigs.add(sw);

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
		final Predicate select = new Predicate("select", "", in1);

		final List<Predicate> preds = new ArrayList<>();
		preds.add(click);
		preds.add(fill);
		preds.add(go);
		preds.add(select);

		final Predicate click_s = new Predicate(
				"click_semantics",
				"(aw in Ok and Current_window.is_in.t  in Form and (#aw.goes = 1 and aw.goes in Form)) => filled_required_in_w_test [Current_window.is_in.t, t]"
						+ System.getProperty("line.separator")
						+ "(aw in Ok and Current_window.is_in.t  in Form and (not (#aw.goes = 1 and aw.goes in Form)) and #Confirm = 0) => (filled_required_test [t] and unique_test [t])",
				in3);

		// System.out.println(click_s.getContent());
		final Predicate fill_s = new Predicate("fill_semantics", "", in2);
		final Predicate select_s = new Predicate("select_semantics", "", in2);

		final Predicate go_s = new Predicate("go_semantics", "", in3);
		preds.add(click_s);
		preds.add(fill_s);
		preds.add(go_s);
		preds.add(select_s);

		final List<Fact> f = new ArrayList<>();
		final List<Function> fun = new ArrayList<>();
		final List<String> s = new ArrayList<>();

		return new FunctionalitySemantics(sigs, f, preds, fun, s);
	}

	@Test
	public void test() {

		try {
			final Instance_GUI_pattern inst = new Instance_GUI_pattern(new GUI(),
					new GUI_Pattern(), new ArrayList<Instance_window>());
			final FunctionalitySemantics fs = this.getFunctSemantics();

			final Wrapper wr = new Wrapper(inst, new GUI());
			final List<String> runs = wr.generate(fs);

			assertEquals(6, runs.size());

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	class Wrapper extends GUIFunctionality_validate {

		public Wrapper(final Instance_GUI_pattern instancePattern, final GUI gui) throws Exception {

			super(instancePattern, gui);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void init() {

		}

		@Override
		public void generate_run_commands(final FunctionalitySemantics sem) throws Exception {

			if (sem == null) {
				return;
			} else {
				super.generate_run_commands(sem);
			}
		}

		public List<String> generate(final FunctionalitySemantics sem) throws Exception {

			super.generate_run_commands(sem);
			return super.getAllSemanticCases();
		}
	}
}
