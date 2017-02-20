package src.usi.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rational.test.ft.object.interfaces.TestObject;

/**
 * Class used to associate a label to a input widget
 *
 * @author daniele
 *
 */
public class ContextAnalyzer {

	private final List<String> descriptors_classes;
	// Map that for each container keeps its children
	private final Map<TestObject, List<TestObject>> containedInContainer;
	private final Map<TestObject, List<Descriptor>> descriptorInContainer;
	// used to detect the hights of tables in a stable way
	private final List<TestObject> tableheaders;

	// inverse map
	private final Map<TestObject, TestObject> fatherMap;
	// used in if there is no descriptor for a widget
	private final Map<TestObject, String> containerDescription;

	/**
	 * The constructor takes in input the container xml node of a window in the
	 * GUI xml file created by GUI Ripping
	 *
	 * @param windowNode
	 * @throws Exception
	 */
	public ContextAnalyzer(final List<TestObject> tos) throws Exception {

		this.tableheaders = new ArrayList<>();

		this.descriptors_classes = new ArrayList<String>();
		this.descriptors_classes.add("LabelUI");
		this.descriptors_classes.add("CheckBoxUI");

		this.descriptorInContainer = new HashMap<TestObject, List<Descriptor>>();
		this.containedInContainer = new HashMap<TestObject, List<TestObject>>();
		this.fatherMap = new HashMap<TestObject, TestObject>();
		this.containerDescription = new HashMap<TestObject, String>();

		for (final TestObject to : tos) {
			final boolean showing = Boolean.valueOf(to.getProperty("showing").toString());

			final String classs = to.getProperty("uIClassID").toString();

			if (classs == null) {
				throw new Exception("ContextAnalyzer: class id not found.");
			}
			// the father is retrieved
			final TestObject father = to.getMappableParent();

			this.fatherMap.put(to, father);
			if (!this.descriptorInContainer.containsKey(father)) {
				final List<Descriptor> list = new ArrayList<Descriptor>();
				this.descriptorInContainer.put(father, list);
			}
			if (!this.containedInContainer.containsKey(father)) {
				final List<TestObject> list = new ArrayList<>();
				this.containedInContainer.put(father, list);
			}

			if (classs.equals("TableHeaderUI")) {
				this.tableheaders.add(to);
			}

			// if it is a descriptor
			if (this.descriptors_classes.contains(classs)) {
				if (!showing) {
					continue;
				}
				Object text = null;
				try {
					text = to.getProperty("text");
				} catch (final Exception e) {}
				final Point p = (Point) to.getProperty("locationOnScreen");
				final int x = p.x;
				final int y = p.y;
				final int width = Integer.valueOf(to.getProperty("width").toString());
				final int height = Integer.valueOf(to.getProperty("height").toString());

				// final Object text = to.getProperty("text");
				String label = "";
				if (text != null) {
					label = text.toString();
				}
				// we filter the empty strings
				if (label.trim().length() == 0) {
					continue;
				}
				final Descriptor d = new Descriptor(label, x, y, height, width);

				this.descriptorInContainer.get(father).add(d);
			} else {
				this.containedInContainer.get(father).add(to);
			}
		}

	}

	public String getDescriptor(final TestObject to) throws Exception {

		final String classs = to.getProperty("uIClassID").toString();
		final Point p = (Point) to.getProperty("locationOnScreen");
		final int x = p.x;
		final int y = p.y;
		final int width = Integer.valueOf(to.getProperty("width").toString());
		int height = Integer.valueOf(to.getProperty("height").toString());
		final Area area = new Area(x, y, height, width);

		// we deal with the height of tables
		Area oo = null;
		double mind = Double.MAX_VALUE;
		if (classs.equals("TableUI")) {
			loop: for (final TestObject head : this.tableheaders) {
				Point pp = null;
				try {
					pp = (Point) head.getProperty("locationOnScreen");
				} catch (final Exception e) {
					continue loop;
				}
				final int xx = pp.x;
				final int yy = pp.y;
				final int ww = Integer.valueOf(head.getProperty("width").toString());
				final int hh = Integer.valueOf(head.getProperty("height").toString());
				final Area aa = new Area(xx, yy, hh, ww);
				final double dist = area.getDistance(aa);

				if (dist < mind) {
					mind = dist;
					oo = aa;
				}
			}
		height = oo.height;
		}

		if (!this.fatherMap.containsKey(to)) {
			throw new Exception("ContextAnalyzer - getContainerDescriptor: father not found.");
		}

		final TestObject father = this.fatherMap.get(to);
		// if (height == 0) {
		// height = 15;
		// }
		final List<Descriptor> descriptors = this.descriptorInContainer.get(father);

		double min_dist = Double.MAX_VALUE;
		Descriptor nearer = null;
		for (final Descriptor desc : descriptors) {
			if (!area.isRelated(desc.a)) {
				continue;
			}
			final double dist = area.getDistance(desc.a);

			if (dist < min_dist) {
				min_dist = dist;
				nearer = desc;
			}
		}
		if (nearer != null && Math.sqrt(min_dist) < (height * 5)) {
			// we can do it cause the descriptors are calculated in order
			if (!classs.equals("RadioButtonUI")) {
				this.descriptorInContainer.get(father).remove(nearer);
			}
			return nearer.label;

		}
		// if no descriptor was found
		return this.getContainerDescriptor(father);
	}

	public String getContainerDescriptor(final TestObject to) throws Exception {

		if (to == null) {
			return null;
		}
		try {
			to.getProperty("uIClassID").toString();
		} catch (final Exception e) {
			// it is a window
			return null;
		}

		if (this.containerDescription.get(to) != null) {
			return this.containerDescription.get(to);
		}

		// if it has a title we return it
		Object title = null;
		try {
			title = to.getProperty("title");
		} catch (final Exception e) {
			// TO does not have a title
		}

		if (title != null && title.toString().trim().length() > 0) {
			return title.toString();
		}
		// else we look for descriptors
		final Point p = (Point) to.getProperty("locationOnScreen");
		final int x = p.x;
		final int y = p.y;
		final int width = Integer.valueOf(to.getProperty("width").toString());
		final int height = Integer.valueOf(to.getProperty("height").toString());
		final Area area = new Area(x, y, height, width);

		if (!this.fatherMap.containsKey(to)) {
			throw new Exception("ContextAnalyzer - getContainerDescriptor: father not found.");
		}

		final TestObject father = this.fatherMap.get(to);
		final List<Descriptor> descriptors = this.descriptorInContainer.get(father);

		double min_dist = Double.MAX_VALUE;
		Descriptor nearer = null;
		for (final Descriptor desc : descriptors) {
			if (!area.isRelated(desc.a)) {
				continue;
			}
			final double dist = area.getDistance(desc.a);
			if (dist < min_dist) {
				min_dist = dist;
				nearer = desc;
			}
		}
		if (nearer != null && Math.sqrt(min_dist) < (height * 5)) {
			this.descriptorInContainer.get(father).remove(nearer);
			return nearer.label;
		}
		// if no descriptor was found
		return this.getContainerDescriptor(father);
	}

	class Area {

		public final int x;
		public final int y;
		public final int height;
		public final int width;
		public final int centerX;
		public final int centerY;

		public Area(final int x, final int y, final int height, final int width) {

			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
			this.centerX = x + (width / 2);
			this.centerY = y + (height / 2);
		}

		/**
		 * function that calculates the distance between this area and the input
		 * area. The distance is calculated from the topleft point of this area
		 * to the bottonleft or topright (depending on the position) of the
		 * input area
		 *
		 * @param area
		 * @return
		 */
		public double getDistance(final Area a) {

			final int distance;
			if ((a.y + a.height) > this.y) {

				distance = (int) (Math.pow(((a.x + a.width + 1) - this.x), 2) + Math.pow(
						(a.y - this.y), 2));
			} else
			/*
			 * if ((a.x + a.width) <= this.x) { distance = (int) (Math.pow(((a.x
			 * + a.width) - this.x), 2) + Math.pow( ((a.y + a.height) - this.y),
			 * 2)); } else
			 */{
				distance = (int) (Math.pow(((a.x + 1) - this.x), 2) + Math.pow(
						((a.y + a.height) - this.y), 2));
			}

			return distance;
		}

		/**
		 * function that returns true if the input area is on the top left of
		 * this area
		 *
		 * @param area
		 * @return
		 */
		public boolean isRelated(final Area a) {

			// controllo che il widget sia all'interno dell'area di interesse
			// del to in esame
			if (a.y - 2 <= this.centerX && a.y + a.height - 2 <= this.y + this.height) {
				return true;
			}
			return false;
		}
	}

	class Descriptor {

		public final String label;
		public final Area a;

		public Descriptor(final String label, final int x, final int y, final int height,
				final int width) {

			this.label = label;
			this.a = new Area(x, y, height, width);
		}

	}
}
