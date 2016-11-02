package usi.gui.pattern.dialogs;

import usi.gui.pattern.Boolean_regexp;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_selectable_widget;
import usi.gui.pattern.Pattern_window;

public class Pattern_confirm_window extends Pattern_window {

	public Pattern_confirm_window() {

		super("error_w", ".*", Cardinality.ONE, "", Boolean_regexp.TRUE, false, ".*dialog.*");
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
}
