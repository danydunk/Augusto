package usi.guistructure.xmlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import usi.guistructure.Action_widget;
import usi.guistructure.GUI;
import usi.guistructure.Input_widget;
import usi.guistructure.Selectable_widget;
import usi.guistructure.Window;

public class GUIParser {

	public static GUI parse(final Document doc) throws Exception {

		final GUIParser parser = new GUIParser();
		return parser.read(doc);
	}

	private GUI read(final Document doc) throws Exception {

		final GUI gui = new GUI();

		final NodeList nList = doc.getElementsByTagName("GUI");

		if (nList == null || nList.getLength() == 0) {
			return null;
		}

		final Node nNodeGUI = nList.item(0);
		if (nNodeGUI == null) {
			return null;
		}

		final NodeList childWin = nNodeGUI.getChildNodes();
		// FOR each GUI child:
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if ("window".equals(nChild.getNodeName())) {
					final Window windows = this.createWindows(nChild);
					gui.addWindow(windows);

				} else if ("edge".equals(nChild.getNodeName())) {
					this.createEdge(nChild, gui);
				}
			}
		}
		return gui;
	}

	private void createEdge(final Node node, final GUI gui) throws Exception {

		// String id = node.getAttributes().getNamedItem("id").getNodeValue();
		// See that the id is not used.
		final Node fromNode = getElementNode(node.getChildNodes(), "from");
		final Node toNode = getElementNode(node.getChildNodes(), "to");
		final String idFrom = this.getNodeContent(fromNode);
		final String idTo = this.getNodeContent(toNode);

		final List<Action_widget> aw = gui.getAction_widgets().stream()
				.filter(e -> e.getId().equals(idFrom)).collect(Collectors.toList());

		final List<Window> w = gui.getWindows().stream().filter(e -> e.getId().equals(idTo))
				.collect(Collectors.toList());
		if (aw.size() != 1 || w.size() != 1) {
			throw new Exception("GUIParser - createEdge: id not found.");
		}
		gui.addEdge(aw.get(0), w.get(0));
	}

	private Window createWindows(final Node nodeWindow) throws Exception {

		final String id = nodeWindow.getAttributes().getNamedItem("id").getNodeValue();
		final Node nodeModal = getElementNode(nodeWindow.getChildNodes(), "modal");
		final Boolean modal = new Boolean(this.getNodeContent(nodeModal));
		final Node nodeTitle = getElementNode(nodeWindow.getChildNodes(), "title");
		final String title = this.getNodeContent(nodeTitle);
		final Node nodeRoot = getElementNode(nodeWindow.getChildNodes(), "root");
		final Boolean root = new Boolean(this.getNodeContent(nodeRoot));

		final Window window = new Window(id, modal, title, root);

		final List<Action_widget> actionWidgets = this.createActionsWidgets(nodeWindow);
		for (final Action_widget action_widget : actionWidgets) {
			window.addActionWidget(action_widget);
		}

		final List<Selectable_widget> selectableWidgets = this.createSelectableWidgets(nodeWindow);
		for (final Selectable_widget selectable_widget : selectableWidgets) {
			window.addSelectableWidget(selectable_widget);
		}

		final List<Input_widget> inputWidgets = this.createInputWidgets(nodeWindow);
		for (final Input_widget input_widget : inputWidgets) {
			window.addInputWidget(input_widget);
		}

		// List<Node> nodeContainerList =
		// getElementNodesList(nodeWindow.getChildNodes(), "container");
		// for (Iterator<Node> iterator = nodeContainerList.iterator();
		// iterator.hasNext();) {
		// //Node nodeContainer = (Node) iterator.next();
		// //Container container = createContainer(nodeContainer);
		// //window.addContainer(container);
		// }

		return window;
	}

	/**
	 * Creates a container form the node that contains the container info.
	 *
	 * @param nodeContainer
	 * @return
	 */
	// private Container createContainer(Node node) throws Exception {
	// String id = node.getAttributes().getNamedItem("id").getNodeValue();
	//
	// Node nodeLabel = getElementNode(node.getChildNodes(), "label");
	// String label = getNodeContent(nodeLabel);
	//
	// Container container = new Container(id, label);
	//
	// List<Action_widget> actionWidgets = createActionsWidgets(node);
	// for (Action_widget action_widget : actionWidgets) {
	// container.add_Action_widget(action_widget);
	// }
	//
	// List<Selectable_widget> selectableWidgets =
	// createSelectableWidgets(node);
	// for (Selectable_widget selectable_widget : selectableWidgets) {
	// container.add_Selectable_widget(selectable_widget);
	// }
	//
	// List<Input_widget> inputWidgets = createInputWidgets(node);
	// for (Input_widget input_widget : inputWidgets) {
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
	 * @throws Exception
	 */
	private List<Input_widget> createInputWidgets(final Node nodeContainer) throws Exception {

		final List<Input_widget> input = new ArrayList<>();
		final List<Node> nodes = this.getElementNodesList(nodeContainer.getChildNodes(),
				"input_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = this.getNodeContent(nodeLabel);
			final Node nodeText = getElementNode(node.getChildNodes(), "text");
			final String text = this.getNodeContent(nodeText);
			final Node nodeClass = getElementNode(node.getChildNodes(), "class");
			final String classs = this.getNodeContent(nodeClass);
			final Input_widget aw = new Input_widget(id, label, text, classs);
			input.add(aw);
		}

		return input;
	}

	/**
	 * Return a list of ActionWidgets located in the container
	 *
	 * @param nodeContainer
	 * @return
	 * @throws Exception
	 */
	private List<Action_widget> createActionsWidgets(final Node nodeContainer) throws Exception {

		final List<Action_widget> actions = new ArrayList<>();
		final List<Node> nodes = this.getElementNodesList(nodeContainer.getChildNodes(),
				"action_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = this.getNodeContent(nodeLabel);
			final Node nodeClass = getElementNode(node.getChildNodes(), "class");
			final String classs = this.getNodeContent(nodeClass);
			final Action_widget aw = new Action_widget(id, label, classs);
			actions.add(aw);
		}

		return actions;
	}

	/**
	 * Return a list of Selectable Widgets located in the container
	 *
	 * @param nodeContainer
	 * @return
	 * @throws Exception
	 */
	private List<Selectable_widget> createSelectableWidgets(final Node nodeContainer)
			throws Exception {

		final List<Selectable_widget> selectbles = new ArrayList<>();
		final List<Node> nodes = this.getElementNodesList(nodeContainer.getChildNodes(),
				"selectable_widget");
		for (final Node node : nodes) {
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			final Node nodeLabel = getElementNode(node.getChildNodes(), "label");
			final String label = this.getNodeContent(nodeLabel);
			final Node sizen = getElementNode(node.getChildNodes(), "size");
			final int size = Integer.valueOf(this.getNodeContent(sizen));
			final Node nodeClass = getElementNode(node.getChildNodes(), "class");
			final String classs = this.getNodeContent(nodeClass);
			final Selectable_widget aw = new Selectable_widget(id, label, size, classs);
			selectbles.add(aw);
		}
		return selectbles;
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
