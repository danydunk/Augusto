package src.usi.semantic.alloy.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import src.usi.pattern.structure.Cardinality;

public class Signature extends AlloyEntity {

	/**
	 * Cardinality of the signature declaration: ONE, SOME, etc
	 */
	private final boolean subset;
	private final Cardinality cardinality;
	private final List<Signature> parents;
	private final boolean abstract_;
	private final LinkedHashMap<String, BinaryRelation> binary_relations;
	private final LinkedHashMap<String, TernaryRelation> ternary_relations;

	public Signature(final String identifier, final Cardinality cardinality, final boolean abstract_,
			final List<Signature> parents, final boolean subset) throws Exception {
		super(identifier);
		if (((parents == null || parents.size() == 0) && subset)
				|| (parents != null && parents.size() > 1 && !subset)) {
			throw new Exception("Signature: error in constructor.");
		}
		this.parents = new ArrayList<>();
		this.subset = subset;
		this.cardinality = cardinality;
		this.abstract_ = abstract_;
		if (parents != null) {
			this.parents.addAll(parents);
		}
		this.binary_relations = new LinkedHashMap<>();
		this.ternary_relations = new LinkedHashMap<>();
	}

	public void addBinaryRelation(final String name, final Signature co_domain, final Cardinality multiplicity)
			throws Exception {

		if (name == null || name.length() == 0 || co_domain == null || multiplicity == null) {
			throw new Exception("Signature - addBinaryRelation: error in inputs.");
		}
		if (this.ternary_relations.containsKey(name) || this.binary_relations.containsKey(name)) {
			throw new Exception("Signature - addBinaryRelation: binary relation with the same name already existing.");
		}
		final BinaryRelation br = new BinaryRelation(name, this, co_domain, multiplicity);
		this.binary_relations.put(name, br);
	}

	public void addTernaryRelation(final String name, final Signature co_domain, final Signature middle_domain,
			final Cardinality middle_domain_multiplicity, final Cardinality co_domain_multiplicity) throws Exception {

		if (this.ternary_relations.containsKey(name) || this.binary_relations.containsKey(name)) {
			throw new Exception("Signature - addTernaryRelation: binary relation with the same name already existing.");
		}
		final TernaryRelation tr = new TernaryRelation(name, this, middle_domain, co_domain, middle_domain_multiplicity,
				co_domain_multiplicity);
		this.ternary_relations.put(name, tr);
	}

	public void removeRelation(final String name) throws Exception {

		if (this.ternary_relations.containsKey(name)) {
			this.ternary_relations.remove(name);
		} else if (this.binary_relations.containsKey(name)) {
			this.binary_relations.remove(name);
		} else {
			throw new Exception("Signature - removeRelation: relation not found.");
		}
	}

	@Override
	public String toString() {

		String s = (this.abstract_) ? "abstract " : "";
		s += (this.cardinality == null || this.cardinality == Cardinality.SET) ? ""
				: (this.cardinality.name().toLowerCase() + " ");
		s += "sig " + this.getIdentifier() + " ";
		if (this.subset) {
			s += "in ";
			for (int x = 0; x < this.parents.size(); x++) {
				s += this.parents.get(x).getIdentifier();
				if (x != this.parents.size() - 1) {
					s += "+";
				}
			}
			s += " ";
		} else {
			s += (this.parents == null || this.parents.size() == 0) ? ""
					: "extends " + this.parents.get(0).getIdentifier() + " ";

		}
		if (this.binary_relations.size() == 0 && this.ternary_relations.size() == 0) {
			s += "{ }";
			return s;
		}

		s += "{" + System.getProperty("line.separator");
		Iterator<String> it = this.binary_relations.keySet().iterator();
		while (it.hasNext()) {
			final String key = it.next();
			final BinaryRelation rel = this.binary_relations.get(key);
			s += rel.toString() + "," + System.getProperty("line.separator");
		}

		it = this.ternary_relations.keySet().iterator();
		while (it.hasNext()) {
			final String key = it.next();
			final TernaryRelation rel = this.ternary_relations.get(key);
			s += rel.toString() + "," + System.getProperty("line.separator");
		}
		s += "}";
		return s;
	}

	public Cardinality getCardinality() {

		return this.cardinality;
	}

	public List<Signature> getParent() {

		return this.parents;
	}

	public boolean isAbstract_() {

		return this.abstract_;
	}

	public Map<String, BinaryRelation> getBinary_relations() {

		return new LinkedHashMap<>(this.binary_relations);
	}

	public Map<String, TernaryRelation> getTernary_relations() {

		return new LinkedHashMap<>(this.ternary_relations);
	}

	public boolean isSubset() {

		return this.subset;
	}

	public boolean hasParent(final Signature p) {

		if (this.getParent().contains(p)) {
			return true;
		}

		boolean hasParent = false;
		for (final Signature parent : this.parents) {
			if (parent != null) {
				hasParent = hasParent || parent.hasParent(p);
			}
		}
		return hasParent;
	}
}
