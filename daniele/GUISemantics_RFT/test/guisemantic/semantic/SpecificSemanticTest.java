package test.guisemantic.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import usi.guifunctionality.GUIFunctionality_search;
import usi.guifunctionality.mapping.Instance_GUI_pattern;
import usi.guifunctionality.mapping.Instance_window;
import usi.guipattern.Boolean_regexp;
import usi.guipattern.Cardinality;
import usi.guipattern.GUI_Pattern;
import usi.guipattern.Pattern_action_widget;
import usi.guipattern.Pattern_input_widget;
import usi.guipattern.Pattern_window;
import usi.guisemantic.FunctionalitySemantics;
import usi.guisemantic.SpecificSemantics;
import usi.guisemantic.alloy.AlloyUtil;
import usi.guisemantic.alloy.Alloy_Model;
import usi.guisemantic.alloy.entity.AlloyEntity;
import usi.guisemantic.alloy.entity.Fact;
import usi.guisemantic.alloy.entity.Signature;
import usi.guistructure.Action_widget;
import usi.guistructure.GUI;
import usi.guistructure.Input_widget;
import usi.guistructure.Window;

public class SpecificSemanticTest {

	@Test
	public void testFromPatternToAlloy1() throws Exception {

		final GUI gui = new GUI();
		// w1
		final Window w1 = new Window("w1", "init");
		final Action_widget aw1 = new Action_widget("aw1", "add");
		final Action_widget aw2 = new Action_widget("aw2", "test");
		w1.addActionWidget(aw1);
		w1.addActionWidget(aw2);
		gui.addWindow(w1);
		// w2
		final Window w2 = new Window("w2", "form");
		final Action_widget aw3 = new Action_widget("aw3", "next");
		final Action_widget aw4 = new Action_widget("aw4", "back");
		final Input_widget iw1 = new Input_widget("iw1", "field1", "");
		final Input_widget iw2 = new Input_widget("iw2", "field2", "");
		w2.addActionWidget(aw3);
		w2.addActionWidget(aw4);
		w2.addInputWidget(iw1);
		w2.addInputWidget(iw2);
		gui.addWindow(w2);
		// w3
		final Window w3 = new Window("w3", "other");
		final Action_widget aw5 = new Action_widget("aw5", "add");
		final Input_widget iw3 = new Input_widget("iw3", "field3", "");
		w3.addActionWidget(aw5);
		w3.addInputWidget(iw3);
		gui.addWindow(w3);
		// edges
		gui.addEdge(aw1, w2);
		gui.addEdge(aw2, w3);
		gui.addEdge(aw4, w1);

		final GUI_Pattern pattern = new GUI_Pattern();
		// pw1
		final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "Initial", Boolean_regexp.ANY,
				Boolean_regexp.ANY);
		final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "Trigger");
		pw1.addActionWidget(paw1);
		pattern.addWindow(pw1);
		// pw2
		final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "Form", Boolean_regexp.ANY,
				Boolean_regexp.ANY);
		final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "Ok");
		final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "Cancel");
		final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "Input_widget",
				null);
		pw2.addActionWidget(paw2);
		pw2.addActionWidget(paw3);
		pw2.addInputWidget(piw1);
		pattern.addWindow(pw2);
		// pw3
		final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "Confirm", Boolean_regexp.TRUE,
				Boolean_regexp.ANY);
		final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "Ok");
		final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "Cancel");
		pw3.addActionWidget(paw4);
		pw3.addActionWidget(paw5);
		pattern.addWindow(pw3);
		// edges
		pattern.addEdge(paw1, pw2);
		pattern.addEdge(paw3, pw1);
		pattern.addEdge(paw2, pw3);
		pattern.addEdge(paw5, pw2);

		final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new File("./files/for_test/alloy/GUI_ADD.als"));
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

		/// One is the abstract + the 2 matched
		assertEquals(3, specsem.getConcrete_windows().size());

		// System.out.println(specsem.toString());
		///
		final Signature s1 = AlloyUtil.searchSignatureInList(specsem.getSignatures(), "Window_" + w1.getId());
		assertNotNull(s1);
		final Signature s2 = AlloyUtil.searchSignatureInList(specsem.getSignatures(), "Window_" + w2.getId());
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

		final AlloyEntity fcw1action = AlloyUtil.searchElementInList(specsem.getFacts(), "Window_w1_aws");
		assertNotNull(fcw1action);
		final Fact factw1 = (Fact) fcw1action;
		// The widget has two actions, add and test, but only add is mapped to
		// the alloy model.
		assertFalse(factw1.getContent().contains("+"));

		final AlloyEntity fcw2action = AlloyUtil.searchElementInList(specsem.getFacts(), "Window_w2_aws");
		assertNotNull(fcw2action);
		final Fact factw2 = (Fact) fcw2action;
		// Two actions (merged by + char) are mapped to alloy model.
		assertTrue(factw2.getContent().contains("+"));

		// Save it, and verify if it can be reloaded
		final String plainConcreteModel = specsem.toString();

		final File fileConcreteModel = AlloyUtil.saveModelInFile(plainConcreteModel,
				"./files/for_test/alloy/generated_model.als");
		final Alloy_Model loadedModelAlloyComplete = AlloyUtil.loadAlloyModelFromFile(fileConcreteModel);
		assertNotNull(loadedModelAlloyComplete);

		assertEquals(specsem.getSignatures().size(), loadedModelAlloyComplete.getSignatures().size());

		assertEquals(specsem.getPredicates().size(), loadedModelAlloyComplete.getPredicates().size());

		assertEquals(specsem.getFunctions().size(), loadedModelAlloyComplete.getFunctions().size());

		assertEquals(specsem.getFacts().size(), loadedModelAlloyComplete.getFacts().size());

		// Now with the MIT API

		final Module moduleAlloyMit = AlloyUtil.compileAlloyModel(fileConcreteModel);
		assertEquals(moduleAlloyMit.getAllSigs().size(), loadedModelAlloyComplete.getSignatures().size());
		assertEquals(moduleAlloyMit.getAllFacts().size(), loadedModelAlloyComplete.getFacts().size());
		// The mit API includes predicates in function.
		assertTrue(moduleAlloyMit.getAllFunc().size() >= loadedModelAlloyComplete.getFunctions().size());

		boolean anySat = false;
		// Now, let's see if there is a solution
		for (final Command command : moduleAlloyMit.getAllCommands()) {
			// Command command = moduleAlloyMit.getAllCommands().get(0);
			final A4Solution asol = AlloyUtil.runCommand(moduleAlloyMit, command);
			assertNotNull(asol);
			anySat = anySat || (asol.satisfiable());
			// if (asol.satisfiable()) {
			// System.out.println("Sat command\n " + command);
			// }
		}
		assertTrue(anySat);
	}
}
