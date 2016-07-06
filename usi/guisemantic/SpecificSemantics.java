package usi.guisemantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;
import usi.guisemantic.alloy.AlloyUtil;
import usi.guisemantic.alloy.Alloy_Model;
import usi.guisemantic.alloy.entity.Fact;
import usi.guisemantic.alloy.entity.Function;
import usi.guisemantic.alloy.entity.Predicate;
import usi.guisemantic.alloy.entity.Signature;

import com.google.common.collect.Lists;

public class SpecificSemantics extends FunctionalitySemantics {

	protected List<Signature> concrete_windows;
	protected List<Signature> concrete_action_w;
	protected List<Signature> concrete_input_w;

	public SpecificSemantics(final List<Signature> signatures, final List<Fact> facts,
			final List<Predicate> predicates, final List<Function> functions,
			final List<String> open_statments) throws Exception {

		super(signatures, facts, predicates, functions, open_statments);

		this.concrete_windows = new ArrayList<>();
		this.concrete_action_w = new ArrayList<>();
		this.concrete_input_w = new ArrayList<>();
		for (final Signature sig : signatures) {
			if (sig.isSubset()) {
				continue;
			}
			if ((sig.hasParent(this.window_signature) || this.windows_extensions.contains(sig
					.getParent())) && !sig.isAbstract_()) {
				this.concrete_windows.add(sig);
			}
			if ((sig.getParent().contains(this.action_w_signature) || this.action_w_extensions
					.contains(sig.getParent())) && !sig.isAbstract_()) {
				this.concrete_action_w.add(sig);
			}
			if ((sig.getParent().contains(this.input_w_signature) || this.input_w_extensions
					.contains(sig.getParent())) && !sig.isAbstract_()) {
				this.concrete_input_w.add(sig);
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

		return new SpecificSemantics(sigs, facts, predicates, functions, imports);
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

		final Map<Window, Pattern_window> windowsMap = in.getWindows_mapping();

		final List<Window> windows = new ArrayList<Window>(windowsMap.keySet());
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
			final Pattern_window pw = windowsMap.get(win);
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

		// we iterate each window added (we need to do it in two cicles because
		// to add the edges in the action windows we need to know which windows
		// will be in the model
		for (final Window win : added_windows.keySet()) {

			// Now, we iterates the input widgets
			final Map<Input_widget, Signature> input_widgets = new LinkedHashMap<>();
			for (final Input_widget iw : win.getInputWidgets()) {

				final Pattern_input_widget piw = in.getInput_widgets_mapping().get(iw);

				if (piw == null) {
					// IW not mapped
					continue;
				}

				Signature piw_sig = null;

				if (piw.getAlloyCorrespondence() != null
						&& piw.getAlloyCorrespondence().length() > 0) {
					// the pattern input widget signature is retrieved
					final List<Signature> to_search = new ArrayList<>(
							func_semantics.getInput_w_extensions());
					to_search.add(func_semantics.input_w_signature);
					piw_sig = AlloyUtil.searchSignatureInList(to_search,
							piw.getAlloyCorrespondence());
					System.out.println(piw.getAlloyCorrespondence());
					if (piw_sig == null) {
						throw new Exception(
								"SpecificSemantics - generate: wrong alloy corrispondence "
										+ piw.getAlloyCorrespondence());
					}
				} else {
					piw_sig = func_semantics.getInput_w_signature();
				}
				final Signature sigIW = new Signature("Input_widget_" + iw.getId(),
						Cardinality.ONE, false, Lists.newArrayList(piw_sig), false);

				signatures.add(sigIW);
				input_widgets.put(iw, sigIW);
			}
			// a fact is created to associate the IWS to the window
			final Fact factIW = createFactsForInputWidget(input_widgets, added_windows.get(win));
			if (!"".equals(factIW.getContent())) {
				facts.add(factIW);
			}

			// Now, we iterates the action widgets
			final Map<Action_widget, Signature> action_widgets = new LinkedHashMap<>();

			for (final Action_widget aw : win.getActionWidgets()) {

				final Pattern_action_widget paw = in.getAction_widgets_mapping().get(aw);

				if (paw == null) {
					// AW not mapped
					continue;
				}
				Signature paw_sig = null;
				if (paw.getAlloyCorrespondence() != null
						&& paw.getAlloyCorrespondence().length() > 0) {

					// the pattern action widget signature is retrieved
					final List<Signature> to_search = new ArrayList<>(
							func_semantics.getAction_w_extensions());
					to_search.add(func_semantics.action_w_signature);
					paw_sig = AlloyUtil.searchSignatureInList(to_search,
							paw.getAlloyCorrespondence());

					if (paw_sig == null) {
						throw new Exception(
								"SpecificSemantics - generate: wrong alloy corrispondence "
										+ paw.getAlloyCorrespondence());
					}
				} else {
					paw_sig = func_semantics.getAction_w_signature();
				}

				final Signature sigAW = new Signature("Action_widget_" + aw.getId(),
						Cardinality.ONE, false, Lists.newArrayList(paw_sig), false);

				signatures.add(sigAW);
				action_widgets.put(aw, sigAW);
			}
			// a fact is created to associate the AWS to the window
			final Fact factAW = createFactsForActionWidget(action_widgets, added_windows.get(win),
					added_windows, in.getGui());
			if (!"".equals(factAW.getContent())) {
				facts.add(factAW);
			}

			// TO DO: add selectable widgets to alloy
		}

		final Alloy_Model specific_model = new Alloy_Model(signatures, facts, predicates,
				functions, opens);
		return instantiate(specific_model);
	}

	/**
	 * Method that links Windows signatures with Input Widget Signatures
	 * according to the structure of the GUI
	 *
	 * @param iws
	 * @param window
	 */
	static private Fact createFactsForInputWidget(final Map<Input_widget, Signature> iws,
			final Signature window) {

		return createFactsForElement(iws.values(), window, "iws");
	}

	/**
	 * Method that links Windows signatures with Action Widget Signatures
	 * according to the structure of the GUI. it requires also the gui and the
	 * list of added signatures to add fact about the edges
	 *
	 * @param aws
	 * @param window
	 * @throws Exception
	 */
	private static Fact createFactsForActionWidget(final Map<Action_widget, Signature> aws,
			final Signature window, final Map<Window, Signature> ws, final GUI gui)
			throws Exception {

		final Fact initial_fact = createFactsForElement(aws.values(), window, "aws");
		String content = initial_fact.getContent();

		for (final Action_widget aw : aws.keySet()) {
			final List<Window> edges = new ArrayList<>();
			for (final Window w : gui.getStaticForwardLinks(aw)) {
				if (ws.containsKey(w)) {
					edges.add(w);
				}
			}
			if (edges.size() > 0) {
				content += System.getProperty("line.separator") + aws.get(aw).getIdentifier()
						+ ".goes = ";
				int i = 0;
				for (final Window edge : edges) {
					content += ws.get(edge).getIdentifier();
					content += (i + 1 == edges.size()) ? "" : " + ";
					i++;
				}
			}
		}

		final Fact fact = new Fact(window.getIdentifier() + "_aws", content);
		return fact;
	}

	/**
	 * Method that links Windows signatures with Selectable Widget Signatures
	 * according to the structure of the GUI
	 *
	 * @param sws
	 * @param window
	 */
	private static Fact createFactsForSelectableWidget(final List<Signature> sws,
			final Signature window) {

		return createFactsForElement(sws, window, "aws");
	}

	private static Fact createFactsForElement(final Collection<Signature> widgets,
			final Signature window, final String fieldToRelated) {

		if (widgets.isEmpty()) {
			return new Fact(window.getIdentifier() + "_" + fieldToRelated, "");
		}

		String content = window.getIdentifier() + "." + fieldToRelated + " = ";
		int i = 0;
		for (final Signature widget : widgets) {
			content += widget.getIdentifier();
			content += (i + 1 == widgets.size()) ? "" : " + ";
			i++;
		}

		return new Fact(window.getIdentifier() + "_" + fieldToRelated, content);
	}
}
