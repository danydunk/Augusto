package usi.gui.functionality.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_selectable_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.structure.Action_widget;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class Instance_window {

	private final Pattern_window pattern;
	private final Window instance;
	// private Map<Pattern_container, List<Instance_container>> c_map;
	private Map<Pattern_action_widget, List<Action_widget>> aw_map;
	// private Map<Action_widget, Pattern_action_widget> inverse_aw_map;
	private Map<Pattern_input_widget, List<Input_widget>> iw_map;
	// private Map<Input_widget, Pattern_input_widget> inverse_iw_map;
	private Map<Pattern_selectable_widget, List<Selectable_widget>> sw_map;
	// private Map<Selectable_widget, Pattern_selectable_widget> inverse_sw_map;

	public Instance_window(final Pattern_window pattern, final Window instance) {
		this.pattern = pattern;
		this.instance = instance;
		// c_map = new HashMap<>();
		this.aw_map = new HashMap<>();
		this.iw_map = new HashMap<>();
		this.sw_map = new HashMap<>();
		// inverse_aw_map = new HashMap<>();
		// inverse_iw_map = new HashMap<>();
		// inverse_sw_map = new HashMap<>();
	}

	public Pattern_window getPattern() {

		return this.pattern;
	}

	public Window getInstance() {

		return this.instance;
	}

	// public Map<Pattern_container, List<Instance_container>> getC_map() {
	// return c_map;
	// }

	public Map<Pattern_action_widget, List<Action_widget>> getAw_map() {

		return new HashMap<>(this.aw_map);
	}

	public Map<Pattern_input_widget, List<Input_widget>> getIw_map() {

		return new HashMap<>(this.iw_map);
	}

	public Map<Pattern_selectable_widget, List<Selectable_widget>> getSw_map() {

		return new HashMap<>(this.sw_map);
	}

	// public void setC_mapping(Pattern_container pc, List<Instance_container>
	// ics) throws Exception {
	// if(!this.pattern.getContainers().contains(pc))
	// throw new Exception("Error in Instance_window: setC_mapping");
	// for(Instance_container ic : ics) {
	// if(!this.instance.getContainers().contains(ic))
	// throw new Exception("Error in Instance_window: setC_mapping");
	// }
	// this.c_map.put(pc, ics);
	// }

	public void addAW_mapping(final Pattern_action_widget paw, final List<Action_widget> aws) throws Exception {

		if (!this.pattern.getActionWidgets().contains(paw)) {
			throw new Exception("Error in Instance_window: addAW_mapping");
		}
		for (final Action_widget aw : aws) {
			if (!this.instance.getActionWidgets().contains(aw)) {
				throw new Exception("Error in Instance_window: setAW_mapping");
			}
		}
		this.aw_map.put(paw, aws);
		// for(Action_widget aw : aws) {
		// this.inverse_aw_map.put(aw, paw);
		// }
	}

	public void addIW_mapping(final Pattern_input_widget piw, final List<Input_widget> iws) throws Exception {

		if (!this.pattern.getInputWidgets().contains(piw)) {
			throw new Exception("Error in Instance_window: setIW_mapping");
		}
		for (final Input_widget iw : iws) {
			if (!this.instance.getInputWidgets().contains(iw)) {
				throw new Exception("Error in Instance_window: setIW_mapping");
			}
		}
		this.iw_map.put(piw, iws);
		// for(Input_widget iw : iws) {
		// this.inverse_iw_map.put(iw, piw);
		// }
	}

	public void addSW_mapping(final Pattern_selectable_widget psw, final List<Selectable_widget> sws) throws Exception {

		if (!this.pattern.getSelectableWidgets().contains(psw)) {
			throw new Exception("Error in Instance_window: setSW_mapping");
		}
		for (final Selectable_widget sw : sws) {
			if (!this.instance.getSelectableWidgets().contains(sw)) {
				throw new Exception("Error in Instance_window: setSW_mapping");
			}
		}
		this.sw_map.put(psw, sws);
		// for(Selectable_widget sw : sws) {
		// this.inverse_sw_map.put(sw, psw);
		// }
	}

	// public Map<Action_widget, Pattern_action_widget> getInverse_aw_map() {
	// return inverse_aw_map;
	// }

	public Pattern_action_widget getPAW_for_AW(final Action_widget aw) {

		for (final Pattern_action_widget paw : this.aw_map.keySet()) {
			if (this.aw_map.get(paw).contains(aw)) {
				return paw;
			}
		}
		return null;
	}

	// public Map<Input_widget, Pattern_input_widget> getInverse_iw_map() {
	// return inverse_iw_map;
	// }

	public Pattern_input_widget getPIW_for_IW(final Input_widget iw) {

		for (final Pattern_input_widget piw : this.iw_map.keySet()) {
			if (this.iw_map.get(piw).contains(iw)) {
				return piw;
			}
		}
		return null;
	}

	// public Map<Selectable_widget, Pattern_selectable_widget>
	// getInverse_sw_map() {
	// return inverse_sw_map;
	// }
	//
	public Pattern_selectable_widget getPSW_for_SW(final Selectable_widget sw) {

		for (final Pattern_selectable_widget psw : this.sw_map.keySet()) {
			if (this.sw_map.get(psw).contains(sw)) {
				return psw;
			}
		}
		return null;
	}

	public void setAw_map(final Map<Pattern_action_widget, List<Action_widget>> in) {

		if (in == null) {
			this.aw_map = new HashMap<>();
		}
		this.aw_map = in;
		// this.inverse_aw_map = new HashMap<>();
		// for(Pattern_action_widget paw : in.keySet()) {
		// for(Action_widget aw : in.get(paw))
		// this.inverse_aw_map.put(aw, paw);
		// }
	}

	public void setIw_map(final Map<Pattern_input_widget, List<Input_widget>> in) {

		if (in == null) {
			this.iw_map = new HashMap<>();
		}
		this.iw_map = in;
		// for(Pattern_input_widget piw : in.keySet()) {
		// for(Input_widget iw : in.get(piw))
		// this.inverse_iw_map.put(iw, piw);
		// }
	}

	public void setSw_map(final Map<Pattern_selectable_widget, List<Selectable_widget>> in) {

		if (in == null) {
			this.sw_map = new HashMap<>();
		}
		this.sw_map = in;
		// for(Pattern_selectable_widget psw : in.keySet()) {
		// for(Selectable_widget sw : in.get(psw))
		// this.inverse_sw_map.put(sw, psw);
		// }
	}

	public boolean isOverlap(final Instance_window iw) {

		if (iw.instance != this.instance) {
			return false;
		}

		for (final Pattern_action_widget paw : this.aw_map.keySet()) {
			for (final Action_widget aw : this.aw_map.get(paw)) {
				if (iw.getPAW_for_AW(aw) != null) {
					return true;
				}
			}
		}

		for (final Pattern_input_widget piw : this.iw_map.keySet()) {
			for (final Input_widget iiw : this.iw_map.get(piw)) {
				if (iw.getPIW_for_IW(iiw) != null) {
					return true;
				}
			}
		}

		for (final Pattern_selectable_widget psw : this.sw_map.keySet()) {
			for (final Selectable_widget sw : this.sw_map.get(psw)) {
				if (iw.getPSW_for_SW(sw) != null) {
					return true;
				}
			}
		}

		// for(Action_widget aw : inverse_aw_map.keySet()) {
		// if(iw.getInverse_aw_map().containsKey(aw))
		// return true;
		// }
		//
		// for(Input_widget iiw : inverse_iw_map.keySet()) {
		// if(iw.getInverse_iw_map().containsKey(iiw))
		// return true;
		// }
		//
		// for(Selectable_widget sw : inverse_sw_map.keySet()) {
		// if(iw.getInverse_sw_map().containsKey(sw))
		// return true;
		// }
		return false;
	}
}
