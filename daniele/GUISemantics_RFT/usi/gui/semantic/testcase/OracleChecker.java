package usi.gui.semantic.testcase;

import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class OracleChecker {

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
	 * function that checks the execution of a test case returns 1 if the test
	 * cases was run correctly 0 if the test case was not run completely -1 if
	 * the functional oracle fails
	 *
	 * @param result
	 * @return
	 */
	public int check(final GUITestCaseResult result) {

		this.description_last_check = "";
		int res = 1;

		if (result.getActions_executed().size() < result.getTc().getActions().size()) {
			this.description_last_check += "TESTCASE NOT RUN CORRECTLY";
			this.description_last_check += System.lineSeparator();
			this.description_last_check += "ACTIONS TO EXECUTE "
					+ result.getTc().getActions().size() + " BUT EXECUTED ONLY "
					+ result.getActions_executed().size();
			this.description_last_check += System.lineSeparator();

			res = 0;
		}

		for (int cont = 0; cont < result.getActions_executed().size(); cont++) {
			final Window oracle = result.getTc().getActions().get(cont).getOracle();
			if (oracle == null) {
				continue;
			}
			// System.out.println(cont);
			final Window actual = result.getResults().get(cont);
			if (!actual.getId().equals(oracle.getId())) {
				// System.out.println("diff winid " + actual.getId() + " " +
				// oracle.getId());
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED WINDOW " + oracle.getId()
						+ oracle.getLabel() + " BUT IT WAS " + actual.getId() + actual.getLabel();
				this.description_last_check += System.lineSeparator();

				res = (res == 1) ? -1 : res;
				continue;
			}
			if (actual.getActionWidgets().size() < oracle.getActionWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED ACTION WIDGET SIZE "
						+ oracle.getActionWidgets().size() + " BUT IT WAS "
						+ actual.getActionWidgets().size();
				this.description_last_check += System.lineSeparator();

				res = (res == 1) ? -1 : res;
			}
			if (actual.getInputWidgets().size() < oracle.getInputWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED INPUT WIDGET SIZE "
						+ oracle.getInputWidgets().size() + " BUT IT WAS "
						+ actual.getInputWidgets().size();
				this.description_last_check += System.lineSeparator();

				res = (res == 1) ? -1 : res;

			}
			if (actual.getSelectableWidgets().size() < oracle.getSelectableWidgets().size()) {
				this.description_last_check += "FUNCTIONAL ORACLE ERROR";
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "ERROR AT ACTION " + (cont + 1);
				this.description_last_check += System.lineSeparator();
				this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE "
						+ oracle.getSelectableWidgets().size() + " BUT IT WAS "
						+ actual.getSelectableWidgets().size();
				this.description_last_check += System.lineSeparator();

				res = (res == 1) ? -1 : res;
			}

			for (final Input_widget iw : oracle.getInputWidgets()) {
				final Input_widget actual_iw = (Input_widget) actual.getWidget(iw.getId());
				if (actual_iw == null) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (cont + 1);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "INPUT WIDGET " + iw.getId() + " NOT FOUND";
					this.description_last_check += System.lineSeparator();

					res = (res == 1) ? -1 : res;
					continue;
				}
				if (!actual_iw.getValue().equals(iw.getValue())) {
					// TODO: change it
					// we manage iw that have a standard value
					final Window ww = this.gui.getWindow(oracle.getId());
					final Input_widget iw_gui = (Input_widget) ww.getWidget(actual_iw.getId());
					// if the iw has a standard value and the oracle expects
					// nothing it is ok
					if (iw_gui.getValue() != null && iw_gui.getValue().length() > 0
							&& iw.getValue().length() == 0) {
						continue;
					}

					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (cont + 1);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "EXPECTED INPUT WIDGET " + iw.getId()
							+ " VALUE " + iw.getValue() + " BUT IT WAS " + actual_iw.getValue();
					this.description_last_check += System.lineSeparator();

					res = (res == 1) ? -1 : res;
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
					this.description_last_check += System.lineSeparator();

					res = (res == 1) ? -1 : res;
				}
				if (actual_sw.getSize() != sw.getSize()) {
					this.description_last_check += "FUNCTIONAL ORACLE ERROR";
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "ERROR AT ACTION " + (cont + 1);
					this.description_last_check += System.lineSeparator();
					this.description_last_check += "EXPECTED SELECTABLE WIDGET SIZE "
							+ sw.getSize() + " BUT IT WAS " + actual_sw.getSize();
					this.description_last_check += System.lineSeparator();

					res = (res == 1) ? -1 : res;
				}
			}
		}
		if (res == 1) {
			this.description_last_check += "TEST CASE RUN CORRECTLY";
		}
		return res;
	}
}
