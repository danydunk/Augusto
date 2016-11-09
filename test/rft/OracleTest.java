package test.rft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.OracleTestHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GUIParser;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Widget;
import usi.gui.structure.Window;
import usi.testcase.GUITestCaseResult;
import usi.testcase.OracleChecker;
import usi.testcase.TestCaseRunner;
import usi.testcase.structure.Click;
import usi.testcase.structure.GUIAction;
import usi.testcase.structure.GUITestCase;
import usi.xml.XMLUtil;

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
	 */
	public void testMain(final Object[] args) {

		try {
			// we load the GUI structure
			final Document doc = XMLUtil.read(new File(
					"./files/for_test/xml/upm-full_newripper.xml").getAbsolutePath());
			final GUI g = GUIParser.parse(doc);

			final OracleChecker oracle = new OracleChecker(g);
			final String conf_file = "files" + File.separator + "for_test" + File.separator
					+ "config" + File.separator + "upm.properties";
			ConfigurationManager.load(conf_file);
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

			if (!oracle.check(res, false)) {
				System.out.println(oracle.getDescriptionOfLastOracleCheck());
				throw new Exception();
			}

			final Window target2 = new Window(target.getId(), target.getLabel(),
					target.getClasss(), target.getX(), target.getY(), target.isModal());
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

			if (oracle.check(res, false)) {
				throw new Exception();
			}

		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
