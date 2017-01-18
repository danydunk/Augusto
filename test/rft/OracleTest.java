package test.rft;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.OracleTestHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Widget;
import src.usi.gui.structure.Window;
import src.usi.testcase.GUITestCaseResult;
import src.usi.testcase.OracleChecker;
import src.usi.testcase.TestCaseRunner;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class OracleTest extends OracleTestHelper {

	/**
	 * Script Name : <b>OracleTest</b> Generated : <b>Aug 29, 2016 6:19:09
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2016/08/29
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		// we load the GUI structure
		final Document doc = XMLUtil.read(PathsManager.getProjectRoot()
				+ "/files/for_test/xml/upm.xml");
		final GUI g = GUIParser.parse(doc);

		final OracleChecker oracle = new OracleChecker(g);

		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/upm.properties");
		ExperimentManager.init();

		final Window target = g.getWindow("w2");
		final Window source = g.getWindow("w1");

		final Action_widget aw = source.getActionWidgets().get(0);
		Click click = new Click(source, target, aw);
		List<GUIAction> acts = new ArrayList<>();
		acts.add(click);
		GUITestCase tc = new GUITestCase(null, acts, "run");

		final TestCaseRunner runner = new TestCaseRunner(g);
		GUITestCaseResult res = runner.runTestCase(tc);

		if (!oracle.check(res)) {
			System.out.println(oracle.getDescriptionOfLastOracleCheck());
			throw new Exception();
		}

		final Window target2 = new Window(target.getId(), target.getLabel(), target.getClasss(),
				target.getX(), target.getY(), target.isModal());
		for (final Widget w : target.getWidgets()) {
			if (w instanceof Selectable_widget) {
				final Selectable_widget s = (Selectable_widget) w;
				final Selectable_widget sw = new Selectable_widget(s.getId(), s.getLabel(),
						s.getClasss(), s.getX(), s.getY(), 1, 0);
				target2.addWidget(sw);
			} else {
				target2.addWidget(w);
			}
		}

		click = new Click(source, target2, aw);
		acts = new ArrayList<>();
		acts.add(click);
		tc = new GUITestCase(null, acts, "run");

		res = runner.runTestCase(tc);

		if (oracle.check(res)) {
			throw new Exception();
		}

		ExperimentManager.cleanUP();
	}
}
