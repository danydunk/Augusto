package usi.gui;

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
	public ContextAnalyzer(List<TestObject> tos) throws Exception {

		this.descriptors_classes = new ArrayList<String>();
		this.descriptors_classes.add("LabelUI");

		this.descriptorInContainer = new HashMap<TestObject, List<Descriptor>>();
		this.containedInContainer = new HashMap<TestObject, List<TestObject>>();
		this.fatherMap = new HashMap<TestObject, TestObject>();
		this.containerDescription = new HashMap<TestObject, String>();

		for (TestObject to : tos) {
			String classs = to.getProperty("uiClassId").toString();
			if (classs == null) {
				throw new Exception("ContextAnalyzer: class id not found.");
			}
			// the father is retrieved
			TestObject father = to.getParent();
			if (father != null && !tos.contains(father)) {
				throw new Exception("ContextAnalyzer: to father not found.");
			}
			this.fatherMap.put(to, father);
			// if it is a descriptor
			if (this.descriptors_classes.contains(classs)) {
				int x = Integer.valueOf(to.getProperty("x").toString());
				int y = Integer.valueOf(to.getProperty("y").toString());
				int width = Integer.valueOf(to.getProperty("width").toString());
				int height = Integer.valueOf(to.getProperty("height").toString());
				String label = to.getProperty("text").toString();
				// we filter the empty strings
				if (label.trim().length() == 0) {
					continue;
				}
				Descriptor d = new Descriptor(label, x, y, height, width);

				if (this.descriptorInContainer.containsKey(father)) {
					this.descriptorInContainer.get(father).add(d);
				} else {
					List<Descriptor> list = new ArrayList<Descriptor>();
					list.add(d);
					this.descriptorInContainer.put(father, list);
				}
			} else {
				if (this.containedInContainer.containsKey(father)) {
					this.containedInContainer.get(father).add(to);
				} else {
					List<TestObject> contained = new ArrayList<TestObject>();
					contained.add(to);
					this.containedInContainer.put(father, contained);
				}
			}
		}

	}

	public String getDescriptor(TestObject to) throws Exception {

		int x = Integer.valueOf(to.getProperty("x").toString());
		int y = Integer.valueOf(to.getProperty("y").toString());
		int width = Integer.valueOf(to.getProperty("width").toString());
		int height = Integer.valueOf(to.getProperty("height").toString());
		Area area = new Area(x, y, height, width);

		if (!this.fatherMap.containsKey(to)) {
			throw new Exception("ContextAnalyzer - getContainerDescriptor: father not found.");
		}

		TestObject father = this.fatherMap.get(to);
		List<Descriptor> descriptors = this.descriptorInContainer.get(father);

		double min_dist = Double.MAX_VALUE;
		Descriptor nearer = null;
		for (final Descriptor desc : descriptors) {
			final double dist = area.getDistance(desc.a);
			if (dist < min_dist) {
				min_dist = dist;
				nearer = desc;
			}
		}
		if (nearer != null) {
			return nearer.label;
		}
		// if no descriptor was found
		return this.getContainerDescriptor(father);
	}

	public String getContainerDescriptor(TestObject to) throws Exception {

		if (to == null) {
			return null;
		}

		if (this.containerDescription.get(to) != null) {
			return this.containerDescription.get(to);
		}

		// if it has a title we return it
		String title = to.getProperty("title").toString();
		if (title != null && title.trim().length() > 0) {
			return title;
		}
		// else we look for descriptors
		int x = Integer.valueOf(to.getProperty("x").toString());
		int y = Integer.valueOf(to.getProperty("y").toString());
		int width = Integer.valueOf(to.getProperty("width").toString());
		int height = Integer.valueOf(to.getProperty("height").toString());
		Area area = new Area(x, y, height, width);

		if (!this.fatherMap.containsKey(to)) {
			throw new Exception("ContextAnalyzer - getContainerDescriptor: father not found.");
		}

		TestObject father = this.fatherMap.get(to);
		List<Descriptor> descriptors = this.descriptorInContainer.get(father);

		double min_dist = Double.MAX_VALUE;
		Descriptor nearer = null;
		for (final Descriptor desc : descriptors) {
			final double dist = area.getDistance(desc.a);
			if (dist < min_dist) {
				min_dist = dist;
				nearer = desc;
			}
		}
		if (nearer != null) {
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
				distance = (int) (Math.pow(((a.x + a.width) - this.x), 2) + Math.pow((a.y - this.y), 2));
			} else {
				distance = (int) (Math.pow((a.x - this.x), 2) + Math.pow(((a.y + a.height) - this.y), 2));
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
			if (a.x <= this.centerX && a.y <= this.centerY) {
				return true;
			}
			return false;
		}
	}

	class Descriptor {

		public final String label;
		public final Area a;

		public Descriptor(final String label, final int x, final int y, final int height, final int width) {

			this.label = label;
			this.a = new Area(x, y, height, width);
		}

	}
}
