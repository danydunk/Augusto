package usi.guistructure.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import usi.guistructure.Action_widget;
import usi.guistructure.GUI;
import usi.guistructure.Widget;
import usi.guistructure.Window;
import usi.guistructure.converter.interfaces.IConverter;
import usi.xml.XMLUtil;

/**
 *
 * @author daniele converter for GUI Ripping technology
 */
public class GUIRippingConverter implements IConverter {

	private Document converted_xml;
	private final List<String> action_widgets_classes;
	private final List<String> input_widgets_classes;
	private final List<String> filter_selectable_widgets_classes;
	// used to track to which window a widget belongs
	private Map<String, String> action_w_window_mapping;
	// used to track to which window a widget belongs
	private Map<String, String> widget_window_mapping;
	private static boolean CONTEXT_ANALYSIS = true;
	private static boolean USE_CONTAINER_TITLE_TEXT = true;
	private static boolean LOCAL = true;
	private GUIRippingContextAnalyzer context_analyzer;
	private final Map<String, String> class_replayableaction;

	public GUIRippingConverter() throws Exception {
		this.action_widgets_classes = new ArrayList<>();
		this.input_widgets_classes = new ArrayList<>();
		this.filter_selectable_widgets_classes = new ArrayList<>();

		this.class_replayableaction = new HashMap<>();

		this.action_widgets_classes.add("javax.swing.JButton");
		this.action_widgets_classes.add("javax.swing.JMenu");
		this.action_widgets_classes.add("javax.swing.JMenuItem");
		this.action_widgets_classes.add("javax.swing.JTabbedPane");
		// only for BUDDI
		this.action_widgets_classes.add("org.homeunix.thecave.buddi.view.menu.items");
		this.action_widgets_classes.add("javax.swing.plaf.basic.BasicOptionPaneUI$ButtonFactory$ConstrainedButton");

		this.class_replayableaction.put("javax.swing.JButton", "edu.umd.cs.guitar.event.JFCActionHandler");
		this.class_replayableaction.put("javax.swing.JMenu", "edu.umd.cs.guitar.event.JFCActionHandler");
		this.class_replayableaction.put("javax.swing.JMenuItem", "edu.umd.cs.guitar.event.JFCActionHandler");
		this.class_replayableaction.put("javax.swing.JTabbedPane", "edu.umd.cs.guitar.event.JFCActionHandler");
		this.class_replayableaction.put("org.homeunix.thecave.buddi.view.menu.items",
				"edu.umd.cs.guitar.event.JFCActionHandler");
		this.class_replayableaction.put("javax.swing.plaf.basic.BasicOptionPaneUI$ButtonFactory$ConstrainedButton",
				"edu.umd.cs.guitar.event.JFCActionHandler");

		this.input_widgets_classes.add("javax.swing.JTextField");
		this.input_widgets_classes.add("javax.swing.JPasswordField");
		this.input_widgets_classes.add("javax.swing.JTextArea");
		// only for BUDDI
		this.input_widgets_classes.add("ca.digitalcave.moss.swing.MossHintTextArea");
		this.input_widgets_classes.add("ca.digitalcave.moss.swing.MossHintTextField");
		this.input_widgets_classes.add("ca.digitalcave.moss.swing.MossDecimalField");
		this.input_widgets_classes.add("ca.digitalcave.moss.swing.MossScrollingComboBox");
		this.input_widgets_classes.add("ca.digitalcave.moss.swing.MossSearchField");

		this.class_replayableaction.put("javax.swing.JTextField", "edu.umd.cs.guitar.event.JFCEditableTextHandler");
		this.class_replayableaction.put("javax.swing.JPasswordField", "edu.umd.cs.guitar.event.JFCEditableTextHandler");
		this.class_replayableaction.put("javax.swing.JTextArea", "edu.umd.cs.guitar.event.JFCEditableTextHandler");
		this.class_replayableaction.put("ca.digitalcave.moss.swing.MossHintTextArea",
				"edu.umd.cs.guitar.event.JFCEditableTextHandler");
		this.class_replayableaction.put("ca.digitalcave.moss.swing.MossHintTextField",
				"edu.umd.cs.guitar.event.JFCEditableTextHandler");
		this.class_replayableaction.put("ca.digitalcave.moss.swing.MossDecimalField",
				"edu.umd.cs.guitar.event.JFCEditableTextHandler");
		this.class_replayableaction.put("ca.digitalcave.moss.swing.MossScrollingComboBox",
				"edu.umd.cs.guitar.event.JFCSelectionHandler");
		this.class_replayableaction.put("ca.digitalcave.moss.swing.MossSearchField",
				"edu.umd.cs.guitar.event.JFCEditableTextHandler");

		this.filter_selectable_widgets_classes.add("org.homeunix.thecave.buddi.view.menu.bars.BuddiMenuBar");
		this.filter_selectable_widgets_classes.add("ca.digitalcave.moss.swing.MossScrollingComboBox");
		this.filter_selectable_widgets_classes.add("javax.swing.JTabbedPane");
		this.filter_selectable_widgets_classes.add("javax.swing.JMenuBar");

		this.class_replayableaction.put("javax.swing.JList", "edu.umd.cs.guitar.event.JFCSelectionHandler");
		this.class_replayableaction.put("com.apple.laf.AquaFileChooserUI$JTableExtension",
				"edu.umd.cs.guitar.event.JFCSelectionHandler");
	}

	public GUIRippingConverter(final boolean context_analysis, final boolean local_analysis,
			final boolean use_container_title_text) throws Exception {
		this();
		CONTEXT_ANALYSIS = context_analysis;
		LOCAL = local_analysis;
		USE_CONTAINER_TITLE_TEXT = use_container_title_text;
	}

	private void initConvertedXML() throws Exception {

		// the output xml is created
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		this.converted_xml = docBuilder.newDocument();
		this.action_w_window_mapping = new HashMap<>();
		this.widget_window_mapping = new HashMap<>();
	}

	@Override
	public Document convert(final Object[] inputs) throws Exception {

		this.initConvertedXML();
		if (inputs.length != 2) {
			throw new Exception("GUIRippingConverter: wrong inputs GUIRippingConvert");
		}

		final Document gui = (Document) inputs[0];
		final Document efg = (Document) inputs[1];
		this.GUIRippingConvert_GUI(gui);
		this.GUIRippingConvert_EFG(efg);
		return this.converted_xml;
	}

	/**
	 * Method that parses the .GUI file
	 *
	 * @param GUI
	 * @throws Exception
	 */
	private void GUIRippingConvert_GUI(final Document GUI) throws Exception {

		// the root element is created
		final Element rootElement = this.converted_xml.createElement("GUI");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:noNamespaceSchemaLocation", "GUI_schema.xsd");
		this.converted_xml.appendChild(rootElement);

		// the parsing starts
		final NodeList struct = GUI.getElementsByTagName("GUIStructure");
		if (struct.getLength() != 1) {
			throw new Exception("GUIStructureConverter: wrong GUI file for GUIRippingConvert");
		}

		final List<Element> guis = XMLUtil.getChildrenElements((Element) struct.item(0));

		int cont = 0;
		// iterates through the GUI tag
		for (final Element el : guis) {
			if (CONTEXT_ANALYSIS) {
				this.context_analyzer = new GUIRippingContextAnalyzer(el, LOCAL, USE_CONTAINER_TITLE_TEXT);
			}
			final List<Element> gui_children = XMLUtil.getChildrenElements(el);

			if (gui_children.size() != 2) {
				throw new Exception("GUIStructureConverter: wrong GUI file for GUIRippingConvert");
			}

			final Element window = gui_children.get(0);
			final Element container = gui_children.get(1);

			if (!"Window".equals(window.getNodeName()) || !"Container".equals(container.getNodeName())) {
				throw new Exception("GUIStructureConverter: wrong GUI file for GUIRippingConvert");
			}

			// a new node window is created
			final Element w = this.converted_xml.createElement("window");
			rootElement.appendChild(w);
			final String w_id = "w" + (++cont);
			w.setAttribute("id", w_id); // the id is in the form "wX"

			// check the attributes tag
			final List<Element> attributes = XMLUtil.getChildrenElements(window);
			if (attributes.size() != 1 || !"Attributes".equals(attributes.get(0).getNodeName())) {
				throw new Exception("GUIStructureConverter: wrong GUI file for GUIRippingConvert");
			}

			// iterates over all the properties tag
			final List<Element> properties = XMLUtil.getChildrenElements(attributes.get(0));
			for (final Element n : properties) {
				final List<Element> n_children = XMLUtil.getChildrenElements(n);
				if (!"Property".equals(n.getNodeName()) || n_children.size() != 2
						|| !"Name".equals(n_children.get(0).getNodeName())) {
					throw new Exception("GUIStructureConverter: wrong GUI file for GUIRippingConvert");
				}

				// the properties we are interested in are saved
				switch (n_children.get(0).getTextContent()) {
				case "Title":
					// node title is created
					final Element title = this.converted_xml.createElement("title");
					title.setTextContent(n_children.get(1).getTextContent());
					w.appendChild(title);
					break;
				case "Modal":
					// node title is created
					final Element modal = this.converted_xml.createElement("modal");
					modal.setTextContent(n_children.get(1).getTextContent().toLowerCase());
					w.appendChild(modal);
					break;
				case "Rootwindow":
					// node title is created
					final Element root = this.converted_xml.createElement("root");
					root.setTextContent(n_children.get(1).getTextContent().toLowerCase());
					w.appendChild(root);
					break;
				}
			}
			this.traverseContainersRecursive(w, container, w_id, null);
		}
	}

	/**
	 * Function that traverses the Container tags in rippedContainer recursively
	 *
	 * @param out
	 * @param rippedContainer
	 * @throws Exception
	 */
	private void traverseContainersRecursive(final Element out, final Element rippedContainer, final String w_id,
			final String father_container_id) throws Exception {

		final List<Element> children = XMLUtil.getChildrenElements(rippedContainer);
		Element attributes = null;
		Element contents = null;
		// the nodes attributes and contents are extracted
		switch (children.size()) {
		case (1):
			if (!"Contents".equals(children.get(0).getNodeName())) {
				throw new Exception(
						"GUIStructureConverter - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
			}
			contents = children.get(0);
			break;
		case (2):
			if (!"Attributes".equals(children.get(0).getNodeName())
					|| !"Contents".equals(children.get(1).getNodeName())) {
				throw new Exception(
						"GUIStructureConverter - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
			}
			attributes = children.get(0);
			contents = children.get(1);
			break;
		}

		String title = null;
		String repl_action = null;
		String id = null;
		String classs = null;
		// the attributes of the container are read
		if (attributes != null) {
			final List<Element> properties = XMLUtil.getChildrenElements(attributes);
			// iterates all the properties
			for (final Element p : properties) {
				final List<Element> p_children = XMLUtil.getChildrenElements(p);
				if (!"Property".equals(p.getNodeName()) || p_children.size() != 2) {
					throw new Exception(
							"GUIStructureConverter - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
				}
				final Element name = p_children.get(0);
				final Element value = p_children.get(1);
				if (!"Name".equals(name.getNodeName()) || !"Value".equals(value.getNodeName())) {
					throw new Exception(
							"GUIStructureConverter - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
				}

				switch (name.getTextContent()) {
				case ("Title"):
					title = value.getTextContent();
					break;
				case ("ReplayableAction"):
					repl_action = value.getTextContent();
					break;
				case ("ID"):
					id = value.getTextContent();
					break;
				case ("Class"):
					classs = value.getTextContent();
					break;
				}
			}
			this.widget_window_mapping.put(id, w_id);
		}

		// if it is a selectable widget
		if (id != null && "edu.umd.cs.guitar.event.JFCSelectionHandler".equals(repl_action)
				&& !this.filter_selectable_widgets_classes.contains(classs)) {
			final Element selectable_widget = this.converted_xml.createElement("selectable_widget");
			selectable_widget.setAttribute("id", id);
			final Element label = this.converted_xml.createElement("label");
			label.setTextContent(title);
			selectable_widget.appendChild(label);
			// TO DO: implement technique to match label with input field
			final Element size = this.converted_xml.createElement("size");
			size.setTextContent("0");
			// TO DO: implement technique to retrive the size of a selectable
			selectable_widget.appendChild(size);
			out.appendChild(selectable_widget);

			if (CONTEXT_ANALYSIS) {
				// duplicated code, refactor maybe?
				final List<Element> properties = XMLUtil.getChildrenElements(attributes);
				int x = -1;
				int y = -1;
				int width = -1;
				int height = -1;
				for (final Element p : properties) {
					final List<Element> p_children = XMLUtil.getChildrenElements(p);

					if (!"Property".equals(p.getNodeName()) || p_children.size() != 2) {
						throw new Exception(
								"GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
					}
					final Element name = p_children.get(0);
					final Element value = p_children.get(1);
					if (!"Name".equals(name.getNodeName()) || !"Value".equals(value.getNodeName())) {
						throw new Exception(
								"GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
					}

					switch (name.getTextContent()) {
					case ("X"):
						x = Integer.valueOf(value.getTextContent());
						break;
					case ("Y"):
						y = Integer.valueOf(value.getTextContent());
						break;
					case ("width"):
						width = Integer.valueOf(value.getTextContent());
						break;
					case ("height"):
						height = Integer.valueOf(value.getTextContent());
						break;
					}
				}
				if (x == -1 || y == -1 || height == -1 || width == -1 || label == null) {
					throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
				}
				final String descriptor = this.context_analyzer.getDescriptor(x, y, width, height, father_container_id);
				if (descriptor != null) {
					label.setTextContent(descriptor.toLowerCase());
				}
			}
			final Element class_node = this.converted_xml.createElement("class");
			class_node.setTextContent(classs);
			selectable_widget.appendChild(class_node);
			return;
		}

		// // iterate trough the elements contained in the container
		// final List<Element> contained =
		// XMLUtil.getChildrenElements(contents);
		// for (final Element n : contained) {
		// if ("Widget".equals(n.getNodeName())
		// || ("Container".equals(n.getNodeName()) &&
		// !this.container_has_contents(n))) {
		// final Element w = this.extractWidget(n, w_id, id);
		// if (w != null) {
		// out.appendChild(w);
		// }
		// } else {
		// this.traverseContainersRecursive(out, n, w_id, id);
		// }
		// }

		// iterate trough the elements contained in the container
		final List<Element> contained = XMLUtil.getChildrenElements(contents);
		for (final Element n : contained) {
			final Element w = this.extractWidget(n, w_id, id);
			if (w != null) {
				out.appendChild(w);
			}
			if (!"Widget".equals(n.getNodeName()) && this.container_has_contents(n)) {
				this.traverseContainersRecursive(out, n, w_id, id);
			}
		}
	}

	/**
	 * Function that verify whether the widget is a widget of interest.
	 *
	 * @param in_widget
	 * @param w_id:
	 *            id of the window
	 * @return
	 * @throws Exception
	 */
	private Element extractWidget(final Element in_widget, final String w_id, final String father_container_id)
			throws Exception {

		final List<Element> w_children = XMLUtil.getChildrenElements(in_widget);

		if (w_children.size() != 1 && !"Attributes".equals(w_children.get(0).getNodeName())) {
			throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
		}
		final Element attributes = w_children.get(0);
		final List<Element> properties = XMLUtil.getChildrenElements(attributes);
		if (properties.size() < 2) {
			throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
		}

		final Element id = properties.get(0);
		final Element classs = properties.get(1);
		final List<Element> id_children = XMLUtil.getChildrenElements(id);
		final List<Element> class_children = XMLUtil.getChildrenElements(classs);

		this.widget_window_mapping.put(id_children.get(1).getTextContent(), w_id);

		if (id_children.size() != 2 || class_children.size() != 2 || !"Name".equals(class_children.get(0).getNodeName())
				|| !"Value".equals(class_children.get(1).getNodeName())
				|| !"Name".equals(id_children.get(0).getNodeName())
				|| !"Value".equals(id_children.get(1).getNodeName())) {
			throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
		}

		String class_name = class_children.get(1).getTextContent();
		// DZ: just to make buddi work
		if (class_name.contains("org.homeunix.thecave.buddi.view.menu.items")) {
			class_name = "org.homeunix.thecave.buddi.view.menu.items";
		}

		if (this.action_widgets_classes.contains(class_name)) {
			final Element action_widget = this.converted_xml.createElement("action_widget");
			action_widget.setAttribute("id", id_children.get(1).getTextContent());
			Element label, label2;
			label = label2 = null;
			for (final Element p : properties) {
				final List<Element> p_children = XMLUtil.getChildrenElements(p);

				if (!"Property".equals(p.getNodeName()) || p_children.size() != 2) {
					throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
				}
				final Element name = p_children.get(0);
				final Element value = p_children.get(1);
				if (!"Name".equals(name.getNodeName()) || !"Value".equals(value.getNodeName())) {
					throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
				}

				switch (name.getTextContent()) {
				case ("text"):
					// the node label is created
					label = this.converted_xml.createElement("label");
					label.setTextContent(value.getTextContent().toLowerCase());
					break;
				case ("Title"):
					// the node label is created
					label2 = this.converted_xml.createElement("label");
					label2.setTextContent(value.getTextContent().toLowerCase());
					break;
				}
			}
			if (label == null) {
				action_widget.appendChild(label2);
			} else {
				action_widget.appendChild(label);
			}
			final Element class_node = this.converted_xml.createElement("class");
			class_node.setTextContent(class_name);
			action_widget.appendChild(class_node);
			this.action_w_window_mapping.put(id_children.get(1).getTextContent(), w_id);
			return action_widget;
		} else if (this.input_widgets_classes.contains(class_name)) {
			final Element input_widget = this.converted_xml.createElement("input_widget");
			input_widget.setAttribute("id", id_children.get(1).getTextContent());
			final Element label = this.converted_xml.createElement("label");
			final Element text = this.converted_xml.createElement("text");

			for (final Element p : properties) {
				final List<Element> p_children = XMLUtil.getChildrenElements(p);

				if (!"Property".equals(p.getNodeName()) || p_children.size() != 2) {
					throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
				}
				final Element name = p_children.get(0);
				final Element value = p_children.get(1);
				if (!"Name".equals(name.getNodeName()) || !"Value".equals(value.getNodeName())) {
					throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
				}

				switch (name.getTextContent()) {
				case ("text"):
					text.setTextContent(value.getTextContent().toLowerCase());
					break;
				}
			}

			if (CONTEXT_ANALYSIS) {
				int x = -1;
				int y = -1;
				int width = -1;
				int height = -1;
				for (final Element p : properties) {
					final List<Element> p_children = XMLUtil.getChildrenElements(p);

					if (!"Property".equals(p.getNodeName()) || p_children.size() != 2) {
						throw new Exception(
								"GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
					}
					final Element name = p_children.get(0);
					final Element value = p_children.get(1);
					if (!"Name".equals(name.getNodeName()) || !"Value".equals(value.getNodeName())) {
						throw new Exception(
								"GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
					}

					switch (name.getTextContent()) {
					case ("X"):
						x = Integer.valueOf(value.getTextContent());
						break;
					case ("Y"):
						y = Integer.valueOf(value.getTextContent());
						break;
					case ("width"):
						width = Integer.valueOf(value.getTextContent());
						break;
					case ("height"):
						height = Integer.valueOf(value.getTextContent());
						break;
					}
				}
				if (x == -1 || y == -1 || height == -1 || width == -1 || label == null) {
					throw new Exception("GUIStructureConverter - extractWidget: wrong GUI file for GUIRippingConvert");
				}
				final String descriptor = this.context_analyzer.getDescriptor(x, y, width, height, father_container_id);
				if (descriptor != null) {
					label.setTextContent(descriptor.toLowerCase());
				}
			}

			input_widget.appendChild(label);

			input_widget.appendChild(text);
			final Element class_node = this.converted_xml.createElement("class");
			class_node.setTextContent(class_name);
			input_widget.appendChild(class_node);
			return input_widget;
		}
		return null;
	}

	private void GUIRippingConvert_EFG(final Document EFG) throws Exception {

		final Element root = (Element) this.converted_xml.getElementsByTagName("GUI").item(0);

		// the parsing starts
		final NodeList struct = EFG.getElementsByTagName("EFG");
		if (struct.getLength() != 1) {
			throw new Exception("GUIRippingConverter: wrong EFG file for GUIRippingConvert");
		}

		final List<Element> root_child = XMLUtil.getChildrenElements((Element) struct.item(0));

		if (root_child.size() == 1 && "Events".equals(root_child.get(0).getNodeName())) {
			return;
		}

		if (root_child.size() != 2 || !"Events".equals(root_child.get(0).getNodeName())
				|| !"EventGraph".equals(root_child.get(1).getNodeName())) {
			throw new Exception("GUIRippingConverter: wrong EFG file for GUIRippingConvert");
		}

		final List<Element> events = XMLUtil.getChildrenElements(root_child.get(0));
		final int events_n = events.size();
		final String[] events_id = new String[events_n];

		for (int cont = 0; cont < events_n; cont++) {
			final Element event = events.get(cont);
			final List<Element> event_child = XMLUtil.getChildrenElements(event);
			if (event_child.size() < 2 || !"WidgetId".equals(event_child.get(1).getNodeName())) {
				throw new Exception("GUIRippingConverter: wrong EFG file for GUIRippingConvert");
			}
			events_id[cont] = event_child.get(1).getTextContent();
		}

		final List<Element> rows = XMLUtil.getChildrenElements(root_child.get(1));
		if (rows.size() != events_n) {
			throw new Exception("GUIRippingConverter: wrong EFG file for GUIRippingConvert");
		}

		final int[][] matrix = new int[events_n][events_n];

		// the table is parsed
		// the windows available after each event are saved
		final Map<String, Set<String>> map_avail_wind = new HashMap<>();
		for (int cont = 0; cont < events_n; cont++) {
			final Element row = rows.get(cont);
			final List<Element> row_events = XMLUtil.getChildrenElements(row);
			if (row_events.size() != events_n) {
				throw new Exception("GUIRippingConverter: wrong EFG file for GUIRippingConvert");
			}
			final Set<String> avail_win = new HashSet<>();
			map_avail_wind.put(events_id[cont], avail_win);
			for (int cont2 = 0; cont2 < events_n; cont2++) {
				final Element el = row_events.get(cont2);
				matrix[cont][cont2] = Integer.valueOf(el.getTextContent());
				if ("2".equals(el.getTextContent()) || "1".equals(el.getTextContent())) {
					avail_win.add(this.widget_window_mapping.get(events_id[cont2]));
				}
			}
		}

		// the windows available before each event are saved
		final Map<String, Set<String>> map_avail_wind_before = new HashMap<>();
		for (int cont = 0; cont < events_n; cont++) {
			Set<String> avail_win = null;
			if (map_avail_wind_before.containsKey(this.widget_window_mapping.get(events_id[cont]))) {
				avail_win = map_avail_wind_before.get(this.widget_window_mapping.get(events_id[cont]));
			} else {
				avail_win = new HashSet<>();
				map_avail_wind_before.put(this.widget_window_mapping.get(events_id[cont]), avail_win);
			}
			for (int cont2 = 0; cont2 < events_n; cont2++) {
				final int val = matrix[cont2][cont];

				if ((2 == val) && this.widget_window_mapping.get(events_id[cont2]) != this.widget_window_mapping
						.get(events_id[cont])) {
					avail_win.add(this.widget_window_mapping.get(events_id[cont2]));
				}
			}
		}

		// edges are calculated
		final ArrayList<String> added_edges = new ArrayList<>();
		for (int cont = 0; cont < events_n; cont++) {
			final String w_id = this.widget_window_mapping.get(events_id[cont]);
			final Set<String> windows_after = map_avail_wind.get(events_id[cont]);
			final Set<String> windows_before = map_avail_wind_before
					.get(this.widget_window_mapping.get(events_id[cont]));

			for (final String w : windows_after) {
				final String edge_id = events_id[cont] + "-" + w;
				final boolean contained_before = this.contained_before(w_id, w, map_avail_wind_before, windows_before);
				if (!w.equals(w_id) && !added_edges.contains(edge_id)
						&& this.action_w_window_mapping.containsKey(events_id[cont])) {
					if (!contained_before && !windows_before.contains(w)) {
						final Element edge = this.converted_xml.createElement("edge");
						final Element from = this.converted_xml.createElement("from");
						final Element to = this.converted_xml.createElement("to");
						edge.setAttribute("id", edge_id);
						from.setTextContent(events_id[cont]);
						to.setTextContent(w);
						edge.appendChild(from);
						edge.appendChild(to);
						root.appendChild(edge);
						added_edges.add(edge_id);
						continue;
					}
					if (!windows_after.contains(w_id) && windows_before.contains(w)) {
						final Element edge = this.converted_xml.createElement("edge");
						final Element from = this.converted_xml.createElement("from");
						final Element to = this.converted_xml.createElement("to");
						edge.setAttribute("id", edge_id);
						from.setTextContent(events_id[cont]);
						to.setTextContent(w);
						edge.appendChild(from);
						edge.appendChild(to);
						root.appendChild(edge);
						added_edges.add(edge_id);
						continue;
					}
				}
			}
		}
	}

	private boolean container_has_contents(final Element c) {

		final List<Element> children = XMLUtil.getChildrenElements(c);
		Element contents = null;
		// the nodes attributes and contents are extracted
		switch (children.size()) {
		case (1):
			contents = children.get(0);
			break;
		case (2):
			contents = children.get(1);
			break;
		}
		final List<Element> contained = XMLUtil.getChildrenElements(contents);
		return (contained.size() > 0);
	}

	/*
	 * function that detects that given a certain window (start_w) and the
	 * window that were open when it was opened (windows_opened_before) a target
	 * window target_w was available 1 generation before the opening of start_w,
	 * i.e. was opened before windows_opened_before
	 */
	private boolean contained_before(final String start_w, final String target_w,
			final Map<String, Set<String>> map_avail_wind_before, final Set<String> windows_opened_before) {

		for (final String w : windows_opened_before) {
			final Set<String> opened_before_w = map_avail_wind_before.get(w);
			if (opened_before_w.contains(target_w)) {
				return true;
			}
			this.contained_before(w, target_w, map_avail_wind_before, opened_before_w);
		}
		return false;
	}

	public Document[] fromGUItoXML(final GUI input_gui) throws Exception {

		final Document[] out = new Document[2];

		try {
			// the xml testcase is created
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document gui = docBuilder.newDocument();
			final Document efg = docBuilder.newDocument();

			final Element root = gui.createElement("GUIStructure");
			gui.appendChild(root);
			final Element root2 = efg.createElement("EFG");
			efg.appendChild(root2);
			final Element events = efg.createElement("Events");
			root2.appendChild(events);

			for (final Window w : input_gui.getWindows()) {
				final Element node1 = gui.createElement("GUI");
				root.appendChild(node1);
				Element node2 = gui.createElement("Window");
				node1.appendChild(node2);
				Element node3 = gui.createElement("Attributes");
				node2.appendChild(node3);
				// title
				Element prop = gui.createElement("Property");
				node3.appendChild(prop);
				Element name = gui.createElement("Name");
				name.setTextContent("Title");
				prop.appendChild(name);
				Element value = gui.createElement("Value");
				prop.appendChild(value);
				value.setTextContent(w.getLabel());
				// modal
				prop = gui.createElement("Property");
				node3.appendChild(prop);
				name = gui.createElement("Name");
				name.setTextContent("Modal");
				prop.appendChild(name);
				value = gui.createElement("Value");
				prop.appendChild(value);
				value.setTextContent(String.valueOf(w.isModal()));
				// root
				prop = gui.createElement("Property");
				node3.appendChild(prop);
				name = gui.createElement("Name");
				name.setTextContent("Rootwindow");
				prop.appendChild(name);
				value = gui.createElement("Value");
				prop.appendChild(value);
				value.setTextContent(String.valueOf(w.isRoot()));

				node2 = gui.createElement("Container");
				node1.appendChild(node2);
				node3 = gui.createElement("Contents");
				node2.appendChild(node3);

				final List<Widget> widgets = new ArrayList<>();
				widgets.addAll(w.getActionWidgets());

				widgets.addAll(w.getInputWidgets());

				widgets.addAll(w.getSelectableWidgets());

				for (final Widget ww : widgets) {
					final Element wid = gui.createElement("Widget");
					node3.appendChild(wid);
					final Element attr = gui.createElement("Attributes");
					wid.appendChild(attr);
					// id
					prop = gui.createElement("Property");
					attr.appendChild(prop);
					name = gui.createElement("Name");
					name.setTextContent("ID");
					prop.appendChild(name);
					value = gui.createElement("Value");
					prop.appendChild(value);
					value.setTextContent(ww.getId());
					// class
					prop = gui.createElement("Property");
					attr.appendChild(prop);
					name = gui.createElement("Name");
					name.setTextContent("Class");
					prop.appendChild(name);
					value = gui.createElement("Value");
					prop.appendChild(value);
					value.setTextContent(ww.getClasss());
					// type
					prop = gui.createElement("Property");
					attr.appendChild(prop);
					name = gui.createElement("Name");
					name.setTextContent("Type");
					prop.appendChild(name);
					value = gui.createElement("Value");
					prop.appendChild(value);
					value.setTextContent("SYSTEM INTERACTION");
					// invokelist
					if (ww instanceof Action_widget) {
						prop = gui.createElement("Property");
						attr.appendChild(prop);
						name = gui.createElement("Name");
						name.setTextContent("Invokelist");
						prop.appendChild(name);
						for (final Window win : input_gui.getForwardLinks((Action_widget) ww)) {
							value = gui.createElement("Value");
							prop.appendChild(value);
							value.setTextContent(win.getLabel());
							break;
						}
					}

					// efg
					final Element event = efg.createElement("Event");
					events.appendChild(event);
					// eventid
					Element node = efg.createElement("EventId");
					node.setTextContent("e" + ww.getId().substring(1));
					event.appendChild(node);
					// widgetid
					node = efg.createElement("WidgetId");
					node.setTextContent(ww.getId());
					event.appendChild(node);
					// action
					node = efg.createElement("Action");
					node.setTextContent(this.class_replayableaction.get(ww.getClasss()));
					event.appendChild(node);
				}

			}

			out[0] = gui;
			out[1] = efg;
		} catch (

		final Exception e) {
			e.printStackTrace();
			throw new Exception("GUIRippingConverter - fromGUItoXML: error, " + e.getMessage());
		}
		return out;
	}
}
