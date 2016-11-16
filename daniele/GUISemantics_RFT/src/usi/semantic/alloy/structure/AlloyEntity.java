package src.usi.semantic.alloy.structure;

public abstract class AlloyEntity {

	// name of the element
	protected final String identifier;

	public AlloyEntity(final String identifier) {

		if (identifier != null) {
			if (identifier.startsWith("this/")) {
				this.identifier = identifier.replace("this/", "");
			} else {
				this.identifier = identifier;
			}
		} else {
			this.identifier = "";
		}
	}

	public String getIdentifier() {

		return this.identifier;
	}

	// public void setIdentifier(String identifier) {
	// this.identifier = identifier;
	// }

	@Override
	public abstract String toString();
}
