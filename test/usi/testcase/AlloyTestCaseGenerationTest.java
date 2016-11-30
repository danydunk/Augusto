package test.usi.testcase;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.GUIFunctionality_validate;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Option_input_widget;
import src.usi.gui.structure.Window;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.semantic.FunctionalitySemantics;
import src.usi.testcase.AlloyTestCaseGenerator;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUITestCase;
import src.usi.xml.XMLUtil;

public class AlloyTestCaseGenerationTest {

	@Test
	public void test1() throws Exception {

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/guipatterns/crud.xml");
		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		// we load the GUI structure
		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/upm.xml");
		final GUI gui = GUIParser.parse(doc);

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);
		final Instance_GUI_pattern r = res.get(0);
		// manual refinement
		final Window view = new Window("w999", "view", "class", 1, 1, false);

		for (final Input_widget iww : gui.getWindow("w8").getInputWidgets()) {
			if (iww instanceof Option_input_widget) {
				final Option_input_widget oiw = (Option_input_widget) iww;
				view.addWidget(new Option_input_widget(iww.getId() + "9", iww.getLabel(), iww
						.getClasss(), iww.getX(), iww.getY(), oiw.getSize(), oiw.getSelected()));
			} else {
				view.addWidget(new Input_widget(iww.getId() + "9", iww.getLabel(), iww.getClasss(),
						iww.getX(), iww.getY(), iww.getValue()));
			}
		}

		final Action_widget ok = new Action_widget("aw999", "ok", "class", 1, 1);
		view.addWidget(ok);

		gui.addWindow(view);
		final Instance_window iw = new Instance_window(pattern.getWindow("view"), view);
		r.addWindow(iw);
		iw.addAW_mapping(pattern.getWindow("view").getActionWidgets().get(0),
				view.getActionWidgets());
		iw.addIW_mapping(pattern.getWindow("view").getInputWidgets().get(0), view.getInputWidgets());

		r.getGui().addWindow(view);
		r.getGui().addDynamicEdge("aw21", "w8");
		r.getGui().addDynamicEdge("aw23", "w999");
		r.getGui().addDynamicEdge("aw999", "w2");
		r.getGui().addDynamicEdge("aw22", "w2");
		r.getGui().addDynamicEdge("aw62", "w2");

		r.generateSpecificSemantics();
		r.getSemantics()
		.addRun_command(
				"run {System and (some t1,t2: Time| t2 = T/next[t1] and not(t1=t2) and Track.op.t1 in Fill and Track.op.t2 in Fill and Track.op.(T/next[t2]) in Fill and not(Track.op.t2.with = Track.op.t1.with) and Track.op.t2.filled = Input_widget_iw17 and Track.op.t1.filled = Input_widget_iw17 and Track.op.(T/next[t2]).filled = Input_widget_iw20)}for 5 but 5 Time,4 Operation, 10 Value");

		final List<String> values = new ArrayList<>();
		values.add("ciao");
		values.add("ciao2");
		values.add("1");

		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(r, 1, 40000);
		final List<GUITestCase> tests = generator.generateTestCases(values);
		assertEquals(1, tests.size());
		Fill f = (Fill) tests.get(0).getActions().get(1);
		assertEquals("ciao", f.getInput());
		f = (Fill) tests.get(0).getActions().get(2);
		assertEquals("ciao2", f.getInput());
		f = (Fill) tests.get(0).getActions().get(3);
		assertEquals("1", f.getInput());
	}

	@Test
	public void test2() throws Exception {

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/guipatterns/crud.xml");
		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		// we load the GUI structure
		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/upm.xml");
		final GUI gui = GUIParser.parse(doc);

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);
		final Instance_GUI_pattern r = res.get(0);
		// manual refinement
		final Window view = new Window("w999", "view", "class", 1, 1, false);

		for (final Input_widget iww : gui.getWindow("w8").getInputWidgets()) {
			if (iww instanceof Option_input_widget) {
				final Option_input_widget oiw = (Option_input_widget) iww;
				view.addWidget(new Option_input_widget(iww.getId() + "9", iww.getLabel(), iww
						.getClasss(), iww.getX(), iww.getY(), oiw.getSize(), oiw.getSelected()));
			} else {
				view.addWidget(new Input_widget(iww.getId() + "9", iww.getLabel(), iww.getClasss(),
						iww.getX(), iww.getY(), iww.getValue()));
			}
		}

		final Action_widget ok = new Action_widget("aw999", "ok", "class", 1, 1);
		view.addWidget(ok);

		gui.addWindow(view);
		final Instance_window iw = new Instance_window(pattern.getWindow("view"), view);
		r.addWindow(iw);
		iw.addAW_mapping(pattern.getWindow("view").getActionWidgets().get(0),
				view.getActionWidgets());
		iw.addIW_mapping(pattern.getWindow("view").getInputWidgets().get(0), view.getInputWidgets());

		r.getGui().addWindow(view);
		r.getGui().addDynamicEdge("aw21", "w8");
		r.getGui().addDynamicEdge("aw23", "w999");
		r.getGui().addDynamicEdge("aw999", "w2");
		r.getGui().addDynamicEdge("aw22", "w2");
		r.getGui().addDynamicEdge("aw62", "w2");

		r.generateSpecificSemantics();
		r.getSemantics()
		.addRun_command(
				"run {System and (#Fill = 2) and (some t1,t2: Time| t2 = T/next[t1] and Track.op.t1 = Track.op.t2 and Track.op.t1 in Fill and Track.op.t2 in Fill and Track.op.t2.with = Track.op.t1.with and Track.op.t1.filled = Input_widget_iw20)}for 5 but 4 Time,3 Operation, 10 Value");

		final List<String> values = new ArrayList<>();
		values.add("1");
		values.add("1");

		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(r, 1, 40000);
		final List<GUITestCase> tests = generator.generateTestCases(values);
		assertEquals(1, tests.size());
		assertEquals(3, tests.get(0).getActions().size());
		Fill f = (Fill) tests.get(0).getActions().get(1);
		assertEquals("1", f.getInput());
		assertEquals("iw20", f.getWidget().getId());
		f = (Fill) tests.get(0).getActions().get(2);
		assertEquals("1", f.getInput());
		assertEquals("iw20", f.getWidget().getId());
	}

	@Test
	public void test3() throws Exception {

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/guipatterns/crud.xml");
		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		// we load the GUI structure
		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/upm.xml");
		final GUI gui = GUIParser.parse(doc);

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);
		final Instance_GUI_pattern r = res.get(0);
		// manual refinement
		final Window view = new Window("w999", "view", "class", 1, 1, false);

		for (final Input_widget iww : gui.getWindow("w8").getInputWidgets()) {
			if (iww instanceof Option_input_widget) {
				final Option_input_widget oiw = (Option_input_widget) iww;
				view.addWidget(new Option_input_widget(iww.getId() + "9", iww.getLabel(), iww
						.getClasss(), iww.getX(), iww.getY(), oiw.getSize(), oiw.getSelected()));
			} else {
				view.addWidget(new Input_widget(iww.getId() + "9", iww.getLabel(), iww.getClasss(),
						iww.getX(), iww.getY(), iww.getValue()));
			}
		}

		final Action_widget ok = new Action_widget("aw999", "ok", "class", 1, 1);
		view.addWidget(ok);

		gui.addWindow(view);
		final Instance_window iw = new Instance_window(pattern.getWindow("view"), view);
		r.addWindow(iw);
		iw.addAW_mapping(pattern.getWindow("view").getActionWidgets().get(0),
				view.getActionWidgets());
		iw.addIW_mapping(pattern.getWindow("view").getInputWidgets().get(0), view.getInputWidgets());

		r.getGui().addWindow(view);
		r.getGui().addDynamicEdge("aw21", "w8");
		r.getGui().addDynamicEdge("aw23", "w999");
		r.getGui().addDynamicEdge("aw999", "w2");
		r.getGui().addDynamicEdge("aw22", "w2");
		r.getGui().addDynamicEdge("aw62", "w2");

		r.generateSpecificSemantics();
		final Wrapper2 wr = new Wrapper2(r, null);
		final List<String> runs = wr.generate(r.getSemantics());
		for (final String run : runs) {
			r.getSemantics().addRun_command(run);
		}

		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(r, 1, 60000);
		final List<GUITestCase> tests = generator.generateTestCases();
		assertEquals(6, tests.size());
	}

	class Wrapper2 extends GUIFunctionality_validate {

		public Wrapper2(final Instance_GUI_pattern instancePattern, final GUI gui) throws Exception {

			super(instancePattern, gui);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void init() {

		}

		@Override
		public void generate_run_commands(final FunctionalitySemantics sem) throws Exception {

			if (sem == null) {
				return;
			} else {
				super.generate_run_commands(sem);
			}
		}

		public List<String> generate(final FunctionalitySemantics sem) throws Exception {

			super.generate_run_commands(sem);
			return super.getAllSemanticCases();
		}
	}

}