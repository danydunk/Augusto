package src.usi.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import src.usi.pattern.structure.Boolean_regexp;
import src.usi.pattern.structure.Cardinality;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.pattern.structure.Pattern_window;

public class GUIPatternParser {

	public static GUI_Pattern parse(final Document doc) throws Exception {

		final NodeList nList = doc.getElementsByTagName("pattern");
		if (nList == null || nList.getLength() == 0) {
			return null;
		}

		final Node nNodeGUI = nList.item(0);
		if (nNodeGUI == null) {
			return null;
		}

		final Node namen = nNodeGUI.getAttributes().getNamedItem("name");
		final GUI_Pattern gui = new GUI_Pattern(namen.getTextContent());

		final Node alloy = nNodeGUI.getAttributes().getNamedItem("alloy");
		try {
			gui.loadSemantics(alloy.getTextContent());
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("GUI_Pattern - parse: error loading alloy metamodel");
		}

		final Node alloy_without = nNodeGUI.getAttributes().getNamedItem(
				"alloy_without_unvalid_data");
		if (alloy_without != null) {
			try {
				gui.loadSemantics_without(alloy_without.getTextContent());
			} catch (final Exception e) {
				e.printStackTrace();
				throw new Exception("GUI_Pattern - parse: error loading alloy metamodel");
			}
		}

		final NodeList childWin = nNodeGUI.getChildNodes();
		final List<Pattern_action_widget> aws = new ArrayList<>();
		// FOR each GUI's child:
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if ("window".equals(nChild.getNodeName())) {
					final Pattern_window windows = createPatternWindows(nChild);
					aws.addAll(windows.getActionWidgets());
					gui.addWindow(windows);

				} else if ("edge".equals(nChild.getNodeName())) {
					createEdge(nChild, gui, aws);
				}

			}
		}

		return gui;
	}

	private static void createEdge(final Node node, final GUI_Pattern gui,
			final List<Pattern_action_widget> aws) throws Exception {

		final String type = node.getAttributes().getNamedItem("type").getNodeValue();
		final Node fromNode = getElementNode(node.getChildNodes(), "from");
		final String idFrom = getNodeContent(fromNode);
		final List<Node> toNodes = getElementsNode(node.getChildNodes(), "to");

		final List<Pattern_action_widget> aw = aws.stream().filter(e -> e.getId().equals(idFrom))
				.collect(Collectors.toList());

		for (final Node toNode : toNodes) {
			final String idTo = getNodeContent(toNode);

			// Pattern_action_widget aw = gui.getAction_widgets().get(idFrom);
			// Pattern_window w = gui.getWindows().get(idTo);

			final List<Pattern_window> w = gui.getWindows().stream()
					.filter(e -> e.getId().equals(idTo)).collect(Collectors.toList());

			assert (aw.size() == 1 && w.size() == 1);

			switch (type) {
			case "static":
				gui.addStaticEdge(aw.get(0).getId(), w.get(0).getId());
				break;
			case "dynamic":
				gui.addDynamicEdge(aw.get(0).getId(), w.get(0).getId());
				break;
			default:
				throw new Exception("GUIParser - createEdge: edge type not found.");
			}
		}
	}

	private static Pattern_window createPatternWindows(final Node nodeWindow) throws Exception {

		final String id = nodeWindow.getAttributes().getNamedItem("id").getNodeValue();

		Cardinality card = null;
		if (nodeWindow.getAttributes().getNamedItem("card") != null) {
			final String cardValue = nodeWindow.getAttributes().getNamedItem("card").getNodeValue();
			card = Cardinality.valueOf(cardValue.toUpperCase());
		}
		final Node nodeModal = getElementNode(nodeWindow.getChildNodes(), "modal");
		final Boolean_regexp modal = Boolean_regexp
				.valueOf(getNodeContent(nodeModal).toUpperCase());
		final Node nodeRoot = getElementNode(nodeWindow.getChildNodes(), "root");
		final Boolean root = Boolean.valueOf(getNodeContent(nodeRoot).toUpperCase());
		final Node nodeTitle = getElementNode(nodeWindow.getChildNodes(), "title");
		final String title = getNodeContent(nodeTitle);
		final Node nodeClass = getElementNode(nodeWindow.getChildNodes(), "class");
		final String classs = getNodeContent(nodeClass);

		String alloy = "";
		final Node nAlloy = nodeWindow.getAttributes().getNamedItem("alloy");
		if (nAlloy != null) {
			alloy = nAlloy.getNodeValue();
		}
		final Pattern_window window = new Pattern_window(id, title, card, alloy, modal, root,
				classs);

		final List<Pattern_action_widget> actionWidgets = createActionsWidgets(nodeWindow);
		for (final Pattern_action_widget action_widget : actionWidgets) {
			window.addWidget(action_widget);
		}

		final List<Pattern_selectable_widget> patternWidgets = createSelectableWidgets(nodeWindow);
		for (final Pattern_selectable_widget pattern_widget : patternWidgets) {
			window.addWidget(pattern_widget);
		}

		final List<Pattern_input_widget> inputWidgets = createInputWidgets(nodeWindow);
		for (final Pattern_input_widget input_widget : inputWidgets) {
			window.addWidget(input_widget);
		}

		// List<Node> nodeContainerList =
		// getElementNodesList(nodeWindow.getChildNodes(), "container");
		// for (Iterator<Node> iterator = nodeContainerList.iterator();
		// iterator.hasNext();) {
		// Node nodeContainer = (Node) iterator.next();
		// Pattern_container container = createContainer(nodeContainer);
		// window.addContainer(container);
		// }

		return window;
	}

	/**
	 * Creates a container form the node that contains the container info.
	 *
	 * @param nodeContainer
	 * @return
	 */
	// private Pattern_container createContainer(Node node) throws Exception {
	// String id = "";// node.getAttributes().getNamedItem("id").getNodeValue();
	// String cardValue =
	// node.getAttributes().getNamedItem("card").getNodeValue();
	// Cardinality card = Cardinality.valueOf(cardValue.toUpperCase());
	//
	// Node nodeLabel = getElementNode(node.getChildNodes(), "label");
	// String label = getNodeContent(nodeLabel);
	//
	// Pattern_container container = new Pattern_container(id, label, card);
	//
	// List<Pattern_action_widget> actionWidgets = createActionsWidgets(node);
	// for (Pattern_action_widget action_widget : actionWidgets) {
	// container.add_Action_widget(action_widget);
	// }
	//
	// List<Pattern_selectable_widget> patternWidgets =
	// createSelectableWidgets(node);
	// for (Pattern_selectable_widget pattern_widget : patternWidgets) {
	// container.add_Selectable_widget(pattern_widget);
	// }
	//
	// List<Pattern_input_widget> inputWidgets = createInputWidgets(node);
	// for (Pattern_input_widget input_widget : inputWidgets) {
	// container.add_Input_widget(input_widget);
	// }
	//
	// return container;
	// }

	/**
	 * Return a list of InputWidgets located in the container
	 *
	 * @param nodeContainer
	 * @return
	 */
	private static List<Pattern_input_widget> createInputWidgets(final Node nodeContainer) {

		final List<Pattern_input_widget> input = new ArrayList<>();
		final List<Node> nodes = getElementNodesList(nodeContainer.getChildNodes(), "input_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = getNodeContent(nodeLabel);
			final Node nodeClass = getElementNode(node.getChildNodes(), "class");
			final String classs = getNodeContent(nodeClass);
			final Node textLabel = getElementNode(node.getChildNodes(), "text");
			final String text = getNodeContent(textLabel);
			final Cardinality card = getCardinality(node);
			final Node nAlloy = node.getAttributes().getNamedItem("alloy");
			String alloy = "";
			if (nAlloy != null) {
				alloy = nAlloy.getNodeValue();
			}
			final Pattern_input_widget aw = new Pattern_input_widget(id, label, card, alloy, text,
					classs);
			input.add(aw);
		}

		return input;
	}

	private static Cardinality getCardinality(final Node node) {

		final Node nodeAtt = node.getAttributes().getNamedItem("card");
		if (nodeAtt == null) {
			return null;
		}
		final String cardValue = nodeAtt.getNodeValue();
		final Cardinality card = Cardinality.valueOf(cardValue.toUpperCase());
		return card;
	}

	/**
	 * Return a list of ActionWidgets located in the container
	 *
	 * @param nodeContainer
	 * @return
	 */
	public static List<Pattern_action_widget> createActionsWidgets(final Node nodeContainer) {

		final List<Pattern_action_widget> actions = new ArrayList<>();
		final List<Node> nodes = getElementNodesList(nodeContainer.getChildNodes(), "action_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final Node nodeClass = getElementNode(node.getChildNodes(), "class");
			final String classs = getNodeContent(nodeClass);
			final String label = getNodeContent(nodeLabel);
			final Cardinality card = getCardinality(node);

			final Node nAlloy = node.getAttributes().getNamedItem("alloy");
			String alloy = "";
			if (nAlloy != null) {
				alloy = nAlloy.getNodeValue();
			}
			final Pattern_action_widget aw = new Pattern_action_widget(id, label, card, alloy,
					classs);

			actions.add(aw);
		}

		return actions;
	}

	/**
	 * Return a list of Selectable Widgets located in the container
	 *
	 * @param nodeContainer
	 * @return
	 */
	private static List<Pattern_selectable_widget>
			createSelectableWidgets(final Node nodeContainer) {

		final List<Pattern_selectable_widget> selectables = new ArrayList<>();
		final List<Node> nodes = getElementNodesList(nodeContainer.getChildNodes(),
				"selectable_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = getNodeContent(nodeLabel);
			final Node nodeClass = getElementNode(node.getChildNodes(), "class");
			final String classs = getNodeContent(nodeClass);
			final Node sizen = getElementNode(node.getChildNodes(), "size");
			final String size = getNodeContent(sizen);
			final Cardinality card = getCardinality(node);

			final Node nAlloy = node.getAttributes().getNamedItem("alloy");
			String alloy = "";
			if (nAlloy != null) {
				alloy = nAlloy.getNodeValue();
			}
			final Pattern_selectable_widget aw = new Pattern_selectable_widget(id, label, card,
					alloy, size, classs);

			selectables.add(aw);
		}

		return selectables;
	}

	/**
	 * Return a content of a Node (i.e., a tag)
	 *
	 * @param node
	 * @return
	 */
	private static String getNodeContent(final Node node) {

		final Node c = node.getChildNodes().item(0);
		if (c != null) {
			return c.getTextContent();
		} else {
			return null;
		}
	}

	/**
	 * Return the first node from a NodeList with a given name passed as
	 * parameter
	 *
	 * @param childWin
	 * @param name
	 * @return
	 */
	private static Node getElementNode(final NodeList childWin, final String name) {

		// FOR each GUI child:
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if (name.equals(nChild.getNodeName())) {
					return nChild;
				}
			}
		}
		return null;
	}

	/**
	 * Return the all nodes from a NodeList with a given name passed as
	 * parameter
	 *
	 * @param childWin
	 * @param name
	 * @return
	 */
	private static List<Node> getElementsNode(final NodeList childWin, final String name) {

		// FOR each GUI child:
		final List<Node> out = new ArrayList<>();
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if (name.equals(nChild.getNodeName())) {
					out.add(nChild);
				}
			}
		}
		return out;
	}

	/**
	 * Retrieve All elements from the list that have a given name
	 *
	 * @param childWin
	 *            List of Nodes
	 * @param name
	 *            Name to match
	 * @return
	 */
	private static List<Node> getElementNodesList(final NodeList childWin, final String name) {

		final List<Node> retrievedNodes = new ArrayList<>();
		// FOR each GUI child:
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if (name.equals(nChild.getNodeName())) {
					retrievedNodes.add(nChild);
				}
			}
		}
		return retrievedNodes;
	}
}
