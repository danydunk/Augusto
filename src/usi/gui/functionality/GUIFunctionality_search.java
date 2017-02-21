package src.usi.gui.functionality;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Window;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_window;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class GUIFunctionality_search {

	private final GUI gui;
	private GUI_Pattern gui_pattern;
	private Map<Pattern_window, List<Window>> possible_matches_wm;
	private Table<Pattern_window, Window, List<Instance_window>> matches_table_wm;
	private List<Instance_window> not_overlapping;

	public GUIFunctionality_search(final GUI gui) {

		this.gui = gui;
	}

	public void init_match(final GUI_Pattern pattern, final boolean filter_dyn) throws Exception {

		this.gui_pattern = pattern;

		// the windows in the GUI are reduced to only the windows that can match
		// the windows in the pattern
		// list that contains for each pattern window
		// all the possible windows that can match
		Map<Pattern_window, List<Window>> possible_matches = new LinkedHashMap<>();
		// table that contains for each window_pattern and window the possible
		// instance
		Table<Pattern_window, Window, List<Instance_window>> matches_table = HashBasedTable
				.create();

		for (final Pattern_window pw : this.gui_pattern.getWindows()) {
			if (filter_dyn && pw.isDynamic()) {
				continue;
			}
			final List<Window> windows = new ArrayList<>();
			possible_matches.put(pw, windows);

			for (final Window w : this.gui.getWindows()) {

				final List<Instance_window> instances = pw.getMatches(w);
				if (instances.size() > 0) {
					windows.add(w);
					matches_table.put(pw, w, instances);
				}
			}
		}

		final Map<Pattern_window, List<Window>> new_possible_matches = new LinkedHashMap<>();
		final Table<Pattern_window, Window, List<Instance_window>> new_matches_table = HashBasedTable
				.create();

		// first we filter those without the right edges
		for (final Pattern_window pw : this.gui_pattern.getWindows()) {
			if (filter_dyn && pw.isDynamic()) {
				continue;
			}
			final List<Window> windows = new ArrayList<>();
			new_possible_matches.put(pw, windows);

			for (final Window w : possible_matches.get(pw)) {

				final List<Instance_window> instances = new ArrayList<>();
				new_matches_table.put(pw, w, instances);
				outloop: for (final Instance_window iw : matches_table.get(pw, w)) {
					// we check forward links
					for (final Pattern_action_widget paw : pw.getActionWidgets()) {
						// if it is not a edge
						if (this.gui_pattern.getStaticForwardLinks(paw.getId()).size() == 0) {
							continue;
						}
						boolean reached = true;
						boolean required = false;

						loop: for (final Action_widget aw : iw.getAWS_for_PAW(paw.getId())) {
							for (final Window ww : this.gui.getStaticForwardLinks(aw.getId())) {
								for (final Pattern_window ppw : this.gui_pattern
										.getStaticForwardLinks(paw.getId())) {
									if (ppw.getCardinality().getMin() > 0) {
										required = true;
									}
									if (possible_matches.get(ppw).contains(ww)) {
										continue loop;
									}
								}
							}
							reached = false;
							break;
						}

						if (!reached && required) {
							continue outloop;
						}
					}
					instances.add(iw);
				}
				if (instances.size() > 0) {
					windows.add(w);
				}
			}
		}

		possible_matches = new_possible_matches;
		matches_table = new_matches_table;

		// the windows found are partitioned into sets of connected windows
		// by recursively following the edges
		this.possible_matches_wm = possible_matches;
		this.matches_table_wm = matches_table;
	}

	public final List<Instance_GUI_pattern> match(final GUI_Pattern pattern) throws Exception {

		this.init_match(pattern, true);
		final List<Instance_GUI_pattern> out = new ArrayList<>();

		while (true) {
			this.not_overlapping = new ArrayList<>();
			for (final Window w : this.matches_table_wm.columnKeySet()) {
				final List<Instance_window> instancesnot = new ArrayList<>();
				final List<Instance_window> instances = new ArrayList<>();

				for (final Window ww : this.matches_table_wm.columnKeySet()) {

					for (final Pattern_window pw : this.matches_table_wm.rowKeySet()) {
						if (this.matches_table_wm.get(pw, ww) == null) {
							continue;
						}
						if (w == ww) {
							instances.addAll(this.matches_table_wm.get(pw, ww));
						}
						instancesnot.addAll(this.matches_table_wm.get(pw, ww));
					}
				}
				loop: for (final Instance_window iw : instances) {
					for (final Instance_window iww : instancesnot) {
						if (iw.isOverlap(iww)) {
							continue loop;
						}
					}
					this.not_overlapping.add(iw);
				}
			}

			final List<Entry<Window, Pattern_window>> tuples = new ArrayList<>();
			// we create a tuple for each match
			for (final Pattern_window pw : this.gui_pattern.getWindows()) {
				if (pw.isDynamic()) {
					continue;
				}
				for (final Window w : this.possible_matches_wm.get(pw)) {
					final Entry<Window, Pattern_window> e = new AbstractMap.SimpleEntry<>(w, pw);
					tuples.add(e);

				}
			}
			if (tuples.size() == 0) {
				break;
			}

			loop: for (final Entry<Window, Pattern_window> entry : tuples) {

				Instance_GUI_pattern match = this.traverse(entry.getKey(), entry.getValue(),
						new Instance_GUI_pattern(new GUI(), this.gui_pattern));

				if (match != null) {
					final List<Instance_window> otherinst = new ArrayList<>();
					for (final List<Instance_window> list : this.matches_table_wm.column(
							entry.getKey()).values()) {
						if (!this.matches_table_wm.row(entry.getValue()).containsValue(list)) {
							otherinst.addAll(list);
						}
					}
					if (otherinst.isEmpty()) {
						otherinst.add(null);
					}
					for (final Instance_window inw : otherinst) {

						if (this.correctMatch(match)) {
							out.add(match);
							// we filter out all the overlaps
							final Map<Pattern_window, List<Window>> new_possible_matches = new LinkedHashMap<>();
							final Table<Pattern_window, Window, List<Instance_window>> new_matches_table = HashBasedTable
									.create();

							// first we filter those without the right edges
							for (final Pattern_window pw : this.gui_pattern.getWindows()) {
								if (pw.isDynamic()) {
									continue;
								}
								final List<Window> windows = new ArrayList<>();
								new_possible_matches.put(pw, windows);

								for (final Window w : this.possible_matches_wm.get(pw)) {
									final List<Instance_window> instances = new ArrayList<>();
									outloop: for (final Instance_window iw : this.matches_table_wm
											.get(pw, w)) {
										for (final Instance_window iww : match.getWindows()) {
											if (iw.isOverlap(iww)) {
												continue outloop;
											}
										}
										instances.add(iw);
									}
									if (instances.size() > 0) {
										new_matches_table.put(pw, w, instances);
										windows.add(w);
									}
								}
							}
							this.matches_table_wm = new_matches_table;
							this.possible_matches_wm = new_possible_matches;
							break loop;
						}
						boolean overlap = false;
						if (inw == null) {
							break;
						}
						for (final Instance_window iiw : match.getWindows()) {
							if (iiw.isOverlap(inw)) {
								overlap = true;
							}
						}
						if (!overlap) {
							final Instance_GUI_pattern newmatch = this.traverse(inw.getInstance(),
									inw.getPattern(), match);
							if (newmatch != null) {
								match = newmatch;
							}
						}
					}
					// if we are here it means the match was not correct
					for (final Instance_window iw : match.getWindows()) {
						if (iw.getInstance().getId().equals(entry.getKey())
								&& iw.getPattern().getId().equals(entry.getValue())) {
							this.matches_table_wm.get(entry.getValue(), entry.getKey()).remove(iw);
							break;
						}
					}

				} else {
					this.matches_table_wm.remove(entry.getValue(), entry.getKey());
					this.possible_matches_wm.get(entry.getValue()).remove(entry.getKey());
				}
			}
		}

		this.gui_pattern = null;
		return out;
	}

	private boolean correctMatch(final Instance_GUI_pattern match) throws Exception {

		// the cardinality of each pattern window is verified
		boolean check = true;
		for (final Pattern_window pw : this.gui_pattern.getWindows()) {
			if (pw.isDynamic()) {
				continue;
			}
			final List<Instance_window> instances = match.getWindows().stream()
					.filter(e -> e.getPattern() == pw).collect(Collectors.toList());

			if (instances.size() == 0 && pw.getCardinality().getMin() > 0) {

				// if the window is not reached by any static
				// edge
				// it can be missing
				check = false;
				// System.out.println("used");
				//
				// if
				// (match.getGuipattern().getStaticBackwardLinks(pw.getId()).size()
				// > 0) {
				// check = false;
				// System.out.println("used2");
				// }
			} else {
				if (instances.size() > pw.getCardinality().getMax()
						|| instances.size() < pw.getCardinality().getMin()) {
					check = false;
				}
			}
		}
		return check;
	}

	/*
	 * recursive function that traverses the graph to find instances of GUI
	 * patterns. A window to match the pattern must have all the forward edges.
	 * A forward edge can be omitted if the target pattern_window is not
	 * required. A window to match the pattern must have at least one correct
	 * backward edge (if the pattern window has some).
	 */
	public Instance_GUI_pattern traverse(final Window w, final Pattern_window pw,
			Instance_GUI_pattern igp) throws Exception {

		// if the window is already part of the instance_gui_pattern
		if (igp.getPW_for_W(w.getId()).contains(pw)) {
			return igp;
		}

		boolean correct = false;

		final Instance_GUI_pattern igp_copy = igp.clone();
		final List<Instance_window> inst_app = this.matches_table_wm.get(pw, w);
		final List<Instance_window> inst = new ArrayList<>();
		// we filter the instances already part of a match
		if (inst_app != null) {
			loop: for (final Instance_window iw : inst_app) {
				for (final Instance_window iww : igp.getWindows()) {
					if (iw.isOverlap(iww)) {
						continue loop;
					}
				}
				inst.add(iw);
			}
		}
		final List<Instance_window> marked = new ArrayList<>();
		if (inst != null) {
			// to avoid problems of concurrency
			final List<Instance_window> instances = new ArrayList<>(inst);
			for (final Instance_window instance : instances) {

				boolean back = false;
				String brokenaw = null;
				String brokenpaw = null;
				if (marked.contains(instance)) {
					continue;
				}

				this.matches_table_wm.get(pw, w).remove(instance);
				// the instance_gui pattern_is updated
				igp.addWindow(instance);
				if (igp.getGui().getWindow(instance.getInstance().getId()) == null) {
					igp.getGui().addWindow(instance.getInstance());
				}

				correct = true;

				// the forward links are checked
				loops_1: for (final Pattern_action_widget paw : pw.getActionWidgets()) {

					final List<Action_widget> aws = instance.getAWS_for_PAW(paw.getId());
					if (aws != null) {
						for (final Action_widget aw : aws) {
							// boolean used to recognise the optional windows
							boolean check_optional = false;
							boolean check = false;
							for (final Pattern_window target_pw : this.gui_pattern
									.getStaticForwardLinks(paw.getId())) {
								if (target_pw.isDynamic()) {
									continue;
								}

								if (this.gui.getStaticForwardLinks(aw.getId()).size() == 0) {
									// if there are not static links we try to
									// match the target_pw in the same window
									final Instance_GUI_pattern new_instance = this.traverse(w,
											target_pw, igp.clone());
									if (new_instance != null) {
										check = true;
										igp = new_instance;
									}

								} else {
									for (final Window target_w : this.gui.getStaticForwardLinks(aw
											.getId())) {
										// if the target_pw has cardinality 1
										// and it
										// is already in the pattern
										// then the edge must go to the window
										// already matched
										final List<Window> lws = igp.getWindows().stream()
												.filter(e -> e.getPattern() == target_pw)
												.map(e -> e.getInstance())
												.collect(Collectors.toList());
										if (!(target_pw.getCardinality().getMax() == 1
												&& lws.size() > 0 && !lws.contains(target_w))) {
											final Instance_GUI_pattern new_instance = this
													.traverse(target_w, target_pw, igp.clone());
											if (new_instance != null) {
												check = true;
												new_instance.getGui().addStaticEdge(aw.getId(),
														target_w.getId());
												igp = new_instance;
											}
										}
									}
								}
								if (target_pw.getCardinality().getMin() != 0) {
									check_optional = true;
								}
							}

							if (!check_optional) {
								check = true;
							}
							if (!check) {
								brokenaw = aw.getId();
								brokenpaw = paw.getId();
								correct = false;
								break loops_1;
							}
						}
					}
				}

				// if everything was fine the backward links are checked
				// backward links are traversed to find the biggest instance of
				// the pattern
				// we require only 1 backward link for the window to match
				if (correct) {
					back = true;
					boolean check_optional = false;
					correct = false;
					final List<Pattern_window> done = new ArrayList<>();
					for (final Pattern_action_widget paw : this.gui_pattern
							.getStaticBackwardLinks(pw.getId())) {
						// boolean used to recognise the optional windows
						final Pattern_window source_pw = this.gui_pattern
								.getActionWidget_Window(paw.getId());
						if (source_pw.isDynamic()) {
							continue;
						}
						for (final Action_widget aw : this.gui.getStaticBackwardLinks(instance
								.getInstance().getId())) {
							final Window source_w = this.gui.getActionWidget_Window(aw.getId());

							// if the target_pw has cardinality 1 and it
							// is already in the pattern
							// then the edge must go to the window
							// already matched
							final List<Window> lws = igp.getWindows().stream()
									.filter(e -> e.getPattern() == source_pw)
									.map(e -> e.getInstance()).collect(Collectors.toList());
							if (!(source_pw.getCardinality().getMax() == 1 && lws.size() > 0 && !lws
									.contains(source_w))) {

								final Instance_GUI_pattern new_instance = this.traverse(source_w,
										source_pw, igp.clone());
								if (new_instance != null) {
									if (paw == new_instance.getPAW_for_AW(aw.getId())) {
										correct = true;
										new_instance.getGui().addStaticEdge(aw.getId(),
												instance.getInstance().getId());
										igp = new_instance;
									}
									// else {
									// igp = igp_copy2;
									// }
								}
							} else {
								if (paw == igp.getPAW_for_AW(aw.getId())) {
									correct = true;
									igp.getGui().addStaticEdge(aw.getId(),
											instance.getInstance().getId());
								}
							}
						}
						if (!correct && !done.contains(source_pw)) {

							final Instance_GUI_pattern new_instance = this.traverse(w, source_pw,
									igp.clone());
							done.add(source_pw);
							if (new_instance != null) {
								if (new_instance.getWS_for_PW(source_pw.getId()).contains(w)) {
									correct = true;
									igp = new_instance;
								}
								// else {
								// igp = igp_copy2;
								// }
							}
						}
						if (source_pw.getCardinality().getMin() != 0 /*
						 * &&
						 * source_pw
						 * != pw
						 */) {
							check_optional = true;
						}
					}
					if (!check_optional) {
						correct = true;
					}
				}
				if (correct) {
					break;
				}
				this.matches_table_wm.get(pw, w).add(instance);
				if (this.not_overlapping.contains(instance)) {
					if (back) {
						return null;
					}
					assert (brokenaw != null & brokenpaw != null);
					for (final Instance_window iw : instances) {
						if (iw.getPAW_for_AW(brokenaw).getId().equals(brokenpaw)) {
							marked.add(iw);
						}
					}
				}
				igp = igp_copy.clone();
			}
		}
		if (correct) {
			return igp;
		} else {
			return null;
		}
	}
}
