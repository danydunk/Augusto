package usi.pattern.structure;

public enum Cardinality {
	ONE(1, 1), LONE(0, 1), SOME(1, Integer.MAX_VALUE), SET(0, Integer.MAX_VALUE), NONE(0, 0);

	private final int min;
	private final int max;

	private Cardinality(final int min, final int max) {
		this.min = min;
		this.max = max;
	}

	public int getMin() {

		return this.min;
	}

	public int getMax() {

		return this.max;
	}
}
