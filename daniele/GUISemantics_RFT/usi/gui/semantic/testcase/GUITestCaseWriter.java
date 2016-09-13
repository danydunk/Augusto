package usi.gui.semantic.testcase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GUITestCaseWriter {

	private Document doc;

	public Document writeGUITestCase(final GUITestCase tc) throws Exception {

		if (tc == null) {
			throw new Exception("GUITestCaseWriter - writeGUITestCase: null input.");
		}

		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		this.doc = docBuilder.newDocument();
		final Element tc_tag = this.doc.createElement("TestCase");
		this.doc.appendChild(tc_tag);

		for (final GUIAction act : tc.getActions()) {
			final Element action = this.doc.createElement("action");
			tc_tag.appendChild(action);

			final Element type = this.doc.createElement("type");
			action.appendChild(type);
		}
		return null;
	}
}
