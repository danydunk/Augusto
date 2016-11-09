package usi.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;
import usi.util.IDManager;
import usi.xml.XMLUtil;

public class GUIParser {

	public static GUI parse(final Document doc) throws Exception {

		if (doc == null) {
			throw new Exception("GUIParser - parse: null input");
		}

		final GUI gui = new GUI();
		IDManager.create(gui);

		final NodeList nList = doc.getElementsByTagName("GUI");
		assert (nList != null && nList.getLength() == 1);

		final Node nNodeGUI = nList.item(0);

		final NodeList childWin = nNodeGUI.getChildNodes();
		// FOR each GUI child:
		final List<Action_widget> aws = new ArrayList<>();
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {
				// windows must be before edges
				if ("window".equals(nChild.getNodeName())) {
					final Window windows = createWindows(nChild);
					aws.addAll(windows.getActionWidgets());
					gui.addWindow(windows);

				} else if ("edge".equals(nChild.getNodeName())) {
					createEdge(nChild, gui, aws);
				}
			}
		}
		return gui;
	}

	private static void createEdge(final Node node, final GUI gui, final List<Action_widget> aws)
			throws Exception {

		final String type = node.getAttributes().getNamedItem("type").getNodeValue();
		// See that the id is not used.
		final Node fromNode = XMLUtil.getElementNode(node.getChildNodes(), "from");
		final Node toNode = XMLUtil.getElementNode(node.getChildNodes(), "to");
		final String idFrom = fromNode.getTextContent();
		final String idTo = toNode.getTextContent();

		final List<Action_widget> aw = aws.stream().filter(e -> e.getId().equals(idFrom))
				.collect(Collectors.toList());

		final List<Window> w = gui.getWindows().stream().filter(e -> e.getId().equals(idTo))
				.collect(Collectors.toList());
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

	public static Window createWindows(final Node nodeWindow) throws Exception {

		// id
		final String id = nodeWindow.getAttributes().getNamedItem("id").getNodeValue();
		// pos
		final Node nodePos = XMLUtil.getElementNode(nodeWindow.getChildNodes(), "pos");
		final String pos = nodePos.getTextContent();
		final int x = Integer.valueOf(pos.split(":")[0]);
		final int y = Integer.valueOf(pos.split(":")[1]);
		// class
		final Node nodeClass = XMLUtil.getElementNode(nodeWindow.getChildNodes(), "class");
		final String classs = nodeClass.getTextContent();
		// title
		final Node nodeTitle = XMLUtil.getElementNode(nodeWindow.getChildNodes(), "title");
		final String title = nodeTitle.getTextContent();
		// modal
		final Node nodeModal = XMLUtil.getElementNode(nodeWindow.getChildNodes(), "modal");
		final Boolean modal = new Boolean(nodeModal.getTextContent());
		// root
		final Node nodeRoot = XMLUtil.getElementNode(nodeWindow.getChildNodes(), "root");
		final Boolean root = new Boolean(nodeRoot.getTextContent());

		final Window window = new Window(id, title, classs, x, y, modal);
		if (root) {
			window.setRoot(true);
		}

		final List<Action_widget> actionWidgets = createActionsWidgets(nodeWindow);
		for (final Action_widget action_widget : actionWidgets) {
			window.addWidget(action_widget);
		}

		final List<Selectable_widget> selectableWidgets = createSelectableWidgets(nodeWindow);
		for (final Selectable_widget selectable_widget : selectableWidgets) {
			window.addWidget(selectable_widget);
		}

		final List<Input_widget> inputWidgets = createInputWidgets(nodeWindow);
		for (final Input_widget input_widget : inputWidgets) {
			window.addWidget(input_widget);
		}

		return window;
	}

	/**
	 * Return a list of InputWidgets located in the container
	 *
	 * @param nodeContainer
	 * @return
	 * @throws Exception
	 */
	private static List<Input_widget> createInputWidgets(final Node nodeContainer) throws Exception {

		final List<Input_widget> input = new ArrayList<>();
		final List<Node> nodes = XMLUtil.getElementNodesList(nodeContainer.getChildNodes(),
				"input_widget");
		for (final Node node : nodes) {
			// id
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			// pos
			final Node nodePos = XMLUtil.getElementNode(node.getChildNodes(), "pos");
			final String pos = nodePos.getTextContent();
			final int x = Integer.valueOf(pos.split(":")[0]);
			final int y = Integer.valueOf(pos.split(":")[1]);
			// class
			final Node nodeClass = XMLUtil.getElementNode(node.getChildNodes(), "class");
			final String classs = nodeClass.getTextContent();
			// label
			final Node nodeLabel = XMLUtil.getElementNode(node.getChildNodes(), "label");
			String label = null;
			if (nodeLabel != null) {
				label = nodeLabel.getTextContent();
			}
			// descriptor
			final Node nodeDescriptor = XMLUtil.getElementNode(node.getChildNodes(), "descriptor");
			String descriptor = null;
			if (nodeDescriptor != null) {
				descriptor = nodeDescriptor.getTextContent();
			}
			// value
			final Node nodevalue = XMLUtil.getElementNode(node.getChildNodes(), "value");
			String value = nodevalue.getTextContent();
			if (value.startsWith("[options] ")) {
				// option input widget
				value = value.substring(10);
				final int selected = Integer.valueOf(value.split(":")[0].trim());
				final int size = Integer.valueOf(value.split(":")[1].trim());
				final Option_input_widget oiw = new Option_input_widget(id, label, classs, x, y,
						size, selected);
				oiw.setDescriptor(descriptor);
				input.add(oiw);
			} else {
				final Input_widget iw = new Input_widget(id, label, classs, x, y, value);
				iw.setDescriptor(descriptor);
				input.add(iw);
			}
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
	private static List<Action_widget> createActionsWidgets(final Node nodeContainer)
			throws Exception {

		final List<Action_widget> actions = new ArrayList<>();
		final List<Node> nodes = XMLUtil.getElementNodesList(nodeContainer.getChildNodes(),
				"action_widget");
		for (final Node node : nodes) {
			// id
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			// pos
			final Node nodePos = XMLUtil.getElementNode(node.getChildNodes(), "pos");
			final String pos = nodePos.getTextContent();
			final int x = Integer.valueOf(pos.split(":")[0]);
			final int y = Integer.valueOf(pos.split(":")[1]);
			// class
			final Node nodeClass = XMLUtil.getElementNode(node.getChildNodes(), "class");
			final String classs = nodeClass.getTextContent();
			// label
			final Node nodeLabel = XMLUtil.getElementNode(node.getChildNodes(), "label");
			String label = null;
			if (nodeLabel != null) {
				label = nodeLabel.getTextContent();
			}
			// descriptor
			final Node nodeDescriptor = XMLUtil.getElementNode(node.getChildNodes(), "descriptor");
			String descriptor = null;
			if (nodeDescriptor != null) {
				descriptor = nodeDescriptor.getTextContent();
			}
			final Action_widget aw = new Action_widget(id, label, classs, x, y);
			aw.setDescriptor(descriptor);
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
	private static List<Selectable_widget> createSelectableWidgets(final Node nodeContainer)
			throws Exception {

		final List<Selectable_widget> selectbles = new ArrayList<>();
		final List<Node> nodes = XMLUtil.getElementNodesList(nodeContainer.getChildNodes(),
				"selectable_widget");
		for (final Node node : nodes) {
			// id
			final String id = node.getAttributes().getNamedItem("id").getNodeValue();
			// pos
			final Node nodePos = XMLUtil.getElementNode(node.getChildNodes(), "pos");
			final String pos = nodePos.getTextContent();
			final int x = Integer.valueOf(pos.split(":")[0]);
			final int y = Integer.valueOf(pos.split(":")[1]);
			// class
			final Node nodeClass = XMLUtil.getElementNode(node.getChildNodes(), "class");
			final String classs = nodeClass.getTextContent();
			// label
			final Node nodeLabel = XMLUtil.getElementNode(node.getChildNodes(), "label");
			String label = null;
			if (nodeLabel != null) {
				label = nodeLabel.getTextContent();
			}
			// descriptor
			final Node nodeDescriptor = XMLUtil.getElementNode(node.getChildNodes(), "descriptor");
			String descriptor = null;
			if (nodeDescriptor != null) {
				descriptor = nodeDescriptor.getTextContent();
			}
			// size
			final Node nodeSize = XMLUtil.getElementNode(node.getChildNodes(), "size");
			final int size = Integer.valueOf(nodeSize.getTextContent());
			// selected
			final Node nodeSelected = XMLUtil.getElementNode(node.getChildNodes(), "selected");
			final int selected = Integer.valueOf(nodeSelected.getTextContent());
			final Selectable_widget sw = new Selectable_widget(id, label, classs, x, y, size,
					selected);
			sw.setDescriptor(descriptor);
			selectbles.add(sw);
		}
		return selectbles;
	}

}
