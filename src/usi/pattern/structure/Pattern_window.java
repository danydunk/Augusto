package src.usi.pattern.structure;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Widget;
import src.usi.gui.structure.Window;

import com.google.common.collect.Collections2;

public class Pattern_window extends Pattern_widget<Window> {

	private final boolean dynamic;
	private final Boolean_regexp modal;
	private final Map<String, Pattern_widget> widgets_map;
	private List<Pattern_action_widget> action_widgets;
	private List<Pattern_input_widget> input_widgets;
	private List<Pattern_selectable_widget> selectable_widgets;

	// private List<Pattern_container> containers;

	public Pattern_window(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final Boolean_regexp modal, final String classs,
			final boolean dynamic) {

		super(id, label, card, alloy_correspondence, classs);
		this.modal = modal;
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
		this.widgets_map = new HashMap<>();
		this.dynamic = dynamic;
	}

	public Pattern_window(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final Boolean_regexp modal, final String classs,
			final boolean dynamic, final String position) {

		super(id, label, card, alloy_correspondence, classs, position);
		this.modal = modal;
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
		this.widgets_map = new HashMap<>();
		this.dynamic = dynamic;
	}

	public boolean isDynamic() {

		return this.dynamic;
	}

	public boolean containsWidget(final String id) {

		return this.widgets_map.containsKey(id);
	}

	public Boolean_regexp getModal() {

		return this.modal;
	}

	public List<Pattern_action_widget> getActionWidgets() {

		return new ArrayList<>(this.action_widgets);
	}

	public List<Pattern_input_widget> getInputWidgets() {

		return new ArrayList<>(this.input_widgets);
	}

	public List<Pattern_selectable_widget> getSelectableWidgets() {

		return new ArrayList<>(this.selectable_widgets);
	}

	public void setAction_widgets(final List<Pattern_action_widget> in) {

		if (in == null) {
			for (final Pattern_action_widget aw : this.action_widgets) {
				this.widgets_map.remove(aw.getId());
			}

			this.action_widgets = in;

			for (final Pattern_action_widget aw : this.action_widgets) {
				this.widgets_map.put(aw.getId(), aw);
			}

		}

		this.action_widgets = in;
	}

	public void setInput_widgets(final List<Pattern_input_widget> in) {

		if (in != null) {

			for (final Pattern_input_widget iw : this.input_widgets) {
				this.widgets_map.remove(iw.getId());
			}

			this.input_widgets = in;

			for (final Pattern_input_widget iw : this.input_widgets) {
				this.widgets_map.put(iw.getId(), iw);
			}
			this.input_widgets.sort(null);
		}
	}

	public void setSelectable_widgets(final List<Pattern_selectable_widget> in) {

		if (in != null) {
			for (final Pattern_selectable_widget sw : this.selectable_widgets) {
				this.widgets_map.remove(sw.getId());
			}

			this.selectable_widgets = in;
			for (final Pattern_selectable_widget sw : this.selectable_widgets) {
				this.widgets_map.put(sw.getId(), sw);
			}
			this.selectable_widgets.sort(null);
		}
	}

	public void addWidget(final Pattern_widget w) {

		if (w instanceof Pattern_action_widget) {
			this.addActionWidget((Pattern_action_widget) w);
		}
		if (w instanceof Pattern_input_widget) {
			this.addInputWidget((Pattern_input_widget) w);
		}
		if (w instanceof Pattern_selectable_widget) {
			this.addSelectableWidget((Pattern_selectable_widget) w);
		}
		this.widgets_map.put(w.getId(), w);
	}

	private void addActionWidget(final Pattern_action_widget in) {

		if (in != null) {
			this.action_widgets.add(in);
		}
	}

	private void addInputWidget(final Pattern_input_widget in) {

		if (in != null) {
			this.input_widgets.add(in);
		}
	}

	private void addSelectableWidget(final Pattern_selectable_widget in) {

		if (in != null) {
			this.selectable_widgets.add(in);
		}
	}

	@Override
	/**
	 * function that returns true if the pattern window can be matched with the
	 * input one this does not mean that the window is a match. To be a match
	 * the edges must be considered as well
	 */
	public boolean isMatch(final Window w) throws Exception {

		if (this.getMatch(w) != null) {
			return true;
		} else {
			return false;
		}
	}

	public Pattern_widget getWidget(final String id) {

		return this.widgets_map.get(id);
	}

	public Instance_window getMatch(final Window w) throws Exception {

		final List<Instance_window> matches = this.getMatches(w);
		if (matches.size() == 0) {
			return null;
		}
		return matches.get(0);
	}

	/**
	 * function that returns all the possible matches of the input window with
	 * the pattern window
	 */
	public List<Instance_window> getMatches(final Window w) throws Exception {

		final List<Instance_window> out = new ArrayList<>();

		if (!super.isMatch(w)) {
			return out;
		}

		// if (this.root == Boolean_regexp.FALSE && w.isRoot()) {
		// return out;
		// }
		// if (this.root == Boolean_regexp.TRUE && !w.isRoot()) {
		// return out;
		// }

		final Map<Pattern_action_widget, List<Action_widget>> aw_map = new HashMap<>();
		final List<Action_widget> matched_aw = new ArrayList<>();
		int needed_el = 0;
		for (final Pattern_action_widget awp : this.getActionWidgets()) {
			needed_el += awp.getCardinality().getMin();
			final List<Action_widget> matches = new ArrayList<>();
			aw_map.put(awp, matches);
			for (final Action_widget aw : w.getActionWidgets()) {

				if (awp.isMatch(aw)) {
					matches.add(aw);
					matched_aw.add(aw);
				}
			}
			if (matches.size() < awp.getCardinality().getMin()
					|| (awp.getCardinality().getMax() == 0 && matches.size() > 0)) {
				return out;
			}
		}
		if (matched_aw.size() < needed_el) {
			return out;
		}

		final List<Input_widget> matched_iw = new ArrayList<>();
		needed_el = 0;
		final Map<Pattern_input_widget, List<Input_widget>> iw_map = new HashMap<>();
		for (final Pattern_input_widget iwp : this.getInputWidgets()) {
			needed_el += iwp.getCardinality().getMin();
			final List<Input_widget> matches = new ArrayList<>();
			iw_map.put(iwp, matches);
			for (final Input_widget iw : w.getInputWidgets()) {

				if (iwp.isMatch(iw)) {
					matches.add(iw);
					matched_iw.add(iw);
				}
			}
			if (matches.size() < iwp.getCardinality().getMin()
					|| (iwp.getCardinality().getMax() == 0 && matches.size() > 0)) {
				return out;
			}
		}
		if (matched_iw.size() < needed_el) {
			return out;
		}

		final List<Selectable_widget> matched_sw = new ArrayList<>();
		needed_el = 0;
		final Map<Pattern_selectable_widget, List<Selectable_widget>> sw_map = new HashMap<>();
		for (final Pattern_selectable_widget swp : this.getSelectableWidgets()) {
			needed_el += swp.getCardinality().getMin();

			final List<Selectable_widget> matches = new ArrayList<>();
			sw_map.put(swp, matches);
			for (final Selectable_widget sw : w.getSelectableWidgets()) {

				if (swp.isMatch(sw)) {
					matches.add(sw);
					matched_sw.add(sw);
				}
			}
			if (matches.size() < swp.getCardinality().getMin()
					|| (swp.getCardinality().getMax() == 0 && matches.size() > 0)) {
				return out;
			}
		}
		if (matched_sw.size() < needed_el) {
			return out;
		}

		final List<Map<? extends Pattern_widget<Action_widget>, List<Action_widget>>> aw_distr = this
				.distribute(this.getActionWidgets(), aw_map);
		final List<Map<? extends Pattern_widget<Input_widget>, List<Input_widget>>> iw_distr = this
				.distribute(this.getInputWidgets(), iw_map);
		final List<Map<? extends Pattern_widget<Selectable_widget>, List<Selectable_widget>>> sw_distr = this
				.distribute(this.getSelectableWidgets(), sw_map);

		for (final Map<? extends Pattern_widget<Action_widget>, List<Action_widget>> a_m : aw_distr) {
			for (final Map<? extends Pattern_widget<Input_widget>, List<Input_widget>> i_m : iw_distr) {
				for (final Map<? extends Pattern_widget<Selectable_widget>, List<Selectable_widget>> s_m : sw_distr) {

					final Instance_window inst_win = new Instance_window(this, w);
					for (final Pattern_widget<Action_widget> paw : a_m.keySet()) {
						inst_win.addAW_mapping((Pattern_action_widget) paw, a_m.get(paw));
					}
					for (final Pattern_widget<Input_widget> piw : i_m.keySet()) {
						inst_win.addIW_mapping((Pattern_input_widget) piw, i_m.get(piw));
					}
					for (final Pattern_widget<Selectable_widget> psw : s_m.keySet()) {
						inst_win.addSW_mapping((Pattern_selectable_widget) psw, s_m.get(psw));
					}
					out.add(inst_win);
				}
			}
		}
		final List<To_order> l = new ArrayList<>();
		for (final Instance_window iw : out) {
			l.add(new To_order(iw));
		}
		Collections.sort(l);
		final List<Instance_window> output = new ArrayList<>();
		for (final To_order o : l) {
			output.add(o.iw);
		}
		return output;
	}

	/*
	 * Functions that returns all the possible combinations of a certain type of
	 * widgets that match the cardinalities
	 */
	protected <C extends Widget> List<Map<? extends Pattern_widget<C>, List<C>>> distribute(
			final List<? extends Pattern_widget<C>> keys,
			final Map<? extends Pattern_widget<C>, List<C>> map) {

		final List<Map<? extends Pattern_widget<C>, List<C>>> out = new ArrayList<>();

		final Map<Pattern_widget<C>, List<List<C>>> possibilities_map = new HashMap<>();

		for (int x = 0; x < keys.size(); x++) {
			final Pattern_widget<C> p1 = keys.get(x);
			final List<C> l1 = map.get(p1);

			final List<List<C>> possibilities = new ArrayList<>();
			possibilities_map.put(p1, possibilities);
			final int min = p1.getCardinality().getMin();
			final int max = p1.getCardinality().getMax();

			// there are not enough elements to satisfy cardinality
			if (l1.size() < min) {
				return out;
			}

			// size of the max permutation
			final int k = Math.min(max, l1.size());
			for (int c = k; c > (min - 1); c--) {
				// structure used to indicate if the element at index i is in
				// the output comb
				final List<Integer> to_perm = new ArrayList<>();
				for (int cont = 0; cont < l1.size(); cont++) {
					if (cont < c) {
						to_perm.add(1);
					} else {
						to_perm.add(0);
					}
				}
				// all the permutations are calculated
				final Collection<List<Integer>> permutations = Collections2
						.orderedPermutations(to_perm);

				// the list of possibilities is created
				possibilities.addAll(permutations.parallelStream().map(e -> {
					final List<C> ret = new ArrayList<>();
					ret.addAll(IntStream.range(0, e.size()).mapToObj(ee -> {
						return new AbstractMap.SimpleEntry<Integer, Integer>(ee, e.get(ee));
					}).filter(eee -> eee.getValue() == 1).map(eeee -> {
						return l1.get(eeee.getKey());
					}).collect(Collectors.toList()));
					return ret;
				}).collect(Collectors.toList()));
			}
		}

		final List<Integer> indexes = new ArrayList<>();
		for (int cont = 0; cont < keys.size(); cont++) {
			indexes.add(0);
		}
		// All the instances are created
		while (true) {
			// a combination is taken
			final List<List<C>> possible_instance = new ArrayList<>();
			for (int cont = 0; cont < keys.size(); cont++) {
				final Integer index = indexes.get(cont);
				possible_instance.add(cont, possibilities_map.get(keys.get(cont)).get(index));
			}

			// if the combination is valid, i.e. no intersection
			if (!hasIntersection(possible_instance)) {
				final Map<Pattern_widget<C>, List<C>> inst = new HashMap<Pattern_widget<C>, List<C>>();
				for (int cont = 0; cont < keys.size(); cont++) {
					inst.put(keys.get(cont), possible_instance.get(cont));
				}
				out.add(inst);
			}

			// the indexes are increased
			boolean exit = true;
			// loop: for (int cont = keys.size() - 1; cont >= 0; cont--) {
			//
			// if (possibilities_map.get(keys.get(cont)).size() >
			// (indexes.get(cont) + 1)) {
			// final Integer i = indexes.remove(cont);
			// indexes.add(cont, i + 1);
			// exit = false;
			// break loop;
			// }
			// }

			loop: for (int cont = keys.size() - 1; cont >= 0; cont--) {

				if (possibilities_map.get(keys.get(cont)).size() > (indexes.get(cont) + 1)) {
					if (indexes.get(cont) == 0 && cont != (keys.size() - 1)) {
						for (int x = cont + 1; x < keys.size(); x++) {
							indexes.remove(x);
							indexes.add(x, 0);
						}
					}
					final Integer i = indexes.remove(cont);
					indexes.add(cont, i + 1);
					exit = false;
					break loop;
				}
			}
			// if none of the indexes was incremented
			if (exit) {
				break;
			}
		}
		return out;
	}

	protected static <C> boolean hasIntersection(final List<List<C>> possible_instance) {

		for (int x = 0; x < possible_instance.size(); x++) {
			final List<C> l1 = possible_instance.get(x);
			for (int y = x + 1; y < possible_instance.size(); y++) {
				final List<C> l2 = possible_instance.get(y);
				for (final C el : l1) {
					if (l2.contains(el)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected <C> List<C> intersection(final List<C> in1, final List<C> in2) {

		final List<C> out = new ArrayList<>();
		for (final C w : in1) {
			if (in2.contains(w)) {
				out.add(w);
			}
		}
		for (final C w : in2) {
			if (in1.contains(w) && !out.contains(w)) {
				out.add(w);
			}
		}
		return out;
	}

	public class To_order implements Comparable<To_order> {

		protected float density;
		protected List<Widget> widgets;
		protected Instance_window iw;
		protected int dist;

		public To_order(final Instance_window iw) throws Exception {

			this.density = 0;
			this.iw = iw;
			this.widgets = new ArrayList<>();
			for (final Pattern_action_widget k : iw.getPattern().getActionWidgets()) {
				this.widgets.addAll(iw.getAWS_for_PAW(k.getId()));
			}
			for (final Pattern_input_widget k : iw.getPattern().getInputWidgets()) {
				this.widgets.addAll(iw.getIWS_for_PIW(k.getId()));
			}
			for (final Pattern_selectable_widget k : iw.getPattern().getSelectableWidgets()) {
				this.widgets.addAll(iw.getSWS_for_PSW(k.getId()));
			}
			int maxx = 0;
			int minx = Integer.MAX_VALUE;
			int maxy = 0;
			int miny = Integer.MAX_VALUE;
			for (int x = 0; x < this.widgets.size(); x++) {
				final Widget w1 = this.widgets.get(x);
				if (w1.getX() < minx) {
					minx = w1.getX();
				}
				if (w1.getY() < miny) {
					miny = w1.getY();
				}
				if (w1.getX() + w1.getWidth() > maxx) {
					maxx = w1.getX() + w1.getWidth();
				}
				if (w1.getY() + w1.getHeight() > maxy) {
					maxy = w1.getY() + w1.getHeight();
				}
				this.density += w1.getHeight() * w1.getWidth();
				for (int y = x + 1; y < this.widgets.size(); y++) {
					final Widget w2 = this.widgets.get(y);
					final int d = this.getDistance(w1.getX(), w1.getY(), w2.getX(), w2.getY());
					if (d > this.dist) {
						this.dist = d;
					}
				}
			}

			this.density = this.density / ((maxx - minx) * (maxy - miny));
		}

		private int getDistance(final int x1, final int y1, final int x2, final int y2) {

			final int distance;

			distance = (int) (Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));

			return distance;
		}

		@Override
		public int compareTo(final To_order o) {

			if (o.density > 0.2 && !(this.density > 0.2)) {
				return Integer.MAX_VALUE;
			}

			if (this.density > 0.2 && !(o.density > 0.2)) {
				return Integer.MIN_VALUE;
			}

			if (o.widgets.size() != this.widgets.size()) {
				if (o.widgets.size() > this.widgets.size()) {

					return Integer.MAX_VALUE;

				} else {

					return Integer.MIN_VALUE;
				}
			}
			return this.dist - o.dist;
		}

	}
}
