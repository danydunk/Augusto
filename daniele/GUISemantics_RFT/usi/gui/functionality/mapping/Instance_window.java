package usi.gui.functionality.mapping;

import java.util.ArrayList;
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

	private final Map<String, List<String>> aw_map;
	private final Map<String, List<String>> iw_map;
	private final Map<String, List<String>> sw_map;

	public Instance_window(final Pattern_window pattern, final Window instance) {

		this.pattern = pattern;
		this.instance = instance;

		this.aw_map = new HashMap<>();
		this.iw_map = new HashMap<>();
		this.sw_map = new HashMap<>();
	}

	public Pattern_window getPattern() {

		return this.pattern;
	}

	public Window getInstance() {

		return this.instance;
	}

	// public Map<Pattern_action_widget, List<Action_widget>> getAw_map() {
	//
	// return new HashMap<>(this.aw_map);
	// }
	//
	// public Map<Pattern_input_widget, List<Input_widget>> getIw_map() {
	//
	// return new HashMap<>(this.iw_map);
	// }
	//
	// public Map<Pattern_selectable_widget, List<Selectable_widget>>
	// getSw_map() {
	//
	// return new HashMap<>(this.sw_map);
	// }

	public void addAW_mapping(final Pattern_action_widget paw, final List<Action_widget> aws)
			throws Exception {

		if (!this.pattern.containsWidget(paw.getId())) {
			throw new Exception("Error in Instance_window: addAW_mapping");
		}
		final List<String> mapping = new ArrayList<>();
		for (final Action_widget aw : aws) {
			if (!this.instance.containsWidget(aw.getId())) {
				throw new Exception("Error in Instance_window: setAW_mapping");
			}
			mapping.add(aw.getId());
		}
		this.aw_map.put(paw.getId(), mapping);
	}

	public void addIW_mapping(final Pattern_input_widget piw, final List<Input_widget> iws)
			throws Exception {

		if (!this.pattern.containsWidget(piw.getId())) {
			throw new Exception("Error in Instance_window: setIW_mapping");
		}
		final List<String> mapping = new ArrayList<>();
		for (final Input_widget iw : iws) {
			if (!this.instance.containsWidget(iw.getId())) {
				throw new Exception("Error in Instance_window: setIW_mapping");
			}
			mapping.add(iw.getId());
		}
		this.iw_map.put(piw.getId(), mapping);
	}

	public void
	addSW_mapping(final Pattern_selectable_widget psw, final List<Selectable_widget> sws)
			throws Exception {

		if (!this.pattern.containsWidget(psw.getId())) {
			throw new Exception("Error in Instance_window: setSW_mapping");
		}
		final List<String> mapping = new ArrayList<>();
		for (final Selectable_widget sw : sws) {
			if (!this.instance.containsWidget(sw.getId())) {
				throw new Exception("Error in Instance_window: setSW_mapping");
			}
			mapping.add(sw.getId());
		}
		this.sw_map.put(psw.getId(), mapping);
	}

	public Pattern_action_widget getPAW_for_AW(final String aw) {

		for (final Pattern_action_widget paw : this.pattern.getActionWidgets()) {
			if (this.aw_map.get(paw.getId()).contains(aw)) {
				return paw;
			}
		}
		return null;
	}

	public Pattern_input_widget getPIW_for_IW(final String iw) {

		for (final Pattern_input_widget piw : this.pattern.getInputWidgets()) {
			if (this.iw_map.get(piw.getId()).contains(iw)) {
				return piw;
			}
		}
		return null;
	}

	public Pattern_selectable_widget getPSW_for_SW(final String sw) {

		for (final Pattern_selectable_widget psw : this.pattern.getSelectableWidgets()) {
			if (this.sw_map.get(psw.getId()).contains(sw)) {
				return psw;
			}
		}
		return null;
	}

	public List<Action_widget> getAWS_for_PAW(final String paw) throws Exception {

		final List<Action_widget> out = new ArrayList<>();
		final List<String> ids = this.aw_map.get(paw);
		if (ids == null) {
			return null;
		}
		for (final String id : ids) {
			final Action_widget aw = (Action_widget) this.instance.getWidget(id);
			if (aw == null) {
				throw new Exception("Instance_window - getAWS_for_PAW: error.");
			}
			out.add(aw);
		}
		return out;
	}

	public List<Input_widget> getIWS_for_PIW(final String piw) throws Exception {

		final List<Input_widget> out = new ArrayList<>();
		final List<String> ids = this.iw_map.get(piw);
		if (ids == null) {
			return null;
		}
		for (final String id : ids) {
			final Input_widget aw = (Input_widget) this.instance.getWidget(id);
			if (aw == null) {
				throw new Exception("Instance_window - getIWS_for_PIW: error.");
			}
			out.add(aw);
		}
		return out;
	}

	public List<Selectable_widget> getSWS_for_PSW(final String psw) throws Exception {

		final List<Selectable_widget> out = new ArrayList<>();
		final List<String> ids = this.iw_map.get(psw);
		if (ids == null) {
			return null;
		}
		for (final String id : ids) {
			final Selectable_widget aw = (Selectable_widget) this.instance.getWidget(id);
			if (aw == null) {
				throw new Exception("Instance_window - getSWS_for_PSW: error.");
			}
			out.add(aw);
		}
		return out;
	}

	public boolean isOverlap(final Instance_window iw) {

		if (iw.instance != this.instance) {
			return false;
		}

		for (final String paw : this.aw_map.keySet()) {
			for (final String aw : this.aw_map.get(paw)) {
				if (iw.getPAW_for_AW(aw) != null) {
					return true;
				}
			}
		}

		for (final String piw : this.iw_map.keySet()) {
			for (final String iiw : this.iw_map.get(piw)) {
				if (iw.getPIW_for_IW(iiw) != null) {
					return true;
				}
			}
		}

		for (final String psw : this.sw_map.keySet()) {
			for (final String sw : this.sw_map.get(psw)) {
				if (iw.getPSW_for_SW(sw) != null) {
					return true;
				}
			}
		}
		return false;
	}
}
