package src.usi.semantic.alloy.structure;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * fun f[x1: e1, ..., xn: en] : e { E }
 */
public class Function extends AlloyEntity {

	// We could replace the result Expression by a Unary expression (Cardianily
	// + Signarute)
	protected final String result;
	protected final String content;
	protected final LinkedHashMap<Signature, List<String>> inputs;

	public Function(final String name, final String content, final String resultExpression,
			final LinkedHashMap<Signature, List<String>> inputs) {
		super(name);
		if (content != null) {
			this.content = content.trim();
		} else {
			this.content = "";
		}
		if (inputs != null) {
			this.inputs = inputs;
		} else {
			this.inputs = new LinkedHashMap<>();
		}

		this.result = resultExpression;
	}

	@Override
	public String toString() {

		final String separator = System.getProperty("line.separator");

		String s = "fun " + this.identifier + " [";
		final Iterator<Signature> it = this.inputs.keySet().iterator();
		while (it.hasNext()) {
			final Signature sig = it.next();
			final List<String> variables = this.inputs.get(sig);

			final Iterator<String> it2 = variables.iterator();
			while (it2.hasNext()) {
				final String var = it2.next();
				s = s + var;
				if (it2.hasNext()) {
					s = s + ", ";
				}
			}
			s = s + ": " + sig.getIdentifier();
			if (it.hasNext()) {
				s = s + ", ";
			}
		}
		s = s + "] "
		// Here the return expression
				+ " : " + this.result + "{" + separator;

		final String[] lines = this.content.split(separator);
		for (String line : lines) {
			line = line.trim();
			s += "	" + line + separator;
		}
		s += "}";
		return s;
	}

	public LinkedHashMap<Signature, List<String>> getInputs() {

		return new LinkedHashMap<>(this.inputs);
	}

	public String getContent() {

		return this.content;
	}

	public String getResult() {

		return this.result;
	}
}
