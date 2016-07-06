package usi.gui;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import usi.gui.structure.Action_widget;
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

	public GUIWriter() {

	}

	public Element writeWindow(final Window in, final boolean root) throws Exception {

		// the a document is created
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document tmp = docBuilder.newDocument();

		final Element out = tmp.createElement("window");
		out.setAttribute("id", in.getId());
		// position
		String pos = String.valueOf(in.getX()) + ":" + String.valueOf(in.getY());
		Element node = tmp.createElement("pos");
		node.setTextContent(pos);
		out.appendChild(node);
		// class
		node = tmp.createElement("class");
		node.setTextContent(in.getClasss());
		out.appendChild(node);
		// title is added
		node = tmp.createElement("title");
		node.setTextContent(in.getLabel());
		out.appendChild(node);
		// modal is added
		node = tmp.createElement("modal");
		node.setTextContent(String.valueOf(in.isModal()));
		out.appendChild(node);
		// root is added
		node = tmp.createElement("root");
		node.setTextContent(String.valueOf(root));
		out.appendChild(node);

		// action widgets are added
		for (final Action_widget aw : in.getActionWidgets()) {
			node = tmp.createElement("action_widget");
			node.setAttribute("id", aw.getId());
			out.appendChild(node);
			// position
			pos = String.valueOf(aw.getX()) + ":" + String.valueOf(aw.getY());
			Element subnode = tmp.createElement("pos");
			subnode.setTextContent(pos);
			node.appendChild(subnode);
			// class
			subnode = tmp.createElement("class");
			subnode.setTextContent(aw.getClasss());
			node.appendChild(subnode);
			// label
			if (aw.getLabel() != null && aw.getLabel().length() > 0) {
				subnode = tmp.createElement("label");
				subnode.setTextContent(aw.getLabel());
				node.appendChild(subnode);
			}
			// descriptor
			if (aw.getDescriptor() != null && aw.getDescriptor().length() > 0) {
				subnode = tmp.createElement("descriptor");
				subnode.setTextContent(aw.getDescriptor());
				node.appendChild(subnode);
			}
		}

		// input widgets are added
		for (final Input_widget iw : in.getInputWidgets()) {
			node = tmp.createElement("input_widget");
			node.setAttribute("id", iw.getId());
			out.appendChild(node);
			// position
			pos = String.valueOf(iw.getX()) + ":" + String.valueOf(iw.getY());
			Element subnode = tmp.createElement("pos");
			subnode.setTextContent(pos);
			node.appendChild(subnode);
			// class
			subnode = tmp.createElement("class");
			subnode.setTextContent(iw.getClasss());
			node.appendChild(subnode);
			// label
			if (iw.getLabel() != null && iw.getLabel().length() > 0) {
				subnode = tmp.createElement("label");
				subnode.setTextContent(iw.getLabel());
				node.appendChild(subnode);
			}
			// descriptor
			if (iw.getDescriptor() != null && iw.getDescriptor().length() > 0) {
				subnode = tmp.createElement("descriptor");
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
			subnode = tmp.createElement("value");
			subnode.setTextContent(value);
			node.appendChild(subnode);
		}

		// selectable widgets are added
		for (final Selectable_widget sw : in.getSelectableWidgets()) {
			node = tmp.createElement("selectable_widget");
			node.setAttribute("id", sw.getId());
			out.appendChild(node);
			// position
			pos = String.valueOf(sw.getX()) + ":" + String.valueOf(sw.getY());
			Element subnode = tmp.createElement("pos");
			subnode.setTextContent(pos);
			node.appendChild(subnode);
			// class
			subnode = tmp.createElement("class");
			subnode.setTextContent(sw.getClasss());
			node.appendChild(subnode);
			// label
			if (sw.getLabel() != null && sw.getLabel().length() > 0) {
				subnode = tmp.createElement("label");
				subnode.setTextContent(sw.getLabel());
				node.appendChild(subnode);
			}
			// descriptor
			if (sw.getDescriptor() != null && sw.getDescriptor().length() > 0) {
				subnode = tmp.createElement("descriptor");
				subnode.setTextContent(sw.getDescriptor());
				node.appendChild(subnode);
			}
			// size
			subnode = tmp.createElement("size");
			subnode.setTextContent(String.valueOf(sw.getSize()));
			node.appendChild(subnode);
			// selected
			subnode = tmp.createElement("selected");
			subnode.setTextContent(String.valueOf(sw.getSelected()));
			node.appendChild(subnode);
		}

		return out;
	}
}
