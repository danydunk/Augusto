package src.usi.testcase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import src.usi.gui.GUIWriter;
import src.usi.testcase.structure.Clean;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;
import src.usi.testcase.structure.Select_doubleclick;

public class GUITestCaseWriter {

	public static Document writeGUITestCase(final GUITestCase tc) throws Exception {

		if (tc == null) {
			throw new Exception("GUITestCaseWriter - writeGUITestCase: null input.");
		}

		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document doc = docBuilder.newDocument();
		final Element tc_tag = doc.createElement("GUITestCase");
		final Element run = doc.createElement("sem_prop");
		run.setTextContent(tc.getSemanticProperty());
		tc_tag.appendChild(run);
		doc.appendChild(tc_tag);

		for (final GUIAction act : tc.getActions()) {
			final Element action = doc.createElement("action");
			tc_tag.appendChild(action);
			final Element type = doc.createElement("type");
			action.appendChild(type);

			if (act instanceof Click) {
				type.setTextContent("click");
			}
			if (act instanceof Fill) {
				final Fill f = (Fill) act;
				type.setTextContent("fill");
				if (f.getInput() != null) {
					final Element v = doc.createElement("input");
					v.setTextContent(f.getInput());
					action.appendChild(v);
				}
			}
			if (act instanceof Clean) {
				final Clean f = (Clean) act;
				type.setTextContent("clean");
			}
			if (act instanceof Select) {
				final Select s = (Select) act;
				type.setTextContent("select");
				final Element abst = doc.createElement("abstract");
				abst.setTextContent(String.valueOf(s.isAbstract()));
				action.appendChild(abst);
				final Element ind = doc.createElement("index");
				ind.setTextContent(String.valueOf(s.getIndex()));
				action.appendChild(ind);
			}
			if (act instanceof Select_doubleclick) {
				type.setTextContent("select_doubleclick");
			}
			final Element widget = doc.createElement("widget");
			widget.setTextContent(act.getWidget().getId());
			action.appendChild(widget);
			final Element source = doc.createElement("source_window");
			action.appendChild(source);
			source.appendChild(GUIWriter.writeWindow(act.getWindow(), doc));

			if (act.getOracle() != null) {
				final Element oracle = doc.createElement("oracle");
				action.appendChild(oracle);
				oracle.appendChild(GUIWriter.writeWindow(act.getOracle(), doc));
			}
		}
		return doc;
	}
}
