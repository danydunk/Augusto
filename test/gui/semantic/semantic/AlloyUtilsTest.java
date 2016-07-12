package test.gui.semantic.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import usi.gui.pattern.Cardinality;
import usi.guisemantic.alloy.AlloyUtil;
import usi.guisemantic.alloy.Alloy_Model;
import usi.guisemantic.alloy.entity.AlloyEntity;
import usi.guisemantic.alloy.entity.BinaryRelation;
import usi.guisemantic.alloy.entity.Function;
import usi.guisemantic.alloy.entity.Predicate;
import usi.guisemantic.alloy.entity.Signature;
import usi.guisemantic.alloy.entity.TernaryRelation;

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
