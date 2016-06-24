package usi.guistructure.converter;

import static org.junit.Assert.*;

import org.junit.Test;

import usi.guistructure.converter.interfaces.IConverter;

public class GUIStructureConverterTest {

	@Test
	public void test() {
		try{
			IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			assertTrue(c != null);
			assertTrue(c.getClass() == GUIRippingConverter.class);
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
