package src.usi.pattern.dialogs;

import java.util.regex.Matcher;

import src.usi.gui.structure.Window;
import src.usi.pattern.structure.Boolean_regexp;
import src.usi.pattern.structure.Cardinality;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.pattern.structure.Pattern_window;

public class Pattern_confirm_window extends Pattern_window {

	public Pattern_confirm_window() {

		super("error_w", ".*", Cardinality.ONE, "", Boolean_regexp.TRUE, ".*dialog.*", false);
		final Pattern_action_widget pawok = new Pattern_action_widget("pawok", "^(confirm|ok|yes)",
				Cardinality.ONE, "", null);
		final Pattern_action_widget pawcancel = new Pattern_action_widget("paw",
				"^(cancel|no|back)", Cardinality.ONE, "", null);
		final Pattern_action_widget other_paw = new Pattern_action_widget("paw_other",
				"^(?!(confirm$|ok$|yes$|cancel$|no$|back$)).*", Cardinality.NONE, "", null);
		final Pattern_input_widget piw = new Pattern_input_widget("piw", ".*", Cardinality.NONE,
				"", ".*", null);
		final Pattern_selectable_widget psw = new Pattern_selectable_widget("psw", ".*",
				Cardinality.NONE, "", ".*", null);
		this.addWidget(pawok);
		this.addWidget(pawcancel);
		this.addWidget(other_paw);
		this.addWidget(piw);
		this.addWidget(psw);
	}

	@Override
	public boolean isMatch(final Window w) throws Exception {

		if (!super.isMatch(w)) {
			return false;
		}

		final java.util.regex.Pattern r = java.util.regex.Pattern.compile(".*continue.*");
		final java.util.regex.Pattern r2 = java.util.regex.Pattern.compile(".*are you sure.*");

		if (w.getActionWidgets().get(0).getDescriptor() != null) {
			final Matcher m = r.matcher(w.getActionWidgets().get(0).getDescriptor().toLowerCase());
			if (m.find()) {
				return true;
			}
			final Matcher m2 = r2
					.matcher(w.getActionWidgets().get(0).getDescriptor().toLowerCase());
			if (m2.find()) {
				return true;
			}
		}
		if (w.getActionWidgets().get(1).getDescriptor() != null) {
			final Matcher m = r.matcher(w.getActionWidgets().get(1).getDescriptor().toLowerCase());
			if (m.find()) {
				return true;
			}
			final Matcher m2 = r2
					.matcher(w.getActionWidgets().get(1).getDescriptor().toLowerCase());
			if (m2.find()) {
				return true;
			}
		}
		return false;
	}
}
