package test.gui.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import test.gui.GUIPatternMaker;
import test.gui.GUIStructureMaker;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.semantic.FunctionalitySemantics;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.alloy.entity.AlloyEntity;
import usi.gui.semantic.alloy.entity.BinaryRelation;
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.semantic.alloy.entity.TernaryRelation;
import usi.gui.structure.GUI;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class AlloyUtilsTest {

	@Test
	public void testParserFileAssertSignatures() throws Exception {

		final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new File(
				"./files/for_test/alloy/CRUD.als"));
		assertNotNull(model);

		assertEquals(12, model.getSignatures().size());

		final Signature sigReq = searchSignatureInList(model.getSignatures(), "Required");
		assertNotNull(sigReq);
		assertFalse(sigReq.isAbstract_());
		assertNotNull(sigReq.getParent());
		assertNull(sigReq.getCardinality());

		final Signature sigOperation = searchSignatureInList(model.getSignatures(), "Operation");
		assertNotNull(sigOperation);

		final Signature sigAdd = searchSignatureInList(model.getSignatures(), "Add");
		assertNotNull(sigAdd);
		assertFalse(sigAdd.isAbstract_());
		assertNotNull(sigAdd.getParent());
		assertEquals(Cardinality.ONE, sigAdd.getCardinality());

		assertEquals(sigOperation, sigAdd.getParent().get(0));

		final Signature sigField = searchSignatureInList(model.getSignatures(), "Field");
		assertNotNull(sigField);
		assertTrue(sigField.isAbstract_());
		assertEquals(0, sigField.getParent().size());

		final Signature sigTime = searchSignatureInList(model.getSignatures(), "Time");
		final Signature sigObj = searchSignatureInList(model.getSignatures(), "Object");
		assertNotNull(sigTime);
		assertNotNull(sigObj);

		final Signature sigList = searchSignatureInList(model.getSignatures(), "List");
		assertEquals(1, sigList.getTernary_relations().size());
		final TernaryRelation terList = sigList.getTernary_relations().values().iterator().next();
		assertEquals(sigTime, terList.getCo_domain());
		assertEquals(sigObj, terList.getMiddle_domain());
		assertEquals("elements", terList.getName());
		assertEquals(2, sigObj.getBinary_relations().size());
		assertEquals(0, sigObj.getTernary_relations().size());

		final Signature sigField1 = searchSignatureInList(model.getSignatures(), "Field1");

		final BinaryRelation binList = sigObj.getBinary_relations().values().iterator().next();
		assertEquals(sigField1, binList.getCo_domain());
		assertEquals("field1", binList.getName());

	}

	@Test
	public void testParserFileAssertPredicates() throws Exception {

		final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new File(
				"./files/for_test/alloy/CRUD.als"));
		assertNotNull(model);
		final List<Predicate> predicates = model.getPredicates();
		assertNotNull(predicates);

		assertEquals(18, predicates.size());

		final Predicate p_noExisitingObjectChange = (Predicate) searchElementInList(predicates,
				"noExisitingObjectChange");
		assertNotNull(p_noExisitingObjectChange);
		final String content_p_1 = p_noExisitingObjectChange.getContent();
		assertNotNull(content_p_1);
		// Note that I cannot asset the content of the pred taken from the file
		// due to Alloy API transforms it.
		assertTrue(content_p_1.contains("all o"));

		final Signature sigTime = searchSignatureInList(model.getSignatures(), "Time");
		final Signature sigList = searchSignatureInList(model.getSignatures(), "List");

		final List<String> inList = p_noExisitingObjectChange.getInputs().get(sigList);
		assertNotNull(inList);
		assertTrue(inList.contains("l"));

		final List<String> insigTime = p_noExisitingObjectChange.getInputs().get(sigTime);
		assertNotNull(insigTime);
		assertTrue(insigTime.contains("t"));

	}

	@Test
	public void testParserFileAssertFunction() throws Exception {

		final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new File(
				"./files/for_test/alloy/CRUD.als"));
		assertNotNull(model);
		final List<Function> functions = model.getFunctions();
		assertNotNull(functions);
		assertEquals(1, functions.size());

		final Function function = functions.get(0);
		assertEquals(1, function.getInputs().keySet().size());
		final Signature sigObj = searchSignatureInList(model.getSignatures(), "Object");
		assertTrue(function.getInputs().keySet().contains(sigObj));

		assertNotNull(function.getContent());

		assertNotNull(function.getResult());
	}

	/**
	 * Currently this test fails due to the generated model has errors
	 *
	 * @throws Exception
	 */
	@Test
	public void testComplete() throws Exception {

		final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new File(
				"./files/for_test/alloy/CRUD.als"));
		final String modelString = model.toString();
		final edu.mit.csail.sdg.alloy4compiler.ast.Module mAlloy = AlloyUtil
				.compileAlloyModel(modelString);
		assertNotNull(mAlloy);
		assertEquals(mAlloy.getAllSigs().size(), model.getSignatures().size());

	}

	@Test
	public void testExtractProperty() throws Exception {

		final GUI gui = GUIStructureMaker.instance1();

		final GUI_Pattern pattern = GUIPatternMaker.instance1();

		final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new File(
				"./files/for_test/alloy/GUI_ADD.als"));
		pattern.setSemantics(FunctionalitySemantics.instantiate(model));

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);
		assertEquals(1, res.size());
		assertEquals(2, res.get(0).getWindows().size());

		final Instance_GUI_pattern in = res.get(0);

		final SpecificSemantics specsem = SpecificSemantics.generate(in);
		specsem.generate_run_commands();

		// Save it, and verify if it can be reloaded
		final String plainConcreteModel = specsem.toString();

		final File fileConcreteModel = AlloyUtil.saveModelInFile(plainConcreteModel,
				"./files/for_test/alloy/generated_model.als");

		final Module moduleAlloyMit = AlloyUtil.compileAlloyModel(fileConcreteModel);
		System.out.println(specsem);

		// Now, let's see if there is a solution
		final Command command = moduleAlloyMit.getAllCommands().get(3);
		final A4Solution asol = AlloyUtil.runCommand(moduleAlloyMit, command);
		assertTrue(asol.satisfiable());
		final String out = AlloyUtil.extractProperty(asol, specsem);
		System.out.println(out);
		assertTrue(out
				.equals("one Field_0,Field_1:Property_unique|Property_unique = (Field_0+Field_1) and Property_required = (Field_1) and Field_0.associated_to = (Input_widget_iw2) and Field_1.associated_to = (Input_widget_iw1)")
				|| out.equals("one Field_1:Property_required,Field_0:Property_unique|Property_required = (Field_1) and Property_unique = (Field_0+Field_1) and Field_1.associated_to = (Input_widget_iw1) and Field_0.associated_to = (Input_widget_iw2)"));
	}

	private static Signature searchSignatureInList(final List<Signature> signatures,
			final String label) {

		for (final Signature signature : signatures) {
			if (signature.getIdentifier().equals(label)) {
				return signature;
			}
		}
		return null;
	}

	private static AlloyEntity searchElementInList(final List<? extends AlloyEntity> elements,
			final String label) {

		for (final AlloyEntity element : elements) {
			if (element.getIdentifier().equals(label)) {
				return element;
			}
		}
		// Any element found with that id.
		return null;
	}
}
