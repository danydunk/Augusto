package usi.gui.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import usi.gui.functionality.instance.Instance_GUI_pattern;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.structure.Action_widget;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;
import usi.pattern.structure.Cardinality;
import usi.pattern.structure.Pattern_action_widget;
import usi.pattern.structure.Pattern_input_widget;
import usi.pattern.structure.Pattern_selectable_widget;
import usi.pattern.structure.Pattern_window;

import com.google.common.collect.Lists;

public class SpecificSemantics extends FunctionalitySemantics {

	protected List<Signature> concrete_windows;
	protected List<Signature> concrete_action_w;
	protected List<Signature> concrete_input_w;
	protected List<Signature> concrete_selectable_w;

	public SpecificSemantics(final List<Signature> signatures, final List<Fact> facts,
			final List<Predicate> predicates, final List<Function> functions,
			final List<String> open_statments) throws Exception {

		super(signatures, facts, predicates, functions, open_statments);

		this.concrete_windows = new ArrayList<>();
		this.concrete_action_w = new ArrayList<>();
		this.concrete_input_w = new ArrayList<>();
		this.concrete_selectable_w = new ArrayList<>();

		for (final Signature sig : signatures) {
			if (sig.isSubset()) {
				continue;
			}

			if (!sig.isAbstract_()) {

				if (sig.getParent().contains(this.window_signature)) {
					this.concrete_windows.add(sig);
				} else {
					for (final Signature par : sig.getParent()) {
						if (this.windows_extensions.contains(par)) {
							this.concrete_windows.add(sig);
							break;
						}
					}
				}

				if (sig.getParent().contains(this.action_w_signature)) {
					this.concrete_action_w.add(sig);
				} else {
					for (final Signature par : sig.getParent()) {
						if (this.action_w_extensions.contains(par)) {
							this.concrete_action_w.add(sig);
							break;
						}
					}
				}

				if (sig.getParent().contains(this.input_w_signature)) {
					this.concrete_input_w.add(sig);
				} else {
					for (final Signature par : sig.getParent()) {
						if (this.input_w_extensions.contains(par)) {
							this.concrete_input_w.add(sig);
							break;
						}
					}
				}

				if (sig.getParent().contains(this.selectable_w_signature)) {
					this.concrete_selectable_w.add(sig);
				} else {
					for (final Signature par : sig.getParent()) {
						if (this.selectable_w_extensions.contains(par)) {
							this.concrete_selectable_w.add(sig);
							break;
						}
					}
				}

			}
		}
		if ((this.concrete_windows.size() + this.concrete_action_w.size() + this.concrete_input_w
				.size()) == 0) {
			throw new Exception("SpecificSemantics: error in constructor");
		}
	}

	static public SpecificSemantics instantiate(final Alloy_Model in) throws Exception {

		final List<Signature> sigs = new ArrayList<>(in.getSignatures());
		final List<Fact> facts = new ArrayList<>(in.getFacts());
		final List<Predicate> predicates = new ArrayList<>(in.getPredicates());
		final List<Function> functions = new ArrayList<>(in.getFunctions());
		final List<String> imports = new ArrayList<>(in.getOpenStatements());

		final SpecificSemantics out = new SpecificSemantics(sigs, facts, predicates, functions,
				imports);
		for (final String run : in.getRun_commands()) {
			out.addRun_command(run);
		}
		return out;
	}

	public List<Signature> getConcrete_windows() {

		return new ArrayList<>(this.concrete_windows);
	}

	public List<Signature> getConcrete_action_w() {

		return new ArrayList<>(this.concrete_action_w);
	}

	public List<Signature> getConcrete_input_w() {

		return new ArrayList<>(this.concrete_input_w);
	}

	public List<Signature> getConcrete_selectable_w() {

		return new ArrayList<>(this.concrete_selectable_w);
	}

	static public SpecificSemantics generate(final Instance_GUI_pattern in) throws Exception {

		final FunctionalitySemantics func_semantics = in.getGuipattern().getSemantics();
		if (func_semantics == null) {
			throw new Exception(
					"SpecificSemantics - generate: semantics is missing in gui pattern.");
		}
		// these lists are going to be used to create the specific semantics
		final List<Signature> signatures = new ArrayList<>(func_semantics.getSignatures());
		final List<Fact> facts = new ArrayList<>(func_semantics.getFacts());
		final List<Predicate> predicates = new ArrayList<>(func_semantics.getPredicates());
		final List<Function> functions = new ArrayList<>(func_semantics.getFunctions());
		final List<String> opens = new ArrayList<>(func_semantics.getOpenStatements());

		final List<Window> windows = new ArrayList<Window>(in.getGui().getWindows());
		// We sort the windows, due they came from a set. Let's try to minimize
		// the nondeterminism
		Collections.sort(windows, new Comparator<Window>() {

			@Override
			public int compare(final Window o1, final Window o2) {

				return o1.getId().compareTo(o2.getId());
			}
		});

		// we iterate each window
		final Map<Window, Signature> added_windows = new LinkedHashMap<>();
		for (final Window win : windows) {
			// we find the associated pattern window
			final Pattern_window pw = in.getPW_for_W(win.getId());
			final List<Signature> to_search = new ArrayList<>(
					func_semantics.getWindows_extensions());
			to_search.add(func_semantics.window_signature);

			final Signature w_sig = AlloyUtil.searchSignatureInList(to_search,
					pw.getAlloyCorrespondence());

			if (w_sig == null) {
				throw new Exception("SpecificSemantics - generate: wrong alloy corrispondence "
						+ pw.getAlloyCorrespondence());
			}

			final Signature concreteWinSig = new Signature("Window_" + win.getId(),
					Cardinality.ONE, false, Lists.newArrayList(w_sig), false);

			// We add the windows to the list of signatures
			signatures.add(concreteWinSig);
			added_windows.put(win, concreteWinSig);
		}

		// we add a fact for the number of windows
		final Fact win_num = new Fact("windows_number", "#Window = " + added_windows.size());
		facts.add(win_num);

		// we iterate each window added (we need to do it in two cicles because
		// to add the edges in the action windows we need to know which windows
		// will be in the model
		for (final Window win : added_windows.keySet()) {

			// Now, we iterates the input widgets
			final Map<Input_widget, Signature> input_widgets = new LinkedHashMap<>();
			for (final Input_widget iw : win.getInputWidgets()) {

				final Pattern_input_widget piw = in.getPIW_for_IW(iw.getId());

				if (piw == null) {
					// IW not mapped
					continue;
				}

				Signature piw_sig = null;

				piw_sig = AlloyUtil.searchForParent(func_semantics, piw);

				final Signature sigIW = new Signature("Input_widget_" + iw.getId(),
						Cardinality.ONE, false, Lists.newArrayList(piw_sig), false);
				// if the widget is a option input widget we create a signature
				// to contain its values
				if (iw instanceof Option_input_widget) {
					final Option_input_widget oiw = (Option_input_widget) iw;
					Signature value = null;
					for (final Signature sign : signatures) {
						if (sign.getIdentifier().equals("Value")) {
							value = sign;
							break;
						}
					}
					if (value == null) {
						throw new Exception(
								"SpecificSemantics - generate: value signature not found.");
					}
					for (int cc = 0; cc < oiw.getSize(); cc++) {
						final Signature values = new Signature("Input_widget_" + iw.getId()
								+ "_value_" + cc, Cardinality.ONE, false,
								Lists.newArrayList(value), false);
						signatures.add(values);
					}
				}
				signatures.add(sigIW);
				input_widgets.put(iw, sigIW);
			}
			// a fact is created to associate the IWS to the window
			final Fact factIW = AlloyUtil.createFactsForInputWidget(input_widgets,
					added_windows.get(win));
			if (!"".equals(factIW.getContent().trim())) {
				facts.add(factIW);
			}

			// Now, we iterates the action widgets
			final Map<Action_widget, Signature> action_widgets = new LinkedHashMap<>();

			for (final Action_widget aw : win.getActionWidgets()) {
				final Pattern_action_widget paw = in.getPAW_for_AW(aw.getId());

				if (paw == null) {
					// AW not mapped
					continue;
				}
				final Signature paw_sig = AlloyUtil.searchForParent(func_semantics, paw);

				final Signature sigAW = new Signature("Action_widget_" + aw.getId(),
						Cardinality.ONE, false, Lists.newArrayList(paw_sig), false);

				signatures.add(sigAW);
				action_widgets.put(aw, sigAW);
			}
			// a fact is created to associate the AWS to the window
			final Fact factAW = AlloyUtil.createFactsForActionWidget(action_widgets,
					added_windows.get(win), added_windows, in.getGui());
			if (!"".equals(factAW.getContent())) {
				facts.add(factAW);
			}

			// Now, we iterates the selectable widgets
			final Map<Selectable_widget, Signature> selectable_widgets = new LinkedHashMap<>();

			for (final Selectable_widget sw : win.getSelectableWidgets()) {

				final Pattern_selectable_widget psw = in.getPSW_for_SW(sw.getId());

				if (psw == null) {
					// SW not mapped
					continue;
				}
				final Signature psw_sig = AlloyUtil.searchForParent(func_semantics, psw);

				final Signature sigSW = new Signature("Selectable_widget_" + sw.getId(),
						Cardinality.ONE, false, Lists.newArrayList(psw_sig), false);

				signatures.add(sigSW);
				selectable_widgets.put(sw, sigSW);
			}
			// a fact is created to associate the AWS to the window
			final Fact factSW = AlloyUtil.createFactsForSelectableWidget(selectable_widgets,
					added_windows.get(win));
			if (!"".equals(factSW.getContent())) {
				facts.add(factSW);
			}
		}

		final Alloy_Model specific_model = new Alloy_Model(signatures, facts, predicates,
				functions, opens);
		return instantiate(specific_model);
	}

}
