package src.usi.gui.functionality.instance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.pattern.structure.Pattern_widget;
import src.usi.semantic.SpecificSemantics;
import src.usi.semantic.alloy.AlloyUtil;
import src.usi.semantic.alloy.Alloy_Model;
import src.usi.xml.XMLUtil;

public class Instance_GUI_patternParser {

	public static Instance_GUI_pattern parse(final Document doc) throws Exception {

		return parse(doc, PathsManager.getGUIPatternsFolder());
	}

	public static Instance_GUI_pattern parse(final Document doc, final String pattern_folder)
			throws Exception {

		if (doc == null) {
			throw new Exception("Instance_GUI_patternParser - parse: null input");
		}

		final Node root = doc.getFirstChild();
		assert (root != null && root.getNodeName().equals("instance_GUI_pattern"));
		// Pattern
		final Node patternn = XMLUtil.getElementNode(root.getChildNodes(), "GUI_pattern");
		final String pattername = patternn.getTextContent();
		final Document doc2 = XMLUtil.read(Instance_GUI_pattern.class
				.getResourceAsStream(pattern_folder + File.separator + pattername + ".xml"));
		final GUI_Pattern pattern = GUIPatternParser.parse(doc2);
		// GUI
		final Node guin = XMLUtil.getElementNode(root.getChildNodes(), "GUI");
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document guidoc = docBuilder.newDocument();
		guidoc.adoptNode(guin);
		guidoc.appendChild(guin);
		final GUI gui = GUIParser.parse(guidoc);
		// Windows
		final Node windowsn = XMLUtil.getElementNode(root.getChildNodes(), "windows");
		final List<Node> ws = XMLUtil.getElementNodesList(windowsn.getChildNodes(),
				"instance_window");
		final List<Instance_window> instances = new ArrayList<>();
		for (final Node n : ws) {
			assert (n.getNodeName().equals("instance_window"));
			instances.add(parseWindow(n, gui, pattern));
		}
		// semantics
		final Node semn = XMLUtil.getElementNode(root.getChildNodes(), "semantics");
		final Alloy_Model mod = AlloyUtil.loadAlloyModelFromString(semn.getTextContent());

		final Instance_GUI_pattern out = new Instance_GUI_pattern(gui, pattern, instances);
		out.setSpecificSemantics(SpecificSemantics.instantiate(mod));
		return out;
	}

	private static Instance_window parseWindow(final Node nodeWindow, final GUI gui,
			final GUI_Pattern pattern) throws Exception {

		final String instanceid = nodeWindow.getAttributes().getNamedItem("instance_id")
				.getNodeValue();
		final String patternid = nodeWindow.getAttributes().getNamedItem("pattern_id")
				.getNodeValue();

		final Instance_window out = new Instance_window(pattern.getWindow(patternid),
				gui.getWindow(instanceid));
		// mappings
		final Map<String, List<String>> map = new HashMap<>();
		final List<Node> mappings = XMLUtil.getElementNodesList(nodeWindow.getChildNodes(),
				"mapping");
		for (final Node n : mappings) {
			final String iid = XMLUtil.getElementNode(n.getChildNodes(), "instance")
					.getTextContent();
			final String pid = XMLUtil.getElementNode(n.getChildNodes(), "pattern")
					.getTextContent();

			if (map.containsKey(pid)) {
				map.get(pid).add(iid);
			} else {
				final List<String> l = new ArrayList<>();
				l.add(iid);
				map.put(pid, l);
			}
		}

		for (final String pid : map.keySet()) {

			final Pattern_widget pw = pattern.getWindow(patternid).getWidget(pid);
			final List<String> l = map.get(pid);
			if (pw instanceof Pattern_action_widget) {
				final List<Action_widget> aws = new ArrayList<>();
				for (final String iid : l) {
					aws.add((Action_widget) gui.getWindow(instanceid).getWidget(iid));
				}
				out.addAW_mapping((Pattern_action_widget) pw, aws);
			}
			if (pw instanceof Pattern_input_widget) {
				final List<Input_widget> iws = new ArrayList<>();
				for (final String iid : l) {
					iws.add((Input_widget) gui.getWindow(instanceid).getWidget(iid));
				}
				out.addIW_mapping((Pattern_input_widget) pw, iws);

			}
			if (pw instanceof Pattern_selectable_widget) {
				final List<Selectable_widget> sws = new ArrayList<>();
				for (final String iid : l) {
					sws.add((Selectable_widget) gui.getWindow(instanceid).getWidget(iid));
				}
				out.addSW_mapping((Pattern_selectable_widget) pw, sws);

			}
		}

		return out;
	}
}
