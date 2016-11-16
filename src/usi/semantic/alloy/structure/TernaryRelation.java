package src.usi.semantic.alloy.structure;

import src.usi.pattern.structure.Cardinality;

public class TernaryRelation {
	private final String name;
	private final Signature domain;
	private final Signature middle_domain;
	private final Signature co_domain;
	private final Cardinality co_domain_multiplicity;
	private final Cardinality middle_domain_multiplicity;

	public TernaryRelation(final String name, final Signature domain, final Signature middle_domain,
			final Signature co_domain, final Cardinality middle_domain_multiplicity,
			final Cardinality co_domain_multiplicity) throws Exception {
		if (name == null || name.length() == 0 || domain == null || co_domain == null || middle_domain == null
				|| co_domain_multiplicity == null || middle_domain_multiplicity == null
				|| co_domain_multiplicity == Cardinality.NONE || middle_domain_multiplicity == Cardinality.NONE) {
			throw new Exception("TernaryRelation: error");
		}
		this.name = name;
		this.domain = domain;
		this.middle_domain = middle_domain;
		this.co_domain = co_domain;
		this.co_domain_multiplicity = co_domain_multiplicity;
		this.middle_domain_multiplicity = middle_domain_multiplicity;
	}

	public String getName() {
		return this.name;
	}

	public Signature getDomain() {
		return this.domain;
	}

	public Signature getMiddle_domain() {
		return this.middle_domain;
	}

	public Signature getCo_domain() {
		return this.co_domain;
	}

	public Cardinality getCo_domain_multiplicity() {
		return this.co_domain_multiplicity;
	}

	public Cardinality getMiddle_domain_multiplicity() {
		return this.middle_domain_multiplicity;
	}

	@Override
	public String toString() {
		String s = "	" + this.name + ": ";

		s += this.middle_domain.getIdentifier() + " ";

		if (this.middle_domain_multiplicity != Cardinality.SET) {
			s += this.middle_domain_multiplicity.name().toLowerCase() + " ";
		}
		s += "-> ";
		if (this.co_domain_multiplicity != Cardinality.SET) {
			s += this.co_domain_multiplicity.name().toLowerCase() + " ";
		}
		s += this.co_domain.getIdentifier();

		return s;
	}
}
