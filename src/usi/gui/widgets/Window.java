package usi.gui.widgets;

import java.util.List;

import com.rational.test.ft.object.interfaces.TestObject;

public class Window extends Widget {

	private final List<Widget> contained;

	public Window(TestObject to, List<Widget> contained) {

		super(to);
		this.contained = contained;
	}
}
