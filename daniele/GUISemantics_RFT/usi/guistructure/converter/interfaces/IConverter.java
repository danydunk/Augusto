package usi.guistructure.converter.interfaces;

import org.w3c.dom.Document;

/**
 * Converter interface, there should be a class implementing this interface for
 * each tool defined in GUIExtractionTools
 * 
 * @author daniele
 *
 */
public interface IConverter {

	public Document convert(Object[] inputs) throws Exception;

}
