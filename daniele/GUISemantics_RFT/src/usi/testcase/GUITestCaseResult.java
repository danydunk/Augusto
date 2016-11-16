package src.usi.testcase;

import java.util.ArrayList;
import java.util.List;

import src.usi.gui.structure.Window;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;

public class GUITestCaseResult {

	private final GUITestCase tc;
	private final List<GUIAction> actions_executed;
	private List<Window> results;
	// it comprehends also the actions executed for going to a window
	private final List<GUIAction> actions_actually_executed;

	public GUITestCaseResult(final GUITestCase tc, final List<GUIAction> actions_executed,
			final List<Window> results, final List<GUIAction> actions_actually_executed)
					throws Exception {

		if (tc == null || actions_executed == null || actions_actually_executed == null
				|| actions_executed.size() > tc.getActions().size()) {
			throw new Exception("GUITestCaseResult: error in constructor.");
		}
		this.tc = tc;
		this.actions_executed = actions_executed;
		this.results = results;
		this.actions_actually_executed = actions_actually_executed;
	}

	public GUITestCase getTc() {

		return this.tc;
	}

	public List<GUIAction> getActions_executed() {

		return new ArrayList<>(this.actions_executed);
	}

	public List<Window> getResults() {

		return new ArrayList<>(this.results);
	}

	public void setResults(final List<Window> res) throws Exception {

		if (res.size() != this.actions_executed.size()) {
			throw new Exception("GUITestCaseResult - setResults: error.");
		}
		this.results = res;
	}

	public List<GUIAction> getActions_actually_executed() {

		return new ArrayList<>(this.actions_actually_executed);
	}
}
