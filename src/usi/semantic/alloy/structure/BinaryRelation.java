package src.usi.semantic.alloy.structure;

import src.usi.pattern.structure.Cardinality;

public class BinaryRelation {

	private final String name;
	private final Signature domain;
	private final Signature co_domain;
	private final Cardinality multiplicity;

	public BinaryRelation(final String name, final Signature domain, final Signature co_domain,
			final Cardinality multiplicity) throws Exception {
		if (name == null || name.length() == 0 || domain == null || co_domain == null
				|| multiplicity == null | multiplicity == Cardinality.NONE) {
			throw new Exception("BinaryRelation: error");
		}
		this.name = name;
		this.domain = domain;
		this.co_domain = co_domain;
		this.multiplicity = multiplicity;
	}

	public String getName() {

		return this.name;
	}

	public Signature getDomain() {

		return this.domain;
	}

	public Signature getCo_domain() {

		return this.co_domain;
	}

	public Cardinality getMultiplicity() {

		return this.multiplicity;
	}

	@Override
	public String toString() {

		String s = "	" + this.name + ": ";
		if (this.multiplicity != Cardinality.ONE) {
			s += this.multiplicity.name().toLowerCase() + " ";
		}
		s += this.co_domain.getIdentifier();
		return s;
	}
}
