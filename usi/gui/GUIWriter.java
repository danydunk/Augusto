package usi.gui;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import usi.gui.widgets.Widget;
import usi.gui.widgets.Window;

/**
 * Class that dumps a GUI into a xml file
 * 
 * @author lta
 * 
 */
public class GUIWriter {

	private final List<String> action_widgets_classes;
	private final List<String> input_widgets_classes;
	private final List<String> selectable_widgets_classes;

	public GUIWriter() {

		this.action_widgets_classes = new ArrayList<String>();
		this.input_widgets_classes = new ArrayList<String>();
		this.selectable_widgets_classes = new ArrayList<String>();

		this.action_widgets_classes.add("ButtonUI");
		this.action_widgets_classes.add("MenuItemUI");
		this.action_widgets_classes.add("TabbedPaneUI");

		this.input_widgets_classes.add("FormattedTextFieldUI");
		this.input_widgets_classes.add("PasswordFieldUI");
		this.input_widgets_classes.add("javax.swing.JTextArea");
		this.input_widgets_classes.add("TextAreaUI");
		this.input_widgets_classes.add("TextFieldUI");
		this.input_widgets_classes.add("RadioButtonUI");
		this.input_widgets_classes.add("CheckBoxUI");
		this.input_widgets_classes.add("ComboBoxUI");

		this.selectable_widgets_classes.add("ListUI");
		this.selectable_widgets_classes.add("TableUI");
	}

	public Element writeWindow(final Window in, final boolean root) throws Exception {

		// the a document is created
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document tmp = docBuilder.newDocument();

		final Element out = tmp.createElement("Window");
		out.setAttribute("id", in.getId());
		// title is added
		Element node = tmp.createElement("title");
		node.setTextContent(in.getTitle());
		out.appendChild(node);
		// modal is added
		node = tmp.createElement("modal");
		node.setTextContent(String.valueOf(in.isModal()));
		out.appendChild(node);
		// root is added
		node = tmp.createElement("root");
		node.setTextContent(String.valueOf(root));
		out.appendChild(node);

		for (final Widget w : in.getContained()) {
			node = null;
			// label
			final Element label = tmp.createElement("label");
			if (w.getProperty("label") != null && w.getProperty("label").length() > 0) {
				label.setTextContent(w.getProperty("label"));
			} else {
				if (w.getDescriptor() != null) {
					label.setTextContent(w.getDescriptor());
				} else {
					label.setTextContent("");
				}
			}
			// class
			final Element classs = tmp.createElement("class");
			classs.setTextContent(w.getType());
			// pos
			final Element pos = tmp.createElement("pos");
			pos.setTextContent(w.getProperty("x") + ":" + w.getProperty("y"));

			if (this.action_widgets_classes.contains(w.getType())) {
				node = tmp.createElement("action_widget");
				node.setAttribute("id", w.getId());
				out.appendChild(node);
				node.appendChild(classs);
				node.appendChild(pos);
				node.appendChild(label);

				if (w.getType().equals("TabbedPaneUI")) {
					// TODO: manage tabbed pane
				}
			}

			if (this.input_widgets_classes.contains(w.getType())) {
				node = tmp.createElement("input_widget");
				node.setAttribute("id", w.getId());
				out.appendChild(node);
				node.appendChild(classs);
				node.appendChild(pos);
				node.appendChild(label);
				// value
				final Element value = tmp.createElement("value");
				node.appendChild(value);
				if (w.getProperty("selected") != null) {
					final Element size = tmp.createElement("size");
					node.appendChild(size);
					if (w.getProperty("size") != null) {
						value.setTextContent(w.getProperty("selected"));
						size.setTextContent(w.getProperty("size"));
						node.appendChild(size);
					} else {
						size.setTextContent("2");
						if ("true".equals(w.getProperty("selected"))) {
							value.setTextContent("0");
						} else {
							value.setTextContent("1");
						}
					}
				} else {
					value.setTextContent(w.getProperty("value"));
				}
			}

			if (this.selectable_widgets_classes.contains(w.getType())) {
				node = tmp.createElement("selectable_widget");
				node.setAttribute("id", w.getId());
				out.appendChild(node);
				node.appendChild(classs);
				node.appendChild(pos);
				node.appendChild(label);
				// size
				final Element size = tmp.createElement("size");
				size.setTextContent(w.getProperty("size"));
				node.appendChild(size);
				// selected
				final Element selected = tmp.createElement("selected");
				selected.setTextContent(w.getProperty("selected"));
				node.appendChild(selected);
			}
		}

		return out;
	}
}
