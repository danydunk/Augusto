package src.usi.pattern;

public enum Patterns {
	CRUD("CRUD_NO_READ.xml"), AUTH("AUTH.xml"), SAVE("SAVE.xml");

	public String name;

	private Patterns(final String name) {

		this.name = name;
	}
}
