package usi.pattern.dialogs;

import java.util.ArrayList;
import java.util.List;

import usi.gui.functionality.instance.Instance_window;
import usi.gui.structure.Window;
import usi.pattern.structure.Pattern_window;
import usi.testcase.structure.Click;
import usi.testcase.structure.GUIAction;

public enum Pattern_dialogs {

	ERROR_WINDOW(new Pattern_error_window()), CONFIRM_WINDOW(new Pattern_confirm_window());

	private Pattern_window pw;

	private Pattern_dialogs(final Pattern_window pw) {

		this.pw = pw;
	}

	public boolean isMatch(final Window w) throws Exception {

		return this.pw.isMatch(w);
	}

	public List<GUIAction> getActionsToGoPast(final Window w) throws Exception {

		final List<GUIAction> acts = new ArrayList<>();
		final Instance_window match = this.pw.getMatch(w);
		if (match == null) {
			return null;
		}
		switch (this) {
		case ERROR_WINDOW:
			final Click click = new Click(w, null, match.getAWS_for_PAW("pawok").get(0));
			acts.add(click);
			break;
		case CONFIRM_WINDOW:
			final Click click2 = new Click(w, null, match.getAWS_for_PAW("pawok").get(0));
			acts.add(click2);
			break;
		default:
			return null;
		}
		return acts;

	}

}
