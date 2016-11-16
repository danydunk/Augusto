package src.usi.gui.functionality.instance;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import src.usi.gui.GUIWriter;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;

public class Instance_GUI_patternWriter {

	public static Document writeInstanceGUIPattern(final Instance_GUI_pattern instance)
			throws Exception {

		if (instance == null) {
			throw new Exception("Instance_GUI_patternWriter - writeInstanceGUIPattern: null input.");
		}

		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document doc = docBuilder.newDocument();
		// root node
		final Element instance_tag = doc.createElement("instance_GUI_pattern");
		doc.appendChild(instance_tag);
		// GUI Pattern
		final Element guipatternn = doc.createElement("GUI_pattern");
		guipatternn.setTextContent(instance.getGuipattern().getName());
		instance_tag.appendChild(guipatternn);
		// windows
		final Element windowsn = doc.createElement("windows");
		instance_tag.appendChild(windowsn);
		for (final Instance_window w : instance.getWindows()) {
			final Element wn = writeWindow(w, doc);
			windowsn.appendChild(wn);
		}
		// Semantics
		final Element semanticsn = doc.createElement("semantics");
		semanticsn.setTextContent(instance.getSemantics().toString());
		instance_tag.appendChild(semanticsn);
		// GUI
		final Document guidoc = GUIWriter.writeGUI(instance.getGui());
		final Node guin = guidoc.getFirstChild();
		assert (guin.getNodeName().equals("GUI"));
		final Node gui = doc.adoptNode(guin);
		instance_tag.appendChild(gui);
		return doc;
	}

	private static Element writeWindow(final Instance_window w, final Document doc)
			throws Exception {

		final Element root = doc.createElement("instance_window");
		root.setAttribute("instance_id", w.getInstance().getId());
		root.setAttribute("pattern_id", w.getPattern().getId());

		for (final Pattern_action_widget paw : w.getPattern().getActionWidgets()) {
			for (final Action_widget aw : w.getAWS_for_PAW(paw.getId())) {
				final Element mapping = doc.createElement("mapping");
				root.appendChild(mapping);
				final Element pid = doc.createElement("pattern");
				pid.setTextContent(paw.getId());
				final Element iid = doc.createElement("instance");
				iid.setTextContent(aw.getId());
				mapping.appendChild(pid);
				mapping.appendChild(iid);
			}
		}

		for (final Pattern_input_widget piw : w.getPattern().getInputWidgets()) {
			for (final Input_widget iw : w.getIWS_for_PIW(piw.getId())) {
				final Element mapping = doc.createElement("mapping");
				root.appendChild(mapping);
				final Element pid = doc.createElement("pattern");
				pid.setTextContent(piw.getId());
				final Element iid = doc.createElement("instance");
				iid.setTextContent(iw.getId());
				mapping.appendChild(pid);
				mapping.appendChild(iid);
			}
		}

		for (final Pattern_selectable_widget psw : w.getPattern().getSelectableWidgets()) {
			for (final Selectable_widget sw : w.getSWS_for_PSW(psw.getId())) {
				final Element mapping = doc.createElement("mapping");
				root.appendChild(mapping);
				final Element pid = doc.createElement("pattern");
				pid.setTextContent(psw.getId());
				final Element iid = doc.createElement("instance");
				iid.setTextContent(sw.getId());
				mapping.appendChild(pid);
				mapping.appendChild(iid);
			}
		}

		return root;
	}

}
