package src.usi.gui.structure;

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
			final String classs, final int x, final int y, final int width, final int height,
			final int size, final int selected) throws Exception {

		super(to, id, label, classs, x, y, width, height);
		if (size < 0 || selected < -1) {
			throw new Exception("Selectable_widget: wrong size or selected.");
		}
		if (selected > size - 1) {
			// it can happen (it is kinda of a bug)
			this.selected = -1;
		} else {
			this.selected = selected;
		}
		this.size = size;
	}

	public Selectable_widget(final String id, final String label, final String classs, final int x,
			final int y, final int width, final int height, final int size, final int selected)
			throws Exception {

		super(id, label, classs, x, y, width, height);
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

		if (!super.isSame(w)) {
			return false;
		}
		if (!(w instanceof Selectable_widget)) {
			return false;
		}

		// size and selected is not checked cause it can change
		return true;
	}

	// @Override
	// public boolean isSimilar(final Widget w) {
	//
	// if (!(w instanceof Selectable_widget)) {
	// return false;
	// }
	//
	// // size and selected is not checked cause it can change
	// return this.sameProperties_weak(w);
	// }

	public static List<String> getElements(final TestObject to) throws Exception {

		final List<String> out = new ArrayList<>();
		switch (to.getProperty("uIClassID").toString()) {
		case "TableUI":
			// we consider only the rows
			final TestDataTable list = (TestDataTable) to.getTestData("visible contents");
			final int rows = list.getRowCount();

			final int columns = list.getColumnCount();

			for (int c = 0; c < rows; c++) {
				String element = "";

				for (int cc = 0; cc < columns; cc++) {
					element += list.getCell(c, cc).toString();
				}
				out.add(element);

			}
			break;
		case "ListUI":
			final TestDataList list2 = (TestDataList) to.getTestData("list");
			final ITestDataElementList el_list = list2.getElements();
			for (int c = 0; c < el_list.getLength(); c++) {
				final ITestDataElement element = el_list.getElement(c);
				if (element == null || element.getElement() == null) {
					out.add("null");
				} else {
					out.add(element.getElement().toString());
				}
			}
			break;
		default:
			throw new Exception("Selectable_widget - getElements: error.");
		}
		return out;
	}
}
