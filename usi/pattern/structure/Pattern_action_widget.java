package usi.pattern.structure;

import usi.gui.structure.Action_widget;

public class Pattern_action_widget extends Pattern_widget<Action_widget> {

	public Pattern_action_widget(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final String classs) {

		super(id, label, card, alloy_correspondence, classs);
	}

	@Override
	public boolean isMatch(final Action_widget aw) throws Exception {

		// if (aw.getClasss().trim().toLowerCase().equals("menuitemui")) {
		// // if it is a menu item we remove the part before the dash (it is
		// // the label of the father menu)
		// final String new_label = aw.getLabel().split(" - ")[1];
		// final Action_widget new_aw = new Action_widget(aw.getId(), new_label,
		// aw.getClasss(),
		// aw.getX(), aw.getY());
		// return super.isMatch(new_aw);
		//
		// } else {
		return super.isMatch(aw);
		// }
	}
}
