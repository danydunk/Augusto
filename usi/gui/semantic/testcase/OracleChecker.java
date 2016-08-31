package usi.gui.semantic.testcase;

import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class OracleChecker {

	public OracleChecker() {

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

		if (result.getActions_executed().size() < result.getTc().getActions().size()) {
			// System.out.println("diff actions");
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

				return -1;
			}
			if (actual.getActionWidgets().size() < oracle.getActionWidgets().size()) {
				// System.out.println("diff aws size");

				return -1;
			}
			if (actual.getInputWidgets().size() < oracle.getInputWidgets().size()) {
				// System.out.println("diff iws size");

				return -1;
			}
			if (actual.getSelectableWidgets().size() < oracle.getSelectableWidgets().size()) {
				// System.out.println("diff sws size");

				return -1;
			}

			for (final Input_widget iw : oracle.getInputWidgets()) {
				final Input_widget actual_iw = (Input_widget) actual.getWidget(iw.getId());
				if (actual_iw == null) {
					// System.out.println("iw null");

					return -1;
				}
				if (!actual_iw.getValue().equals(iw.getValue())) {
					// System.out.println("diff value " + actual_iw.getId() +
					// " "
					// + actual_iw.getValue() + " " + iw.getId() + " " +
					// iw.getValue());

					return -1;
				}
			}

			for (final Selectable_widget sw : oracle.getSelectableWidgets()) {
				final Selectable_widget actual_sw = (Selectable_widget) actual
						.getWidget(sw.getId());
				if (actual_sw == null) {
					// System.out.println("sw null");

					return -1;
				}
				if (actual_sw.getSize() != sw.getSize()
						|| actual_sw.getSelected() != sw.getSelected()) {
					// System.out
					// .println("diff selected " + actual_sw.getSize() + " "
					// + actual_sw.getSelected() + " " + sw.getSize() + " "
					// + sw.getSelected());

					return -1;
				}
			}
		}
		return 1;
	}
}
