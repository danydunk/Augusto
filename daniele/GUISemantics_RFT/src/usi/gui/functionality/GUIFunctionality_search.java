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
import src.usi.pattern.structure.Cardinality;
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
	private List<Window> matches_to_root;

	public GUIFunctionality_search(final GUI gui) {

		this.gui = gui;
	}

	/**
	 * Function that finds all the pattern windows reachable from the root
	 * pattern window of the pattern with static edges.
	 *
	 * @param pattern
	 * @return
	 * @throws Exception
	 *             if the number of root windows in the pattern is different
	 *             than 1
	 */
	private List<Pattern_window> getReachableWindows(final Pattern_window root,
			final GUI_Pattern pattern) throws Exception {

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
		// root window
		final List<Pattern_window> roots = pattern.getWindows().stream()
				.filter(e -> (e.isRoot() == true)).collect(Collectors.toList());
		assert (roots.size() == 1);
		final Pattern_window root = roots.get(0);

		// the windows in the GUI are reduced to only the windows that can match
		// the windows in the pattern
		// list that contains for each pattern window
		// all the possible windows that can match
		final Map<Pattern_window, List<Window>> possible_matches = new LinkedHashMap<>();
		// table that contains for each window_pattern and window the possible
		// instance
		final Table<Pattern_window, Window, List<Instance_window>> matches_table = HashBasedTable
				.create();

		for (final Pattern_window pw : this.getReachableWindows(root, pattern)) {
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

		// the windows found are partitioned into sets of connected windows
		// by recursively following the edges
		this.possible_matches_wm = possible_matches;
		this.matches_table_wm = matches_table;
		this.matches_to_root = new ArrayList<>();

		while (true) {
			final List<Window> matches_to_root_copy = new ArrayList<>(this.matches_to_root);

			final List<Entry<Window, Pattern_window>> tuples = new ArrayList<>();
			// we create a tuple for each match of the root window
			for (final Window w : possible_matches.get(root)) {
				// we filter the ones that are already part of a instance
				if (!this.matches_to_root.contains(w)) {
					final Entry<Window, Pattern_window> e = new AbstractMap.SimpleEntry<>(w, root);
					tuples.add(e);
				}
			}

			for (final Entry<Window, Pattern_window> entry : tuples) {

				final Instance_GUI_pattern match = this.traverse(entry.getKey(), entry.getValue(),
						new Instance_GUI_pattern(new GUI(), this.gui_pattern));
				if (match != null) {

					// the cardinality of each pattern window is verified
					boolean check = true;
					for (final Pattern_window pw : this.getReachableWindows(root, pattern)) {
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
				this.matches_to_root = matches_to_root_copy;
			}

			// the number of root window matches is equal to the total of
			// possibilities
			if (this.matches_to_root.size() == this.possible_matches_wm.get(root).size()) {
				break;
			}
			// if the number of root windows matched has not changed
			if (this.matches_to_root.size() == matches_to_root_copy.size()) {
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

		// if the window is already part of the instance_gui_pattern
		if (pw == igp.getPW_for_W(w.getId())) {
			return igp;
		}
		// if the window is already matched to another pattern window
		if (igp.getPW_for_W(w.getId()) != null) {
			return null;
		}
		if (pw.isRoot()) {
			this.matches_to_root.add(w);
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
