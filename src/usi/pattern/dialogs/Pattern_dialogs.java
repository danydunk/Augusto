package src.usi.pattern.dialogs;

import java.util.ArrayList;
import java.util.List;

import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.Window;
import src.usi.pattern.structure.Pattern_window;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.GUIAction;

public enum Pattern_dialogs {

	ERROR_WINDOW2(new Pattern_error_window_bis()), ERROR_WINDOW(new Pattern_error_window()), CONFIRM_WINDOW(
			new Pattern_confirm_window()), CONFIRM_WINDOW2(new Pattern_confirm_window_bis());

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
		case ERROR_WINDOW2:
			final Click click2 = new Click(w, null, match.getAWS_for_PAW("pawok").get(0));
			acts.add(click2);
			break;
		case CONFIRM_WINDOW:
			final Click click3 = new Click(w, null, match.getAWS_for_PAW("pawok").get(0));
			acts.add(click3);
			break;
		case CONFIRM_WINDOW2:
			final Click click4 = new Click(w, null, match.getAWS_for_PAW("pawok").get(0));
			acts.add(click4);
			break;
		default:
			return null;
		}
		return acts;

	}

}
