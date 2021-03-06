package src.usi.testcase;

import java.util.List;
import java.util.stream.Collectors;

import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Option_input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Window;
import src.usi.testcase.structure.Clean;
import src.usi.testcase.structure.GUIAction;

public class OracleChecker {

	public static String SKIP_IW = "";
	private String description_last_check;
	// needed to manage standard values
	private final GUI gui;

	public OracleChecker(final GUI gui) {

		this.gui = gui;
	}

	public String getDescriptionOfLastOracleCheck() {

		return this.description_last_check;
	}

	/**
	 * function that checks the execution of a test case returns true if the
	 * test cases was run correctly false if the test case was not run correctly
	 *
	 * @param result
	 * @return
	 */
	public boolean check(final GUITestCaseResult result) {

		this.description_last_check = "";
		boolean out = true;

		final List<GUIAction> actions = result.getTc().getActions().stream()
				.filter(e -> !(e instanceof Clean)).collect(Collectors.toList());
		final List<GUIAction> actions_executed = result.getActions_executed().stream()
				.filter(e -> !(e instanceof Clean)).collect(Collectors.toList());

		if (actions_executed.size() < actions.size()) {
			this.description_last_check += "TESTCASE NOT RUN CORRECTLY";
			this.description_last_check += System.lineSeparator();
			this.description_last_check += "ACTIONS TO EXECUTE " + actions.size()
					+ " BUT EXECUTED ONLY " + actions_executed.size();
			this.description_last_check += System.lineSeparator();
			out = false;
		}
		int cont = 0;
		int index = 0;
		Window old_oracle = null;
		for (; cont < result.getActions_executed().size(); cont++) {
			if (result.getTc().getActions().get(cont) instanceof Clean
					&& cont < result.getActions_executed().size() - 1
					&& result.getTc().getActions().get(cont + 1) instanceof Clean) {
				continue;
			}
			Window oracle = result.getTc().getActions().get(cont).getOracle();
			if (oracle == null && !(result.getTc().getActions().get(cont) instanceof Clean)) {
				index++;
				continue;
			}
			if (cont < result.getActions_executed().size() - 1
					&& result.getTc().getActions().get(cont + 1) instanceof Clean) {
				old_oracle = oracle;
				continue;
			}
			if ((result.getTc().getActions().get(cont) instanceof Clean)) {
				if (old_oracle == null) {
					continue;
				}
				oracle = old_oracle;
			}
			index++;
			// System.out.println(cont);
			final Window actual = result.getResults().get(cont);
			if (!actual.getId().equals(oracle.getId())) {
				// System.out.println("diff winid " + actual.getId() + " " +
				// oracle.getId());
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (index);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED WINDOW " + oracle.getId() + " "
						+ oracle.getLabel() + " BUT IT WAS " + actual.getId() + " "
						+ actual.getLabel();
				this.description_last_check += System.lineSeparator();

				out = false;
				continue;
			}
			if (actual.getActionWidgets().size() < oracle.getActionWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (index);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED ACTION WIDGET SIZE "
						+ oracle.getActionWidgets().size() + " BUT IT WAS "
						+ actual.getActionWidgets().size();
				this.description_last_check += System.lineSeparator();

				out = false;
			}
			if (actual.getInputWidgets().size() < oracle.getInputWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (index);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED INPUT WIDGET SIZE "
						+ oracle.getInputWidgets().size() + " BUT IT WAS "
						+ actual.getInputWidgets().size();
				this.description_last_check += System.lineSeparator();

				out = false;

			}
			if (actual.getSelectableWidgets().size() < oracle.getSelectableWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (index);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE "
						+ oracle.getSelectableWidgets().size() + " BUT IT WAS "
						+ actual.getSelectableWidgets().size();
				this.description_last_check += System.lineSeparator();

				out = false;
			}

			for (final Input_widget iw : oracle.getInputWidgets()) {
				final Input_widget actual_iw = (Input_widget) actual.getWidget(iw.getId());
				if (actual_iw == null) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (index);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "INPUT WIDGET " + iw.getId() + " NOT FOUND";
					this.description_last_check += System.lineSeparator();

					out = false;
					continue;
				}
				if (iw instanceof Option_input_widget) {
					final Option_input_widget oiw = (Option_input_widget) iw;
					if (oiw.getSelected() == -2) {
						continue;
					}
				}
				if (iw.getValue().equals(SKIP_IW)) {
					continue;
				}
				if (!actual_iw.getValue().equals(iw.getValue())) {
					// TODO: change it
					// we manage iw that have a standard value
					final Window ww = this.gui.getWindow(oracle.getId());
					final Input_widget iw_gui = (Input_widget) ww.getWidget(actual_iw.getId());
					// if the iw has a standard value and the oracle expects it
					// it is ok
					if (iw_gui.getValue() != null && iw_gui.getValue().length() > 0
							&& iw_gui.getValue().equals(iw.getValue())) {
						continue;
					}

					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (index);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "EXPECTED INPUT WIDGET " + iw.getId()
							+ " VALUE " + iw.getValue() + " BUT IT WAS " + actual_iw.getValue();
					this.description_last_check += System.lineSeparator();

					out = false;
				}
			}

			for (final Selectable_widget sw : oracle.getSelectableWidgets()) {
				final Selectable_widget actual_sw = (Selectable_widget) actual
						.getWidget(sw.getId());
				if (actual_sw == null) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (index);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "SELECTABLE WIDGET " + sw.getId() + " NOT FOUND";
					this.description_last_check += System.lineSeparator();

					out = false;
				}
				if (actual_sw.getSize() != sw.getSize()) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (index);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE "
							+ sw.getSize() + " BUT IT WAS " + actual_sw.getSize();
					this.description_last_check += System.lineSeparator();

					out = false;
				}
			}
		}
		if (out) {
			this.description_last_check += "TEST CASE RUN CORRECTLY";
		}
		return out;
	}

	public boolean checkWindow(final Window actual, final Window oracle) {

		this.description_last_check = "";
		if (!actual.getId().equals(oracle.getId())) {
			// System.out.println("diff winid " + actual.getId() + " " +
			// oracle.getId());
			this.description_last_check += "FUNCTIONAL ORACLE ERROR";
			this.description_last_check += System.lineSeparator();
			this.description_last_check += System.lineSeparator();
			this.description_last_check += "EXPECTED WINDOW " + oracle.getId() + " "
					+ oracle.getLabel() + " BUT IT WAS " + actual.getId() + " " + actual.getLabel();
			this.description_last_check += System.lineSeparator();

			return false;
		}
		if (actual.getActionWidgets().size() < oracle.getActionWidgets().size()) {
			this.description_last_check += "FUNCTIONAL ORACLE ERROR";
			this.description_last_check += System.lineSeparator();
			this.description_last_check += System.lineSeparator();
			this.description_last_check += "EXPECTED ACTION WIDGET SIZE "
					+ oracle.getActionWidgets().size() + " BUT IT WAS "
					+ actual.getActionWidgets().size();
			this.description_last_check += System.lineSeparator();

			return false;
		}
		if (actual.getInputWidgets().size() < oracle.getInputWidgets().size()) {
			this.description_last_check += "FUNCTIONAL ORACLE ERROR";
			this.description_last_check += System.lineSeparator();
			this.description_last_check += System.lineSeparator();
			this.description_last_check += "EXPECTED INPUT WIDGET SIZE "
					+ oracle.getInputWidgets().size() + " BUT IT WAS "
					+ actual.getInputWidgets().size();
			this.description_last_check += System.lineSeparator();

			return false;

		}
		if (actual.getSelectableWidgets().size() < oracle.getSelectableWidgets().size()) {
			this.description_last_check += "FUNCTIONAL ORACLE ERROR";
			this.description_last_check += System.lineSeparator();
			this.description_last_check += System.lineSeparator();
			this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE "
					+ oracle.getSelectableWidgets().size() + " BUT IT WAS "
					+ actual.getSelectableWidgets().size();
			this.description_last_check += System.lineSeparator();

			return false;
		}

		for (final Input_widget iw : oracle.getInputWidgets()) {
			final Input_widget actual_iw = (Input_widget) actual.getWidget(iw.getId());
			if (actual_iw == null) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "INPUT WIDGET " + iw.getId() + " NOT FOUND";
				this.description_last_check += System.lineSeparator();

				return false;
			}
			// if (!actual_iw.getValue().equals(iw.getValue())) {
			// // TODO: change it
			// // we manage iw that have a standard value
			// final Window ww = this.gui.getWindow(oracle.getId());
			// final Input_widget iw_gui = (Input_widget)
			// ww.getWidget(actual_iw.getId());
			// // if the iw has a standard value and the oracle expects
			// // nothing it is ok
			// if (iw_gui.getValue() != null && iw_gui.getValue().length() > 0
			// && iw.getValue().length() == 0) {
			// continue;
			// }
			//
			// this.description_last_check += "FUNCTIONAL ORACLE ERROR";
			// this.description_last_check += System.lineSeparator();
			// this.description_last_check += System.lineSeparator();
			// this.description_last_check += "EXPECTED INPUT WIDGET " +
			// iw.getId() + " VALUE "
			// + iw.getValue() + " BUT IT WAS " + actual_iw.getValue();
			// this.description_last_check += System.lineSeparator();
			//
			// return false;
			// }
		}

		for (final Selectable_widget sw : oracle.getSelectableWidgets()) {
			final Selectable_widget actual_sw = (Selectable_widget) actual.getWidget(sw.getId());
			if (actual_sw == null) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "SELECTABLE WIDGET " + sw.getId() + " NOT FOUND";
				this.description_last_check += System.lineSeparator();

				return false;
			}
			if (actual_sw.getSize() != sw.getSize()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE " + sw.getSize()
						+ " BUT IT WAS " + actual_sw.getSize();
				this.description_last_check += System.lineSeparator();

				return false;
			}
		}
		return true;
	}
}