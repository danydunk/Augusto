package usi.gui;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

/**
 * Class that dumps a GUI into a xml file
 *
 * @author lta
 *
 */
public class GUIWriter {

	public static Document writeGUI(final GUI gui) throws Exception {

		if (gui == null) {
			throw new Exception("GUIWriter - writeGUI: null input.");
		}

		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document doc = docBuilder.newDocument();
		final Element gui_tag = doc.createElement("GUI");
		doc.appendChild(gui_tag);

		final List<Action_widget> aws = new ArrayList<>();

		for (final Window w : gui.getWindows()) {
			aws.addAll(w.getActionWidgets());
			final Element el = writeWindow(w, doc);
			gui_tag.appendChild(el);
		}

		for (final Action_widget aw : aws) {
			List<Window> winds = gui.getStaticForwardLinks(aw.getId());
			if (winds.size() > 0) {
				final Element edge = doc.createElement("edge");
				edge.setAttribute("type", "static");
				gui_tag.appendChild(edge);
				final Element from = doc.createElement("from");
				from.setTextContent(aw.getId());
				edge.appendChild(from);
				for (final Window ww : winds) {
					final Element to = doc.createElement("to");
					to.setTextContent(ww.getId());
					edge.appendChild(to);
				}
			}

			winds = gui.getDynamicForwardLinks(aw.getId());
			if (winds.size() > 0) {
				final Element edge = doc.createElement("edge");
				edge.setAttribute("type", "dynamic");
				gui_tag.appendChild(edge);
				final Element from = doc.createElement("from");
				from.setTextContent(aw.getId());
				edge.appendChild(from);
				for (final Window ww : winds) {
					final Element to = doc.createElement("to");
					to.setTextContent(ww.getId());
					edge.appendChild(to);
				}
			}
		}

		return doc;
	}

	public static Element writeWindow(final Window in, final Document doc) throws Exception {

		final Element out = doc.createElement("window");
		out.setAttribute("id", in.getId());
		// position
		String pos = String.valueOf(in.getX()) + ":" + String.valueOf(in.getY());
		Element node = doc.createElement("pos");
		node.setTextContent(pos);
		out.appendChild(node);
		// class
		node = doc.createElement("class");
		node.setTextContent(in.getClasss());
		out.appendChild(node);
		// title is added
		node = doc.createElement("title");
		node.setTextContent(in.getLabel());
		out.appendChild(node);
		// modal is added
		node = doc.createElement("modal");
		node.setTextContent(String.valueOf(in.isModal()));
		out.appendChild(node);
		// root is added
		node = doc.createElement("root");
		node.setTextContent(String.valueOf(in.isRoot()));
		out.appendChild(node);

		// action widgets are added
		for (final Action_widget aw : in.getActionWidgets()) {
			node = doc.createElement("action_widget");
			node.setAttribute("id", aw.getId());
			out.appendChild(node);
			// position
			pos = String.valueOf(aw.getX()) + ":" + String.valueOf(aw.getY());
			Element subnode = doc.createElement("pos");
			subnode.setTextContent(pos);
			node.appendChild(subnode);
			// class
			subnode = doc.createElement("class");
			subnode.setTextContent(aw.getClasss());
			node.appendChild(subnode);
			// label
			if (aw.getLabel() != null) {
				subnode = doc.createElement("label");
				subnode.setTextContent(aw.getLabel());
				node.appendChild(subnode);
			}
			// descriptor
			if (aw.getDescriptor() != null) {
				subnode = doc.createElement("descriptor");
				subnode.setTextContent(aw.getDescriptor());
				node.appendChild(subnode);
			}
		}

		// input widgets are added
		for (final Input_widget iw : in.getInputWidgets()) {
			node = doc.createElement("input_widget");
			node.setAttribute("id", iw.getId());
			out.appendChild(node);
			// position
			pos = String.valueOf(iw.getX()) + ":" + String.valueOf(iw.getY());
			Element subnode = doc.createElement("pos");
			subnode.setTextContent(pos);
			node.appendChild(subnode);
			// class
			subnode = doc.createElement("class");
			subnode.setTextContent(iw.getClasss());
			node.appendChild(subnode);
			// label
			if (iw.getLabel() != null) {
				subnode = doc.createElement("label");
				subnode.setTextContent(iw.getLabel());
				node.appendChild(subnode);
			}
			// descriptor
			if (iw.getDescriptor() != null) {
				subnode = doc.createElement("descriptor");
				subnode.setTextContent(iw.getDescriptor());
				node.appendChild(subnode);
			}
			// value
			String value;
			if (iw instanceof Option_input_widget) {
				final Option_input_widget oiw = (Option_input_widget) iw;
				value = "[options] " + String.valueOf(oiw.getSelected()) + " : "
						+ String.valueOf(oiw.getSize());
			} else {
				value = iw.getValue();
			}
			subnode = doc.createElement("value");
			subnode.setTextContent(value);
			node.appendChild(subnode);
		}

		// selectable widgets are added
		for (final Selectable_widget sw : in.getSelectableWidgets()) {
			node = doc.createElement("selectable_widget");
			node.setAttribute("id", sw.getId());
			out.appendChild(node);
			// position
			pos = String.valueOf(sw.getX()) + ":" + String.valueOf(sw.getY());
			Element subnode = doc.createElement("pos");
			subnode.setTextContent(pos);
			node.appendChild(subnode);
			// class
			subnode = doc.createElement("class");
			subnode.setTextContent(sw.getClasss());
			node.appendChild(subnode);
			// label
			if (sw.getLabel() != null) {
				subnode = doc.createElement("label");
				subnode.setTextContent(sw.getLabel());
				node.appendChild(subnode);
			}
			// descriptor
			if (sw.getDescriptor() != null) {
				subnode = doc.createElement("descriptor");
				subnode.setTextContent(sw.getDescriptor());
				node.appendChild(subnode);
			}
			// size
			subnode = doc.createElement("size");
			subnode.setTextContent(String.valueOf(sw.getSize()));
			node.appendChild(subnode);
			// selected
			subnode = doc.createElement("selected");
			subnode.setTextContent(String.valueOf(sw.getSelected()));
			node.appendChild(subnode);
		}

		return out;
	}
}
