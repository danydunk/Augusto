package usi.guistructure.converter;

import java.lang.reflect.Constructor;

import usi.guistructure.converter.interfaces.IConverter;

/**
 *
 * @author daniele
 *
 */
public class GUIStructureConverter {

	/**
	 * Method that returns the right converter for the technology used
	 * 
	 * @param tool
	 * @param inputs
	 * @return
	 * @throws Exception
	 */
	public static IConverter getConverter(final GUIExtractionTools tool) throws Exception {
		Class<?> converter_class;
		final String converter_to_load = GUIStructureConverter.class.getPackage().getName() + "." + tool.name()
				+ "Converter";
		IConverter out = null;
		try {
			converter_class = Thread.currentThread().getContextClassLoader().loadClass(converter_to_load);
			final Constructor<?> constructor = converter_class.getConstructor();
			out = (IConverter) constructor.newInstance();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("GUIStructureConverter: Error finding converter");
		}
		return out;
	}
}
