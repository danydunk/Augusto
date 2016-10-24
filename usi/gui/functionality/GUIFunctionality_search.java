package usi.gui.functionality;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class GUIFunctionality_search {

	private final GUI gui;
	private GUI_Pattern gui_pattern;
	private Map<Pattern_window, List<Window>> possible_matches_wm;
	private Table<Pattern_window, Window, List<Instance_window>> matches_table_wm;

	public GUIFunctionality_search(final GUI gui) {

		this.gui = gui;
	}

	/**
	 * Function that finds all the pattern windows reachable from the root
	 * pattern window of the pattern with static edges.
	 *
	 *
	 * @param pattern
	 * @return
	 * @throws Exception
	 *             if the number of root windows in the pattern is different
	 *             than 1
	 */
	private List<Pattern_window> getReachableWindows(final GUI_Pattern pattern) throws Exception {

		final List<Pattern_window> roots = pattern.getWindows().stream()
				.filter(e -> (e.getRoot() == true)).collect(Collectors.toList());

		assert (roots.size() == 1);

		final Pattern_window root = roots.get(0);
		final List<Pattern_window> out = new ArrayList<>();
		this.recursion(out, root, pattern);
		return out;
	}

	private void recursion(final List<Pattern_window> wins, final Pattern_window current,
			final GUI_Pattern pattern) throws Exception {

		if (wins.contains(current)) {
			return;
		}

		wins.add(current);
		for (final Pattern_action_widget paw : current.getActionWidgets()) {
			for (final Pattern_window pw : pattern.getStaticForwardLinks(paw.getId())) {
				this.recursion(wins, pw, pattern);
			}
		}
	}

	public List<Instance_GUI_pattern> match(final GUI_Pattern pattern) throws Exception {

		this.gui_pattern = pattern;
		final List<Instance_GUI_pattern> out = new ArrayList<>();

		// the windows in the GUI are reduced to only the windows that can match
		// the windows in the pattern
		// list that contains for each pattern window
		// all the possible windows that can match
		Map<Pattern_window, List<Window>> possible_matches = new LinkedHashMap<>();
		// table that contains for each window_pattern and window the possible
		// instances
		Table<Pattern_window, Window, List<Instance_window>> matches_table = HashBasedTable
				.create();

		for (final Pattern_window pw : this.getReachableWindows(pattern)) {
			final List<Window> windows = new ArrayList<>();
			possible_matches.put(pw, windows);

			for (final Window w : this.gui.getWindows()) {
				final List<Instance_window> instances = pw.getMatches(w);
				if (instances.size() > 0) {
					windows.add(w);
					matches_table.put(pw, w, instances);
				}
			}
			// if the possible matches are less than the minimum number required
			// for the pattern window the pattern cannot be found
			// if (windows.size() < pw.getCardinality().getMin()) {
			// return out;
			// }
		}

		// the windows found are partitioned into sets of connected windows
		// by recursively following the edges
		this.possible_matches_wm = possible_matches;
		this.matches_table_wm = matches_table;

		while (true) {
			possible_matches = copyMap(this.possible_matches_wm);
			matches_table = copyTable(this.matches_table_wm);

			final List<Entry<Window, Pattern_window>> tuples = new ArrayList<>();

			for (final Entry<Pattern_window, List<Window>> entry : this.possible_matches_wm
					.entrySet()) {
				for (final Window w : entry.getValue()) {
					final Entry<Window, Pattern_window> e = new AbstractMap.SimpleEntry<>(w,
							entry.getKey());
					tuples.add(e);
				}
			}
			// modified to guarantee entry order
			// for (final Pattern_window pw : this.possible_matches_wm.keySet())
			// {
			// for (final Window w : this.possible_matches_wm.get(pw)) {
			// final Entry<Window, Pattern_window> entry = new
			// AbstractMap.SimpleEntry<>(w, pw);
			// tuples.add(entry);
			// }
			// }

			for (final Entry<Window, Pattern_window> entry : tuples) {

				final Instance_GUI_pattern match = this.traverse(entry.getKey(), entry.getValue(),
						new Instance_GUI_pattern(new GUI(), this.gui_pattern));
				if (match != null) {

					// the cardinality of each pattern window is verified
					boolean check = true;
					for (final Pattern_window pw : this.getReachableWindows(pattern)) {
						final List<Instance_window> instances = match.getWindows().stream()
								.filter(e -> e.getPattern() == pw).collect(Collectors.toList());

						if (instances.size() > pw.getCardinality().getMax()
								|| instances.size() < pw.getCardinality().getMin()) {
							// if thes window is not reached by any static edge
							// it can be missing
							if (pattern.getStaticBackwardLinks(pw.getId()).size() > 0) {
								check = false;
							}
						}
					}

					if (check) {
						out.add(match);
						break;
					}
				}
				this.possible_matches_wm = copyMap(possible_matches);
				this.matches_table_wm = copyTable(matches_table);
			}

			// the number of remained windows
			final int wn = tableSize(this.matches_table_wm);
			if (wn == 0 || wn == tableSize(matches_table)) {
				break;
			}
		}

		this.gui_pattern = null;
		return out;
	}

	/*
	 * recursive function that traverses the graph to find instances of GUI
	 * patterns. A window to match the pattern must have all the forward edges.
	 * A forward edge can be omitted if the target pattern_window is not
	 * required. A window to match the pattern must have at least one correct
	 * backward edge (if the pattern window has some).
	 */
	private Instance_GUI_pattern traverse(final Window w, final Pattern_window pw,
			Instance_GUI_pattern igp) throws Exception {

		// System.out.println(w.getId()+" - "+pw.getId());
		// if the window is already part of the instance_gui_pattern
		if (pw == igp.getPW_for_W(w.getId())) {
			return igp;
		}
		// if the window is already matched to another pattern window
		if (igp.getPW_for_W(w.getId()) != null) {
			return null;
		}

		boolean correct = false;
		final Instance_GUI_pattern igp_copy = igp.clone();
		final List<Instance_window> inst = this.matches_table_wm.get(pw, w);
		if (inst != null) {
			// to avoid problems of concurrency
			final List<Instance_window> instances = new ArrayList<>(inst);

			// List used to keep track of all the instances that are surely not
			// satisfying the requirements
			final List<Instance_window> marked = new ArrayList<Instance_window>();

			for (final Instance_window instance : instances) {
				if (marked.contains(instance)) {
					continue;
				}

				// the working structures are saved
				final Map<Pattern_window, List<Window>> possible_matches_copy = copyMap(this.possible_matches_wm);
				final Table<Pattern_window, Window, List<Instance_window>> matches_table_copy = copyTable(this.matches_table_wm);

				// the instance_windows that match this one are remove
				this.adaptWorkStructures(instance);
				// the instance_gui pattern_is updated
				igp.addWindow(instance);
				igp.getGui().addWindow(instance.getInstance());

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
								for (final Window target_w : this.gui.getStaticForwardLinks(aw
										.getId())) {
									// if the target_pw has cardinality 1 and it
									// is already in the pattern
									// then the edge must go to the window
									// already matched
									final List<Window> lws = igp.getWindows().stream()
											.filter(e -> e.getPattern() == target_pw)
											.map(e -> e.getInstance()).collect(Collectors.toList());
									if (!(target_pw.getCardinality() == Cardinality.ONE
											&& lws.size() > 0 && !lws.contains(target_w))) {
										final Instance_GUI_pattern new_instance = this.traverse(
												target_w, target_pw, igp.clone());
										if (new_instance != null) {
											check = true;
											new_instance.getGui().addStaticEdge(aw.getId(),
													target_w.getId());
											igp = new_instance;
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
					boolean check_optional = false;
					correct = false;
					for (final Pattern_action_widget paw : this.gui_pattern
							.getStaticBackwardLinks(pw.getId())) {
						// boolean used to recognise the optional windows
						final Pattern_window source_pw = this.gui_pattern
								.getActionWidget_Window(paw.getId());

						for (final Action_widget aw : this.gui.getStaticBackwardLinks(instance
								.getInstance().getId())) {
							final Window source_w = this.gui.getActionWidget_Window(aw.getId());

							// if(igp.getGui().getAction_widgets().contains(aw)
							// &&
							// igp.getGui().isEdge(aw, instance.getInstance())
							// &&
							// igp.getWindows_mapping().get(source_w) ==
							// source_pw)
							// final Instance_GUI_pattern igp_copy2 =
							// igp.clone();
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

				final List<Instance_window> newMarked = instances
						.parallelStream()
						.filter(e -> !marked.contains(e))
						.filter(e -> {
							for (final Action_widget aw : e.getInstance().getActionWidgets()) {
								if (e.getPAW_for_AW(aw.getId()) != instance.getPAW_for_AW(aw
										.getId())) {
									return false;
								}
							}
							return true;
						}).collect(Collectors.toList());

				// marked.addAll(new ArrayList<Instance_window>());
				marked.addAll(newMarked);

				this.possible_matches_wm = copyMap(possible_matches_copy);
				this.matches_table_wm = copyTable(matches_table_copy);
				// igp.removeWindow(instance);
				// igp.getGui().removeWindow(instance.getInstance());
				igp = igp_copy.clone();
			}
		}
		if (correct) {
			return igp;
		} else {
			return null;
		}
	}

	/*
	 * Function that removes all the instances that are not feasible if the
	 * input window is added to the the output instance
	 */
	private void adaptWorkStructures(final Instance_window iw) {

		final List<Map.Entry<Pattern_window, List<Instance_window>>> entries = this.matches_table_wm
				.column(iw.getInstance())
				.entrySet()
				.stream()
				.map(e -> {
					final List<Instance_window> l = new ArrayList<>(e.getValue().parallelStream()
							.filter(ee -> !iw.isOverlap(ee)).collect(Collectors.toList()));
					return new AbstractMap.SimpleEntry<Pattern_window, List<Instance_window>>(e
							.getKey(), l);
				}).collect(Collectors.toList());

		for (final Entry<Pattern_window, List<Instance_window>> entry : entries) {
			this.matches_table_wm.put(entry.getKey(), iw.getInstance(), entry.getValue());
			if (entry.getValue().size() == 0) {
				this.possible_matches_wm.get(entry.getKey()).remove(iw.getInstance());
			}
		}
	}

	private static <T, TT> Map<T, List<TT>> copyMap(final Map<T, List<TT>> in) {

		final Map<T, List<TT>> out = new LinkedHashMap<T, List<TT>>();
		for (final T k : in.keySet()) {
			final List<TT> l = new ArrayList<>();
			for (final TT el : in.get(k)) {
				l.add(el);
			}
			out.put(k, l);
		}
		return out;
	}

	private static <T, TT, TTT> Table<T, TT, List<TTT>> copyTable(final Table<T, TT, List<TTT>> in) {

		final Table<T, TT, List<TTT>> out = HashBasedTable.create();
		for (final Table.Cell<T, TT, List<TTT>> c : in.cellSet()) {
			final List<TTT> liw = new ArrayList<>();
			if (c.getValue() != null) {
				for (final TTT iw : c.getValue()) {
					liw.add(iw);
				}
			}
			out.put(c.getRowKey(), c.getColumnKey(), liw);
		}
		return out;
	}

	// private static <T, TT> int mapSize(Map<T, List<TT>> in) {
	// int size = 0;
	// for(T k : in.keySet()) {
	// size += in.get(k).size();
	// }
	// return size;
	// }

	private static <T, TT, TTT> int tableSize(final Table<T, TT, List<TTT>> in) {

		int size = 0;
		for (final Table.Cell<T, TT, List<TTT>> c : in.cellSet()) {
			if (c.getValue() != null) {
				size += c.getValue().size();
			}
		}

		return size;
	}
}
