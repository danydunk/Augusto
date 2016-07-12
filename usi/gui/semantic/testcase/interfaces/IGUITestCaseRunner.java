package usi.gui.semantic.testcase.interfaces;

import usi.gui.semantic.testcase.GUITestCase;

/**
 * GUITestCaseRunner interface, there should be a class implementing this
 * interface for each tool defined in GUITestCaseRunnerTools
 *
 * @author daniele
 *
 */
public interface IGUITestCaseRunner {

	public boolean runGUITestCase(GUITestCase test) throws Exception;

	public boolean[] runGUITestSuite(GUITestCase[] tests) throws Exception;
}
