package src.usi.pattern;

public enum Patterns {
	SAVE("SAVE.xml"), CRUD("CRUD_NO_READ.xml"), AUTH("AUTH.xml");

	public String name;

	private Patterns(final String name) {

		this.name = name;
	}
}
