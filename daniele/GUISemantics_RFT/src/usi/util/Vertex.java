package src.usi.util;

public class Vertex {

	final private String id;
	final private String name;

	public Vertex(final String id, final String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {

		return this.id;
	}

	public String getName() {

		return this.name;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (obj.getClass() == String.class.getClass()) {
			return this.id.equals(obj);
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Vertex other = (Vertex) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {

		return this.name;
	}

}