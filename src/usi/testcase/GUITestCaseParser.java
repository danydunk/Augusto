package src.usi.testcase;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import src.usi.gui.GUIParser;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Widget;
import src.usi.gui.structure.Window;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;
import src.usi.xml.XMLUtil;

public class GUITestCaseParser {

	public static GUITestCase parse(final Document doc) throws Exception {

		final NodeList nList = doc.getElementsByTagName("GUITestCase");
		assert (nList != null && nList.getLength() == 1);

		String run_command = null;
		final Node nNodeGUI = nList.item(0);

		final NodeList childs = nNodeGUI.getChildNodes();
		// FOR each Action :
		final List<GUIAction> actions = new ArrayList<>();
		for (int ch = 0; ch < childs.getLength(); ch++) {

			final Node child = childs.item(ch);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if ("run_command".equals(child.getNodeName())) {
					run_command = child.getTextContent();
				} else if ("action".equals(child.getNodeName())) {
					try {
						actions.add(createAction(child));
					} catch (final Exception e) {
						throw new Exception("GUITestCaseParser - wrong format.");
					}
				} else {
					throw new Exception("GUITestCaseParser - wrong format.");
				}
			}
		}

		final GUITestCase tc = new GUITestCase(null, actions, run_command);
		return tc;
	}

	private static GUIAction createAction(final Node actionn) throws Exception {

		// widget
		final Node widn = XMLUtil.getElementNode(actionn.getChildNodes(), "widget");
		final String wid = widn.getTextContent();
		// type
		final Node typen = XMLUtil.getElementNode(actionn.getChildNodes(), "type");
		final String type = typen.getTextContent();
		// oracle
		final Node oraclen = XMLUtil.getElementNode(actionn.getChildNodes(), "oracle");
		Window oracle = null;
		if (oraclen != null) {
			oracle = GUIParser.createWindows(XMLUtil.getElementNode(oraclen.getChildNodes(),
					"window"));
		}

		// source
		final Node sourcen = XMLUtil.getElementNode(
				XMLUtil.getElementNode(actionn.getChildNodes(), "source_window").getChildNodes(),
				"window");
		final Window source = GUIParser.createWindows(sourcen);

		final Widget widget = source.getWidget(wid);

		switch (type) {
		case "click":
			if (!(widget instanceof Action_widget)) {
				throw new Exception();
			}
			final Action_widget aw = (Action_widget) widget;
			final Click click = new Click(source, oracle, aw);
			return click;
		case "fill":
			if (!(widget instanceof Input_widget)) {
				throw new Exception();
			}
			String input = null;
			final Node inputn = XMLUtil.getElementNode(actionn.getChildNodes(), "input");
			if (inputn != null) {
				input = inputn.getTextContent();
			}
			final Input_widget iw = (Input_widget) widget;
			final Fill f = new Fill(source, oracle, iw, input);
			return f;
		case "select":
			if (!(widget instanceof Selectable_widget)) {
				throw new Exception();
			}
			final Selectable_widget sw = (Selectable_widget) widget;
			final Node absn = XMLUtil.getElementNode(actionn.getChildNodes(), "abstract");
			final boolean abs = Boolean.valueOf(absn.getTextContent());
			final Node indexn = XMLUtil.getElementNode(actionn.getChildNodes(), "index");
			final int index = Integer.valueOf(indexn.getTextContent());
			final Select s = new Select(source, oracle, sw, index, abs);
			return s;
		default:
			throw new Exception();
		}

	}
}
