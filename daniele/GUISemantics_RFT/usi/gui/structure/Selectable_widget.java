package usi.gui.structure;

import java.util.ArrayList;
import java.util.List;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.vp.ITestDataElement;
import com.rational.test.ft.vp.ITestDataElementList;
import com.rational.test.ft.vp.impl.TestDataList;
import com.rational.test.ft.vp.impl.TestDataTable;

public class Selectable_widget extends Widget {

	private final int size;
	private final int selected;

	public Selectable_widget(final TestObject to, final String id, final String label,
			final String classs, final int x, final int y, final int size, final int selected)
			throws Exception {

		super(to, id, label, classs, x, y);
		if (size < 0 || selected < -1 || selected > size - 1) {
			throw new Exception("Selectable_widget: wrong size or selected.");
		}
		this.size = size;
		this.selected = selected;
	}

	public Selectable_widget(final String id, final String label, final String classs, final int x,
			final int y, final int size, final int selected) throws Exception {

		super(id, label, classs, x, y);
		if (size < 0 || selected < -1 || selected > size - 1) {
			throw new Exception("Selectable_widget: wrong size or selected.");
		}
		this.size = size;
		this.selected = selected;
	}

	public int getSize() {

		return this.size;
	}

	public int getSelected() {

		return this.selected;
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!(w instanceof Selectable_widget)) {
			return false;
		}

		// size and selected is not checked cause it can change
		return this.sameProperties_strong(w);
	}

	@Override
	public boolean isSimilar(final Widget w) {

		if (!(w instanceof Selectable_widget)) {
			return false;
		}

		// size and selected is not checked cause it can change
		return this.sameProperties_weak(w);
	}

	public static List<String> getElements(final TestObject to) throws Exception {

		final List<String> out = new ArrayList<>();
		switch (to.getProperty("uIClassID").toString()) {
		case "TableUI":
			final TestDataTable list = (TestDataTable) to.getTestData("visible contents");
			final int rows = list.getRowCount();
			for (int c = 0; c < rows; c++) {
				final String element = list.getCell(c, 0).toString();
				out.add(element);
			}
			break;
		case "ListUI":
			final TestDataList list2 = (TestDataList) to.getTestData("list");
			final ITestDataElementList el_list = list2.getElements();
			for (int c = 0; c < el_list.getLength(); c++) {
				final ITestDataElement element = el_list.getElement(c);
				out.add(element.getElement().toString());
			}
			break;
		default:
			throw new Exception("Selectable_widget - getElements: error.");
		}
		return out;
	}
}
