package usi.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.rational.test.ft.object.interfaces.TestObject;

public class Window {

	private final TestObject to;
	private final String title;
	private final boolean modal;
	private final int x;
	private final int y;
	private final List<Widget> contained;
	private final String id;

	public Window(TestObject to, List<Widget> contained, String id) throws Exception {

		if (to == null || contained == null || id == null) {
			throw new Exception("Window: error in constructor.");
		}
		this.title = to.getProperty("title").toString();
		this.x = Integer.valueOf(to.getProperty("x").toString());
		this.y = Integer.valueOf(to.getProperty("y").toString());
		this.to = to;
		this.contained = contained;
		this.modal = Boolean.valueOf(to.getProperty("modal").toString());
		this.id = id;
	}

	public TestObject getView() {

		return this.to;
	}

	public String getId() {

		return this.id;
	}

	public List<Widget> getContained() {

		return new ArrayList<Widget>(this.contained);
	}

	public String getTitle() {

		return this.title;
	}

	public boolean isSame(Window w) throws Exception {

		if (this == w) {
			return true;
		}

		if (w.getView() != null && this.to == w.getView()) {
			return true;
		}

		if (!this.title.equals(w.getTitle())) {
			return false;
		}

		if (this.x != w.getX() || this.y != w.getY()) {
			return false;
		}

		if (this.contained.size() != w.getContained().size()) {
			return false;
		}

		for (int cont = 0; cont < this.contained.size(); cont++) {
			Widget wi = this.contained.get(cont);
			try {
				if (!wi.isSame(w.getContained().get(cont))) {
					return false;
				}
			} catch (Exception e) {
				throw new Exception("Window - isSame: error, " + e.getMessage());
			}
		}

		return true;
	}

	public int getX() {

		return this.x;
	}

	public int getY() {

		return this.y;
	}

	public boolean isModal() {

		return this.modal;
	}
}
