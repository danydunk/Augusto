package usi.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {

	/*
	 * function that save a XML into a path
	 */
	public static void save(final String path, final Document xml) throws Exception {

		try {
			final Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			// tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// send DOM to file
			tr.transform(new DOMSource(xml), new StreamResult(new FileOutputStream(path)));

		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("XMLUtil - save: error");
		}
	}

	/*
	 * function that save a XML into a temp file
	 */
	public static String saveTMP(final Document xml) throws Exception {

		try {
			final Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			// tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			final File f = File.createTempFile("xml", ".tmp");
			// send DOM to file
			tr.transform(new DOMSource(xml), new StreamResult(new FileOutputStream(f)));
			return f.getAbsolutePath();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("XMLUtil - save: error");
		}
	}

	/*
	 * function that reads a XML from a path
	 */
	public static Document read(final String path) throws Exception {

		try {
			final File fXmlFile = new File(path);
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("XMLUtil - read: error");
		}
	}

	/**
	 * function that extracts the elements children of a input element
	 *
	 * @param element
	 * @return
	 */
	public static List<Element> getChildrenElements(final Element element) {

		final List<Element> elements = new ArrayList<Element>();
		final NodeList nl = element.getChildNodes();
		for (int cont = 0; cont < nl.getLength(); cont++) {
			if (nl.item(cont).getNodeType() == Node.ELEMENT_NODE) {
				final Element el = (Element) nl.item(cont);
				elements.add(el);
			}
		}
		return elements;
	}

	/**
	 * function that searches a element with a certain name between the children
	 * of a input element
	 *
	 * @param element
	 * @return
	 */
	public static List<Element> searchChildren(final Element element, final String toSearch) {

		final List<Element> out = new ArrayList<Element>();
		final NodeList nl = element.getChildNodes();
		for (int cont = 0; cont < nl.getLength(); cont++) {
			if (nl.item(cont).getNodeType() == Node.ELEMENT_NODE) {
				final Element el = (Element) nl.item(cont);
				if (toSearch.equals(el.getNodeName())) {
					out.add(el);
				}
			}
		}
		return out;
	}

	/**
	 * Return the first node from a NodeList with a given name passed as
	 * parameter
	 *
	 * @param childWin
	 * @param name
	 * @return
	 */
	public static Node getElementNode(final NodeList childWin, final String name) {

		// FOR each GUI child:
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if (name.equals(nChild.getNodeName())) {
					return nChild;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieve All elements from the list that have a given name
	 *
	 * @param childWin
	 *            List of Nodes
	 * @param name
	 *            Name to match
	 * @return
	 */
	public static List<Node> getElementNodesList(final NodeList childWin, final String name) {

		final List<Node> retrievedNodes = new ArrayList<>();
		// FOR each GUI child:
		for (int ch = 0; ch < childWin.getLength(); ch++) {

			final Node nChild = childWin.item(ch);
			if (nChild.getNodeType() == Node.ELEMENT_NODE) {

				if (name.equals(nChild.getNodeName())) {
					retrievedNodes.add(nChild);
				}
			}
		}
		return retrievedNodes;
	}
}
