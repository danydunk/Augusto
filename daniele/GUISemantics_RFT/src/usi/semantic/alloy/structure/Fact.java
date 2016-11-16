package src.usi.semantic.alloy.structure;

public class Fact extends AlloyEntity {

	// we keep only the string with the fact
	private String content;

	public Fact(final String name, final String content) {
		super(name);
		if (content != null) {
			this.content = content;
		} else {
			this.content = "";
		}
	}

	@Override
	public String toString() {

		String s = "fact " + this.identifier + "{";
		if ("".equals(this.content)) {
			s += " }";
		} else {
			s += System.getProperty("line.separator");
			final String[] lines = this.content.split(System.getProperty("line.separator"));
			for (final String line : lines) {
				s += "	" + line.trim() + System.getProperty("line.separator");
			}
			s += System.getProperty("line.separator") + "}";
		}
		return s;
	}

	public String getContent() {

		return this.content;
	}
}
