package src.usi.pattern;

public enum Patterns {
	SAVE("SAVE.xml"), AUTH("AUTH.xml"), CRUD("CRUD.xml"), ;

	public String name;

	private Patterns(final String name) {

		this.name = name;
	}
}
