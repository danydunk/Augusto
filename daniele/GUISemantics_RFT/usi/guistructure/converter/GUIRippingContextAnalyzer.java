package usi.guistructure.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import usi.xml.XMLUtil;

/**
 * Class used to associate a label to a input widget
 *
 * @author daniele
 *
 */
public class GUIRippingContextAnalyzer {

	private final List<String> descriptors_classes;
	// Map that for each container id contains the descriptors in the container
	private final Map<String, List<Descriptor>> descriptorsInContainer;
	// Map that for each container id returns the descriptor associated to the
	// container
	// used in if there is no descriptor for a widget
	private final Map<String, String> containerDescription;

	private static boolean LOCAL_ANALYSIS = true;
	private static boolean USE_CONTAINER_TITLE_TEXT = true;

	/**
	 * The constructor takes in input the container xml node of a window in the
	 * GUI xml file created by GUI Ripping
	 *
	 * @param windowNode
	 * @throws Exception
	 */
	public GUIRippingContextAnalyzer(final Element node, final boolean local_analysis,
			final boolean use_container_title_text) throws Exception {
		this.descriptors_classes = new ArrayList<>();
		this.descriptors_classes.add("javax.swing.JLabel");
		LOCAL_ANALYSIS = local_analysis;
		USE_CONTAINER_TITLE_TEXT = use_container_title_text;
		this.descriptorsInContainer = new HashMap<>();
		this.containerDescription = new HashMap<>();

		final List<Element> children = XMLUtil.getChildrenElements(node);
		if (children.size() != 2 || !"Window".equals(children.get(0).getNodeName())
				|| !"Container".equals(children.get(1).getNodeName())) {
			throw new Exception("GUIRippingContextAnalyzer: wrong node in constructor");
		}
		this.traverseContainersRecursive(children.get(1), null);
	}

	public GUIRippingContextAnalyzer(final Element node) throws Exception {
		this(node, LOCAL_ANALYSIS, USE_CONTAINER_TITLE_TEXT);
	}

	/**
	 * Function that traverses the Container tags in rippedContainer recursively
	 *
	 * @param out
	 * @param rippedContainer
	 * @throws Exception
	 */
	private void traverseContainersRecursive(final Element container, final String father_id) throws Exception {

		final List<Element> children = XMLUtil.getChildrenElements(container);
		Element attributes = null;
		Element contents = null;
		// the nodes attributes and contents are extracted
		switch (children.size()) {
		case (1):
			if ("Contents".equals(children.get(0).getNodeName())) {
				contents = children.get(0);
			} else if ("Attributes".equals(children.get(0).getNodeName())) {
				// if it only has attributes it must be a widget
				final Descriptor desc = this.extractDescriptor(children.get(0));
				if (desc == null) {
					return;
				}

				this.descriptorsInContainer.get(father_id).add(desc);
				return;
			}
			break;
		case (2):
			if (!"Attributes".equals(children.get(0).getNodeName())
					|| !"Contents".equals(children.get(1).getNodeName())) {
				throw new Exception(
						"GUIRippingContextAnalyzer - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
			}
			attributes = children.get(0);
			contents = children.get(1);
			break;
		}

		String label = null;
		int x = -1;
		int y = -1;
		int height = -1;
		int width = -1;
		String id = null;
		// the attributes of the container are read
		if (attributes != null) {
			final List<Element> properties = XMLUtil.getChildrenElements(attributes);
			// iterates all the properties
			for (final Element p : properties) {
				final List<Element> p_children = XMLUtil.getChildrenElements(p);
				if (!"Property".equals(p.getNodeName()) || p_children.size() != 2) {
					throw new Exception(
							"GUIRippingContextAnalyzer - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
				}
				final Element name = p_children.get(0);
				final Element value = p_children.get(1);
				if (!"Name".equals(name.getNodeName()) || !"Value".equals(value.getNodeName())) {
					throw new Exception(
							"GUIRippingContextAnalyzer - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
				}

				switch (name.getTextContent()) {
				case ("text"):
					label = value.getTextContent().toLowerCase();
					break;
				case ("Title"):
					// we use also the property title in case text is not
					// available
					if (label == null) {
						label = value.getTextContent().toLowerCase();
					}
					break;
				case ("ID"):
					id = value.getTextContent();
					break;
				case ("X"):
					x = Integer.valueOf(value.getTextContent());
					break;
				case ("Y"):
					y = Integer.valueOf(value.getTextContent());
					break;
				case ("width"):
					width = Integer.valueOf(value.getTextContent());
					break;
				case ("height"):
					height = Integer.valueOf(value.getTextContent());
					break;
				}
			}
			if (id == null || x == -1 || y == -1 || height == -1 || width == -1 || label == null) {
				throw new Exception(
						"GUIRippingContextAnalyzer - traverseContainersRecursive: wrong GUI file for GUIRippingConvert");
			}

			this.descriptorsInContainer.put(id, new ArrayList<Descriptor>());

			final String desc = this.getDescriptor(x, y, width, height, father_id);

			if (desc != null) {
				this.containerDescription.put(id, desc);
			} else {
				// if no descriptor was found we use the title or the text
				// attribute as descriptor
				if (USE_CONTAINER_TITLE_TEXT && label != null) {
					this.containerDescription.put(id, label);
				}
			}
		}

		// iterate trough the elements contained in the container
		final List<Element> contained = XMLUtil.getChildrenElements(contents);
		for (final Element n : contained) {
			this.traverseContainersRecursive(n, id);
		}
	}

	/**
	 * Function that verify whether the widget is a descriptor
	 *
	 * @param widget_attributes
	 * @param w_id:
	 *            id of the window
	 * @return
	 * @throws Exception
	 */
	private Descriptor extractDescriptor(final Element widget_attributes) throws Exception {

		// final List<Element> w_children =
		// XMLUtil.getChildrenElements(in_widget);
		//
		// if (w_children.size() != 1 &&
		// !"Attributes".equals(w_children.get(0).getNodeName())) {
		// throw new Exception("GUIRippingContextAnalyzer - extractDescriptor:
		// wrong GUI file for GUIRippingConvert");
		// }
		// final Element attributes = w_children.get(0);
		final List<Element> properties = XMLUtil.getChildrenElements(widget_attributes);
		if (properties.size() < 2) {
			throw new Exception("GUIRippingContextAnalyzer - extractDescriptor: wrong GUI file for GUIRippingConvert");
		}

		// the class name is extracted
		final Element classs = properties.get(1);
		final List<Element> class_children = XMLUtil.getChildrenElements(classs);
		final String class_name = class_children.get(1).getTextContent();

		if (this.descriptors_classes.contains(class_name)) {

			String label = null;
			int x = -1;
			int y = -1;
			int width = -1;
			int height = -1;
			boolean visible = true;
			for (final Element p : properties) {
				final List<Element> p_children = XMLUtil.getChildrenElements(p);

				if (!"Property".equals(p.getNodeName()) || p_children.size() != 2) {
					throw new Exception(
							"GUIRippingContextAnalyzer - extractDescriptor: wrong GUI file for GUIRippingConvert");
				}

				final Element name = p_children.get(0);
				final Element value = p_children.get(1);
				if (!"Name".equals(name.getNodeName()) || !"Value".equals(value.getNodeName())) {
					throw new Exception(
							"GUIRippingContextAnalyzer - extractDescriptor: wrong GUI file for GUIRippingConvert");
				}

				switch (name.getTextContent()) {
				case ("X"):
					x = Integer.valueOf(value.getTextContent());
					break;
				case ("Y"):
					y = Integer.valueOf(value.getTextContent());
					break;
				case ("width"):
					width = Integer.valueOf(value.getTextContent());
					break;
				case ("height"):
					height = Integer.valueOf(value.getTextContent());
					break;
				case ("text"):
					label = value.getTextContent().toLowerCase();
					break;
				case ("Title"):
					// we use also the property title in case text is not
					// available
					if (label == null) {
						label = value.getTextContent().toLowerCase();
					}
					break;
				case ("visible"):
					visible = Boolean.valueOf(value.getTextContent());
				}
			}

			if (x == -1 || y == -1 || width == -1 || height == -1 || label == null) {
				throw new Exception(
						"GUIRippingContextAnalyzer - extractDescriptor: wrong GUI file for GUIRippingConvert");
			}
			if (visible) {
				final Descriptor out = new Descriptor(label, x, y, height, width);
				return out;
			}
		}
		return null;
	}

	/**
	 * function that returns the nearest descriptor wrt the x and y coordinates
	 * in input. if a descriptor is not found a descriptor associated to the
	 * container is returned. It returns null if nothing is found
	 *
	 * @param x
	 * @param y
	 * @param father_id
	 * @return
	 */
	public String getDescriptor(final int x, final int y, final int width, final int height, final String father_id) {

		if (x == 16 && y == 112 && height == 28 && width == 134) {
			System.out.println();
		}
		final Area area = new Area(x, y, height, width);

		final List<Descriptor> descriptors;
		if (LOCAL_ANALYSIS) {
			descriptors = this.getLocalDescriptor(area, father_id);
		} else {
			descriptors = this.getGlobalDescriptor(area);
		}
		if (descriptors.size() == 0) {
			if (father_id != null) {
				return this.containerDescription.get(father_id);
			} else {
				return null;
			}
		}

		double min_dist = Double.MAX_VALUE;
		Descriptor nearer = null;
		for (final Descriptor desc : descriptors) {
			final double dist = area.getDistance(desc.a);
			if (dist < min_dist) {
				min_dist = dist;
				nearer = desc;
			}
		}
		return nearer.label;
	}

	private List<Descriptor> getLocalDescriptor(final Area area, final String father_id) {

		if (father_id != null) {
			final List<Descriptor> descriptors = this.descriptorsInContainer.get(father_id).stream()
					.filter(e -> area.isRelated(e.a)).collect(Collectors.toList());
			return descriptors;
		}
		return new ArrayList<Descriptor>();
	}

	private List<Descriptor> getGlobalDescriptor(final Area area) {

		final List<Descriptor> descriptors = new ArrayList<>();
		for (final String container : this.descriptorsInContainer.keySet()) {
			final List<Descriptor> d_list = this.descriptorsInContainer.get(container).stream()
					.filter(e -> area.isRelated(e.a)).collect(Collectors.toList());
			descriptors.addAll(d_list);
		}

		return descriptors;
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
		 * function that returns true if the input area on the top left of this
		 * area
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
