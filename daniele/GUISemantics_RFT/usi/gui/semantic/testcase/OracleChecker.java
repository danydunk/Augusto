package usi.gui.semantic.testcase;

import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class OracleChecker {

	private String description_last_check;

	public OracleChecker() {

	}

	public String getDescriptionOfLastOracleCheck() {

		return this.description_last_check;
	}

	/**
	 * function that checks the execution of a test case returns 1 if the test
	 * cases was run correctly 0 if the test case was not run completely -1 if
	 * the functional oracle fails
	 *
	 * @param result
	 * @return
	 */
	public int check(final GUITestCaseResult result) {

		this.description_last_check = "";

		if (result.getActions_executed().size() < result.getTc().getActions().size()) {
			this.description_last_check += "TESTCASE NOT RUN CORRECTLY";
			this.description_last_check += System.lineSeparator();
			this.description_last_check += "ACTIONS TO EXECUTE "
					+ result.getTc().getActions().size() + " BUT EXECUTED ONLY "
					+ result.getActions_executed().size();
			return 0;
		}

		for (int cont = 0; cont < result.getTc().getActions().size(); cont++) {
			final Window oracle = result.getTc().getActions().get(cont).getOracle();
			if (oracle == null) {
				continue;
			}
			final Window actual = result.getResults().get(cont);
			if (!actual.getId().equals(oracle.getId())) {
				// System.out.println("diff winid " + actual.getId() + " " +
				// oracle.getId());
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED WINDOW " + oracle.getId() + " BUT IT WAS "
						+ actual.getId();
				return -1;
			}
			if (actual.getActionWidgets().size() < oracle.getActionWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED ACTION WIDGET SIZE "
						+ oracle.getActionWidgets().size() + " BUT IT WAS "
						+ actual.getActionWidgets().size();
				return -1;
			}
			if (actual.getInputWidgets().size() < oracle.getInputWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED INPUT WIDGET SIZE "
						+ oracle.getActionWidgets().size() + " BUT IT WAS "
						+ actual.getActionWidgets().size();
				return -1;

			}
			if (actual.getSelectableWidgets().size() < oracle.getSelectableWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE "
						+ oracle.getActionWidgets().size() + " BUT IT WAS "
						+ actual.getActionWidgets().size();
				return -1;
			}

			for (final Input_widget iw : oracle.getInputWidgets()) {
				final Input_widget actual_iw = (Input_widget) actual.getWidget(iw.getId());
				if (actual_iw == null) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (cont + 1);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "INPUT WIDGET " + iw.getId() + " NOT FOUND";
					return -1;
				}
				if (!actual_iw.getValue().equals(iw.getValue())) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (cont + 1);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "EXPECTED INPUT WIDGET " + iw.getId()
							+ " VALUE " + iw.getValue() + " BUT IT WAS " + actual_iw.getValue();
					return -1;
				}
			}

			for (final Selectable_widget sw : oracle.getSelectableWidgets()) {
				final Selectable_widget actual_sw = (Selectable_widget) actual
						.getWidget(sw.getId());
				if (actual_sw == null) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (cont + 1);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "SELECTABLE WIDGET " + sw.getId() + " NOT FOUND";
					return -1;
				}
				if (actual_sw.getSize() != sw.getSize()
						|| actual_sw.getSelected() != sw.getSelected()) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (cont + 1);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE AND SELECTION "
							+ sw.getSize() + " AND " + sw.getSelected() + " BUT IT WAS "
							+ actual_sw.getSize() + " AND " + actual_sw.getSelected();
					return -1;
				}
			}
		}
		this.description_last_check += "TEST CASE RUN CORRECTLY";
		return 1;
	}
}
