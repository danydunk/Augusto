package test.gui.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import test.gui.pattern.GUIPatternMaker;
import test.gui.structure.GUIStructureMaker;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.semantic.FunctionalitySemantics;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.alloy.entity.AlloyEntity;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.structure.GUI;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;

public class SpecificSemanticTest {

	@Test
	public void testFromPatternToAlloy1() throws Exception {

		final GUI gui = GUIStructureMaker.instance1();

		final GUI_Pattern pattern = GUIPatternMaker.instance1();

		final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new File(
				"./files/for_test/alloy/GUI_ADD.als"));
		pattern.setSemantics(FunctionalitySemantics.instantiate(model));

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);
		assertEquals(1, res.size());
		assertEquals(2, res.get(0).getWindows().size());

		Instance_window ww1 = null;
		Instance_window ww2 = null;
		for (final Instance_window ww : res.get(0).getWindows()) {
			switch (ww.getInstance().getId()) {
			case "w1":
				ww1 = ww;
				break;
			case "w2":
				ww2 = ww;
				break;
			}
		}
		assertTrue(ww1 != null);
		assertTrue(ww2 != null);

		final Instance_GUI_pattern in = res.get(0);

		final SpecificSemantics specsem = SpecificSemantics.generate(in);
		// System.out.println(specsem);
		assertNotNull(specsem);

		// / One is the abstract + the 2 matched
		assertEquals(3, specsem.getConcrete_windows().size());

		// System.out.println(specsem.toString());
		// /
		final Signature s1 = AlloyUtil.searchSignatureInList(specsem.getSignatures(), "Window_w1");
		assertNotNull(s1);
		final Signature s2 = AlloyUtil.searchSignatureInList(specsem.getSignatures(), "Window_w2");
		assertNotNull(s2);

		//
		// Win 2 has two inputs
		final AlloyEntity fcw2 = AlloyUtil.searchElementInList(specsem.getFacts(), "Window_w2_iws");
		assertNotNull(fcw2);
		final Fact finWin2 = (Fact) fcw2;
		assertTrue(finWin2.getContent().contains("+"));

		// Win 1 has not inputs
		final AlloyEntity fcw1 = AlloyUtil.searchElementInList(specsem.getFacts(), "Window_w1_iws");
		assertNull(fcw1);

		final AlloyEntity fcw1action = AlloyUtil.searchElementInList(specsem.getFacts(),
				"Window_w1_aws");
		assertNotNull(fcw1action);
		final Fact factw1 = (Fact) fcw1action;
		// The widget has two actions, add and test, but only add is mapped to
		// the alloy model.
		assertFalse(factw1.getContent().contains("+"));

		final AlloyEntity fcw2action = AlloyUtil.searchElementInList(specsem.getFacts(),
				"Window_w2_aws");
		assertNotNull(fcw2action);
		final Fact factw2 = (Fact) fcw2action;
		// Two actions (merged by + char) are mapped to alloy model.
		assertTrue(factw2.getContent().contains("+"));

		// Save it, and verify if it can be reloaded
		final String plainConcreteModel = specsem.toString();

		final File fileConcreteModel = AlloyUtil.saveModelInFile(plainConcreteModel,
				"./files/for_test/alloy/generated_model.als");
		final Alloy_Model loadedModelAlloyComplete = AlloyUtil
				.loadAlloyModelFromFile(fileConcreteModel);
		assertNotNull(loadedModelAlloyComplete);

		assertEquals(specsem.getSignatures().size(), loadedModelAlloyComplete.getSignatures()
				.size());

		assertEquals(specsem.getPredicates().size(), loadedModelAlloyComplete.getPredicates()
				.size());

		assertEquals(specsem.getFunctions().size(), loadedModelAlloyComplete.getFunctions().size());

		assertEquals(specsem.getFacts().size(), loadedModelAlloyComplete.getFacts().size());

		// Now with the MIT API

		final Module moduleAlloyMit = AlloyUtil.compileAlloyModel(fileConcreteModel);
		assertEquals(moduleAlloyMit.getAllSigs().size(), loadedModelAlloyComplete.getSignatures()
				.size());
		assertEquals(moduleAlloyMit.getAllFacts().size(), loadedModelAlloyComplete.getFacts()
				.size());
		// The mit API includes predicates in function.
		assertTrue(moduleAlloyMit.getAllFunc().size() >= loadedModelAlloyComplete.getFunctions()
				.size());

	}
}
