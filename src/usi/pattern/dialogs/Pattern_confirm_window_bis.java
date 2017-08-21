package src.usi.pattern.dialogs;

import java.util.regex.Matcher;

import src.usi.gui.GuiStateManager;
import src.usi.gui.structure.Window;
import src.usi.pattern.structure.Boolean_regexp;
import src.usi.pattern.structure.Cardinality;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.pattern.structure.Pattern_window;

import com.rational.test.ft.object.interfaces.TestObject;

public class Pattern_confirm_window_bis extends Pattern_window {

	public Pattern_confirm_window_bis() {

		super("error_w", ".*", Cardinality.ONE, "", Boolean_regexp.TRUE, ".*dialog.*", false);
		final Pattern_action_widget pawok = new Pattern_action_widget("pawok", "^(confirm|ok|yes)",
				Cardinality.ONE, "", null);
		final Pattern_action_widget pawcancel = new Pattern_action_widget("paw",
				"^(cancel|no|back)", Cardinality.ONE, "", null);
		final Pattern_action_widget other_paw = new Pattern_action_widget("paw_other",
				"^(?!(confirm$|ok$|yes$|cancel$|no$|back$|$)).*", Cardinality.NONE, "", null);
		final Pattern_action_widget close_paw = new Pattern_action_widget("paw_close", "?null?",
				Cardinality.ONE, "", null);
		final Pattern_input_widget piw = new Pattern_input_widget("piw", ".*", Cardinality.NONE,
				"", ".*", null);
		final Pattern_selectable_widget psw = new Pattern_selectable_widget("psw", ".*",
				Cardinality.NONE, "", ".*", null);
		this.addWidget(pawok);
		this.addWidget(pawcancel);
		this.addWidget(other_paw);
		this.addWidget(piw);
		this.addWidget(psw);
		this.addWidget(close_paw);
	}

	@Override
	public boolean isMatch(final Window w) throws Exception {

		if (!super.isMatch(w)) {
			return false;
		}

		final GuiStateManager gm = GuiStateManager.getInstance();
		for (final TestObject to : gm.getCurrentTOs()) {
			final String classs = to.getProperty("uIClassID").toString();
			final String text = (String) to.getProperty("text");
			if (classs.equals("LabelUI")) {
				final java.util.regex.Pattern r = java.util.regex.Pattern.compile(".*continue.*");
				final java.util.regex.Pattern r2 = java.util.regex.Pattern
						.compile(".*are you sure.*");

				if (w.getActionWidgets().get(0).getDescriptor() != null) {
					final Matcher m = r.matcher(text.toLowerCase());
					if (m.find()) {
						return true;
					}
					final Matcher m2 = r2.matcher(text.toLowerCase());
					if (m2.find()) {
						return true;
					}
				}
			}

		}

		return false;
	}
}
