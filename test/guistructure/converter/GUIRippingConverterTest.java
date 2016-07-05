package test.guistructure.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import usi.guistructure.converter.GUIExtractionTools;
import usi.guistructure.converter.GUIStructureConverter;
import usi.guistructure.converter.interfaces.IConverter;
import usi.xml.XMLUtil;

public class GUIRippingConverterTest {

	@Test
	public void test1() {

		try {
			final Document g = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmsmall-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmsmall-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// XMLUtil.save("./files/for_test/xml/out.xml", out);
			final Element el = out.getDocumentElement();
			final List<Element> el_child = XMLUtil.getChildrenElements(el);
			assertEquals(5, el_child.size());
			assertEquals(2, el.getElementsByTagName("window").getLength());
			assertEquals(3, el.getElementsByTagName("edge").getLength());

			// check for the correctness of the data context analysis
			final NodeList windows = el.getElementsByTagName("window");
			final Element form_w = (Element) windows.item(0);
			final NodeList i_widgets = form_w.getElementsByTagName("input_widget");
			assertEquals(5, i_widgets.getLength());
			assertTrue(
					"account".equals(XMLUtil.getChildrenElements((Element) i_widgets.item(0)).get(0).getTextContent()));
			assertTrue(
					"user id".equals(XMLUtil.getChildrenElements((Element) i_widgets.item(1)).get(0).getTextContent()));
			assertTrue("password"
					.equals(XMLUtil.getChildrenElements((Element) i_widgets.item(2)).get(0).getTextContent()));
			assertTrue("url".equals(XMLUtil.getChildrenElements((Element) i_widgets.item(3)).get(0).getTextContent()));
			assertTrue(
					"notes".equals(XMLUtil.getChildrenElements((Element) i_widgets.item(4)).get(0).getTextContent()));

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() {

		try {
			final Document g = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmfull-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmfull-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// XMLUtil.save("./files/for_test/xml/out.xml", out);

			final Element el = out.getDocumentElement();
			assertEquals(9, el.getElementsByTagName("window").getLength());

			// check for the correctness of the data context analysis in only 1
			// window
			final NodeList windows = el.getElementsByTagName("window");
			final Element form_w = (Element) windows.item(2);
			final NodeList i_widgets = form_w.getElementsByTagName("input_widget");
			assertEquals(3, i_widgets.getLength());
			assertTrue("url".equals(XMLUtil.getChildrenElements((Element) i_widgets.item(0)).get(0).getTextContent()));
			assertTrue("user name"
					.equals(XMLUtil.getChildrenElements((Element) i_widgets.item(1)).get(0).getTextContent()));
			assertTrue("password"
					.equals(XMLUtil.getChildrenElements((Element) i_widgets.item(2)).get(0).getTextContent()));

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	// private ArrayList<Element> getChildrenElements(final Element element) {
	// final ArrayList<Element> elements = new ArrayList<>();
	// final NodeList nl = element.getChildNodes();
	// for (int cont = 0; cont < nl.getLength(); cont++) {
	// if (nl.item(cont).getNodeType() == Node.ELEMENT_NODE) {
	// final Element el = (Element) nl.item(cont);
	// elements.add(el);
	// }
	// }
	// return elements;
	// }
}
