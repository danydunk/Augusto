package src.usi.pattern;

public enum Patterns {
	CRUD("CRUD.xml"), AUTH("AUTH.xml");

	public String name;

	private Patterns(final String name) {

		this.name = name;
	}
}
