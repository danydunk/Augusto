package usi.gui.semantic.alloy.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Semantic_predicate extends Predicate {

	// the name of the predicate must be <operation predicate name>_semantics

	// Map that contains the semantic cases
	// the key is the precondition and the value are the semantic post
	// conditions
	protected final Map<String, List<String>> cases;

	public Semantic_predicate(final String name, final String content,
			final LinkedHashMap<Signature, List<String>> inputs) throws Exception {

		super(name, content, inputs);
		this.cases = new HashMap<String, List<String>>();
		this.detetcCases();
	}

	private void detetcCases() throws Exception {

		if (this.identifier.equals("click_semantics")) {
			this.content = this.content.replace("(aw ", "(Track.op.(T/next[t]).clicked ");
			this.content = this.content.replace(" aw ", " Track.op.(T/next[t]).clicked ");
			this.content = this.content.replace(" aw)", " Track.op.(T/next[t]).clicked)");
			this.content = this.content.replace("#aw ", "#Track.op.(T/next[t]).clicked ");
			this.content = this.content.replace("#aw)", "#Track.op.(T/next[t]).clicked)");
			this.content = this.content.replace("(aw.", "(Track.op.(T/next[t]).clicked.");
			this.content = this.content.replace(" aw.", " Track.op.(T/next[t]).clicked.");
			this.content = this.content.replace("aw.", "Track.op.(T/next[t]).clicked.");
			this.content = this.content.replace(".aw)", ".Track.op.(T/next[t]).clicked)");
			this.content = this.content.replace(".aw ", ".Track.op.(T/next[t]).clicked ");

		}
		if (this.identifier.equals("fill_semantics")) {
			this.content = this.content.replace("(iw ", "(Track.op.(T/next[t]).filled ");
			this.content = this.content.replace(" iw ", " Track.op.(T/next[t]).filled ");
			this.content = this.content.replace(" iw)", " Track.op.(T/next[t]).filled)");
			this.content = this.content.replace("#iw ", "#Track.op.(T/next[t]).filled ");
			this.content = this.content.replace("#iw)", "#Track.op.(T/next[t]).filled)");
			this.content = this.content.replace("(iw.", "(Track.op.(T/next[t]).filled.");
			this.content = this.content.replace(" iw.", " Track.op.(T/next[t]).filled.");
			this.content = this.content.replace("iw.", "Track.op.(T/next[t]).filled.");
			this.content = this.content.replace(".iw)", ".Track.op.(T/next[t]).filled)");
			this.content = this.content.replace(".iw ", ".Track.op.(T/next[t]).filled ");
			this.content = this.content.replace("(v ", "(Track.op.(T/next[t]).with ");
			this.content = this.content.replace(" v ", " Track.op.(T/next[t]).with ");
			this.content = this.content.replace(" v)", " Track.op.(T/next[t]).with)");
			this.content = this.content.replace("#v ", "#Track.op.(T/next[t]).with ");
			this.content = this.content.replace("#v)", "#Track.op.(T/next[t]).with)");
			this.content = this.content.replace("(v.", "(Track.op.(T/next[t]).with.");
			this.content = this.content.replace(" v.", " Track.op.(T/next[t]).with.");
			this.content = this.content.replace("v.", "Track.op.(T/next[t]).with.");
			this.content = this.content.replace(".v)", ".Track.op.(T/next[t]).with)");
			this.content = this.content.replace(".v ", ".Track.op.(T/next[t]).with ");
		}
		if (this.identifier.equals("select_semantics")) {
			this.content = this.content.replace("(sw ", "(Track.op.(T/next[t]).wid ");
			this.content = this.content.replace(" sw ", " Track.op.(T/next[t]).wid ");
			this.content = this.content.replace(" sw)", " Track.op.(T/next[t]).wid)");
			this.content = this.content.replace("#sw ", "#Track.op.(T/next[t]).wid ");
			this.content = this.content.replace("#sw)", "#Track.op.(T/next[t]).wid)");
			this.content = this.content.replace("(sw.", "(Track.op.(T/next[t]).wid.");
			this.content = this.content.replace(" sw.", " Track.op.(T/next[t]).wid.");
			this.content = this.content.replace("sw.", "Track.op.(T/next[t]).wid.");
			this.content = this.content.replace(".sw)", ".Track.op.(T/next[t]).wid)");
			this.content = this.content.replace(".sw ", ".Track.op.(T/next[t]).wid ");
			this.content = this.content.replace("(o ", "(Track.op.(T/next[t]).selected_o ");
			this.content = this.content.replace(" o ", " Track.op.(T/next[t]).selected_o ");
			this.content = this.content.replace(" o)", " Track.op.(T/next[t]).selected_o)");
			this.content = this.content.replace("#o ", "#Track.op.(T/next[t]).selected_o ");
			this.content = this.content.replace("#o)", "#Track.op.(T/next[t]).selected_o)");
			this.content = this.content.replace("(o.", "(Track.op.(T/next[t]).selected_o.");
			this.content = this.content.replace(" o.", " Track.op.(T/next[t]).selected_o.");
			this.content = this.content.replace("o.", "Track.op.(T/next[t]).selected_o.");
			this.content = this.content.replace(".o)", ".Track.op.(T/next[t]).selected_o)");
			this.content = this.content.replace(".o ", ".Track.op.(T/next[t]).selected_o ");
		}

		final String separator = System.getProperty("line.separator");
		final String[] lines = this.content.split(separator);
		if (lines.length == 1 && "true".equals(lines[0].trim())) {
			return;
		}

		for (String line : lines) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			final int index = line.lastIndexOf("=>");
			final String precondition = line.substring(0, index).trim();
			String postcondition = line.substring(index + 2, line.length()).trim();
			if (postcondition.startsWith("(") && postcondition.endsWith(")")) {
				postcondition = postcondition.substring(1, postcondition.length() - 1);
			}

			final List<String> preds = new ArrayList<>();
			while (true) {
				final int index_and = postcondition.indexOf(" and ");
				final int index_or = postcondition.indexOf(" or ");
				if (index_and == -1 && index_or == -1) {
					preds.add(postcondition);
					break;
				}
				int ind = -1;
				if (index_and == -1) {
					ind = index_or;
				} else if (index_or == -1) {
					ind = index_and;
				} else {
					ind = Math.min(index_and, index_or);
				}
				final String pred = postcondition.substring(0, ind).trim();
				preds.add(pred);
				postcondition = postcondition.substring(ind + 4).trim();
			}
			if (preds.size() == 0) {
				throw new Exception("Semantic_predicate - detetcCases: error");
			}
			this.cases.put(precondition, preds);
		}
	}

	public Map<String, List<String>> getCases() {

		return new HashMap<>(this.cases);
	}
}
