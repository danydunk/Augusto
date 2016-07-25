package usi.gui.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GUIPatternParser {

	public static GUI_Pattern parse(final Document doc) throws Exception {

		final GUIPatternParser parser = new GUIPatternParser();
		return parser.read(doc);
	}

	private GUI_Pattern read(final Document doc) throws Exception {

		final GUI_Pattern gui = new GUI_Pattern();

		final NodeList nList = doc.getElementsByTagName("pattern");
		if (nList == null || nList.getLength() == 0) {
			return null;
		}

		final Node nNodeGUI = nList.item(0);
		if (nNodeGUI == null) {
			return null;
		}

		final Node alloy = nNodeGUI.getAttributes().getNamedItem("alloy");
		try {
			gui.loadSemantics(alloy.getTextContent());
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("GUI_Pattern - parse: error loading alloy metamodel");
		}

		final NodeList childWin = nNodeGUI.getChildNodes();
		final List<Pattern_action_widget> aws = new ArrayList<>();
		// FOR each GUI's child:
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if ("window".equals(nChild.getNodeName())) {
					final Pattern_window windows = this.createPatternWindows(nChild);
					aws.addAll(windows.getActionWidgets());
					gui.addWindow(windows);

				} else if ("edge".equals(nChild.getNodeName())) {
					this.createEdge(nChild, gui, aws);
				}

			}
		}

		return gui;
	}

	private void createEdge(final Node node, final GUI_Pattern gui,
			final List<Pattern_action_widget> aws) throws Exception {

		final String type = node.getAttributes().getNamedItem("type").getNodeValue();
		final Node fromNode = getElementNode(node.getChildNodes(), "from");
		final String idFrom = this.getNodeContent(fromNode);
		final List<Node> toNodes = getElementsNode(node.getChildNodes(), "to");

		final List<Pattern_action_widget> aw = aws.stream().filter(e -> e.getId().equals(idFrom))
				.collect(Collectors.toList());

		for (final Node toNode : toNodes) {
			final String idTo = this.getNodeContent(toNode);

			// Pattern_action_widget aw = gui.getAction_widgets().get(idFrom);
			// Pattern_window w = gui.getWindows().get(idTo);

			final List<Pattern_window> w = gui.getWindows().stream()
					.filter(e -> e.getId().equals(idTo)).collect(Collectors.toList());

			if (aw.size() != 1 || w.size() != 1) {
				throw new Exception("GUIParser - createEdge: id not found.");
			}
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

	private Pattern_window createPatternWindows(final Node nodeWindow) throws Exception {

		final String id = nodeWindow.getAttributes().getNamedItem("id").getNodeValue();
		final String cardValue = nodeWindow.getAttributes().getNamedItem("card").getNodeValue();

		final Node nodeModal = getElementNode(nodeWindow.getChildNodes(), "modal");
		final Boolean_regexp modal = Boolean_regexp.valueOf(this.getNodeContent(nodeModal)
				.toUpperCase());
		final Node nodeRoot = getElementNode(nodeWindow.getChildNodes(), "root");
		final Boolean_regexp root = Boolean_regexp.valueOf(this.getNodeContent(nodeRoot)
				.toUpperCase());
		final Node nodeTitle = getElementNode(nodeWindow.getChildNodes(), "title");
		final String title = this.getNodeContent(nodeTitle);

		final Cardinality card = Cardinality.valueOf(cardValue.toUpperCase());
		String alloy = "";
		final Node nAlloy = nodeWindow.getAttributes().getNamedItem("alloy");
		if (nAlloy != null) {
			alloy = nAlloy.getNodeValue();
		}
		final Pattern_window window = new Pattern_window(id, title, card, alloy, modal, root);

		final List<Pattern_action_widget> actionWidgets = this.createActionsWidgets(nodeWindow);
		for (final Pattern_action_widget action_widget : actionWidgets) {
			window.addWidget(action_widget);
		}

		final List<Pattern_selectable_widget> patternWidgets = this
				.createSelectableWidgets(nodeWindow);
		for (final Pattern_selectable_widget pattern_widget : patternWidgets) {
			window.addWidget(pattern_widget);
		}

		final List<Pattern_input_widget> inputWidgets = this.createInputWidgets(nodeWindow);
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
	private List<Pattern_input_widget> createInputWidgets(final Node nodeContainer) {

		final List<Pattern_input_widget> input = new ArrayList<>();
		final List<Node> nodes = this.getElementNodesList(nodeContainer.getChildNodes(),
				"input_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = this.getNodeContent(nodeLabel);
			final Node textLabel = getElementNode(node.getChildNodes(), "text");
			final String text = this.getNodeContent(textLabel);
			final Cardinality card = this.getCardinality(node);
			final Node nAlloy = node.getAttributes().getNamedItem("alloy");
			String alloy = "";
			if (nAlloy != null) {
				alloy = nAlloy.getNodeValue();
			}
			final Pattern_input_widget aw = new Pattern_input_widget(id, label, card, alloy, text);
			input.add(aw);
		}

		return input;
	}

	private Cardinality getCardinality(final Node node) {

		final Node nodeAtt = node.getAttributes().getNamedItem("card");
		if (nodeAtt == null) {
			// TODO: check
			return Cardinality.SET;
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
	private List<Pattern_action_widget> createActionsWidgets(final Node nodeContainer) {

		final List<Pattern_action_widget> actions = new ArrayList<>();
		final List<Node> nodes = this.getElementNodesList(nodeContainer.getChildNodes(),
				"action_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = this.getNodeContent(nodeLabel);
			final Cardinality card = this.getCardinality(node);

			final Node nAlloy = node.getAttributes().getNamedItem("alloy");
			String alloy = "";
			if (nAlloy != null) {
				alloy = nAlloy.getNodeValue();
			}
			final Pattern_action_widget aw = new Pattern_action_widget(id, label, card, alloy);

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
	private List<Pattern_selectable_widget> createSelectableWidgets(final Node nodeContainer) {

		final List<Pattern_selectable_widget> selectables = new ArrayList<>();
		final List<Node> nodes = this.getElementNodesList(nodeContainer.getChildNodes(),
				"selectable_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = this.getNodeContent(nodeLabel);
			final Node sizen = getElementNode(node.getChildNodes(), "size");
			final String size = this.getNodeContent(sizen);
			final Cardinality card = this.getCardinality(node);

			final Node nAlloy = node.getAttributes().getNamedItem("alloy");
			String alloy = "";
			if (nAlloy != null) {
				alloy = nAlloy.getNodeValue();
			}
			final Pattern_selectable_widget aw = new Pattern_selectable_widget(id, label, card,
					alloy, size);

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
	private String getNodeContent(final Node node) {

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
	private List<Node> getElementNodesList(final NodeList childWin, final String name) {

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
