package usi.testcase.inputdata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import usi.configuration.PathsManager;

import com.google.common.collect.Lists;

public class DataManager {

	private static DataManager instance = null;
	private final HashMap<String, List<String>> validDataMap = new HashMap<>();
	private final HashMap<String, List<String>> invalidDataMap = new HashMap<>();
	private final HashMap<String, List<Integer>> validItemizedDataMap = new HashMap<>();
	private final HashMap<String, List<Integer>> invalidItemizedDataMap = new HashMap<>();
	private final List<String> discardedWords = Lists.newArrayList("the", "in", "these", "this",
			"that", "of", "an", "and");
	private final List<String> specialCharacters = Lists.newArrayList(":", ";", ".");

	public static DataManager getInstance() throws Exception {

		if (instance == null) {
			instance = new DataManager();
		}

		return instance;
	}

	private DataManager() throws Exception {

		this.loadDataFromXMLFile();
	}

	public List<String> getValidData(final String descriptor) throws Exception {

		if (descriptor == null) {
			throw new Exception("DataManager - getValidData: null input.");
		}
		final List<String> out = new ArrayList<>();
		final List<String> descriptors = this.splitDescriptor(descriptor);
		for (final String s : descriptors) {
			final List<String> values = this.validDataMap.get(s);
			if (values != null && values.size() > 0) {
				out.addAll(values);
			}
		}
		return out;
	}

	public List<String> getInvalidData(final String descriptor) throws Exception {

		if (descriptor == null) {
			throw new Exception("DataManager - getInvalidData: null input.");
		}
		final List<String> out = new ArrayList<>();
		final List<String> descriptors = this.splitDescriptor(descriptor);
		for (final String s : descriptors) {
			final List<String> values = this.invalidDataMap.get(s);
			if (values != null && values.size() > 0) {
				out.addAll(values);
			}
		}
		return out;
	}

	public List<Integer> getValidItemizedData(final String descriptor) throws Exception {

		if (descriptor == null) {
			throw new Exception("DataManager - getValidItemizedData: null input.");
		}
		final List<Integer> out = new ArrayList<>();
		final List<String> descriptors = this.splitDescriptor(descriptor);
		for (final String s : descriptors) {
			final List<Integer> values = this.validItemizedDataMap.get(s);
			if (values != null && values.size() > 0) {
				out.addAll(values);
			}
		}
		return out;
	}

	public List<Integer> getInvalidItemizedData(final String descriptor) throws Exception {

		if (descriptor == null) {
			throw new Exception("DataManager - getInvalidItemizedData: null input.");
		}
		final List<Integer> out = new ArrayList<>();
		final List<String> descriptors = this.splitDescriptor(descriptor);
		for (final String s : descriptors) {
			final List<Integer> values = this.invalidItemizedDataMap.get(s);
			if (values != null && values.size() > 0) {
				out.addAll(values);
			}
		}
		return out;
	}

	public List<String> getValidGenericData() {

		final List<String> out = new ArrayList<>();
		final List<String> values = this.validDataMap.get("generic-input-data");
		if (values != null && values.size() > 0) {
			out.addAll(values);
		}
		return out;
	}

	public List<String> getInvalidGenericData() {

		final List<String> out = new ArrayList<>();
		final List<String> values = this.invalidDataMap.get("generic-input-data");
		if (values != null && values.size() > 0) {
			out.addAll(values);
		}
		return out;
	}

	private void loadDataFromXMLFile() throws Exception {

		final File dataSet = new File(PathsManager.getInputdataFilePath());

		if (dataSet.exists()) {
			final SAXBuilder builder = new SAXBuilder();

			final Document document = builder.build(dataSet);

			final Element rootElement = document.getRootElement();
			if (!rootElement.getName().equals("DATASET")) {
				throw new Exception("DataManager - loadDataFromXMLFile: wrong root.");
			}

			final List<Element> children = rootElement.getChildren();
			for (final Element nodo : children) {

				assert (nodo.getName().equals("DATA"));

				if (nodo.getAttribute("type") != null
						&& nodo.getAttribute("type").getValue().equals("itemized")) {
					// if it is itemized
					// get the metadata
					final String metadata = nodo.getChildText("metadata").trim();
					assert (!this.invalidItemizedDataMap.containsKey(metadata) && !this.validItemizedDataMap
							.containsKey(metadata));

					final List<Integer> valid = new ArrayList<>();
					final List<Integer> invalid = new ArrayList<>();
					this.validItemizedDataMap.put(metadata, valid);
					this.invalidItemizedDataMap.put(metadata, invalid);

					// recupero i valori
					if ((nodo.getChild("values") != null)
							&& (nodo.getChild("values").getChildren() != null)) {
						final List<Element> values = nodo.getChild("values").getChildren();

						for (final Element valueNode : values) {
							switch (valueNode.getName()) {
							case "valid":
								valid.add(Integer.valueOf(valueNode.getTextTrim()));
								break;
							case "invalid":
								invalid.add(Integer.valueOf(valueNode.getTextTrim()));
								break;
							default:
								throw new Exception(
										"DataManager - loadDataFromXMLFile: value type not found.");

							}
						}
					}
				} else {
					// get the metadata
					final String metadata = nodo.getChildText("metadata").trim();
					assert (!this.invalidDataMap.containsKey(metadata) && !this.validDataMap
							.containsKey(metadata));

					final List<String> valid = new ArrayList<>();
					final List<String> invalid = new ArrayList<>();
					this.validDataMap.put(metadata, valid);
					this.invalidDataMap.put(metadata, invalid);

					// recupero i valori
					if ((nodo.getChild("values") != null)
							&& (nodo.getChild("values").getChildren() != null)) {
						final List<Element> values = nodo.getChild("values").getChildren();

						for (final Element valueNode : values) {
							switch (valueNode.getName()) {
							case "valid":
								valid.add(valueNode.getTextTrim());
								break;
							case "invalid":
								invalid.add(valueNode.getTextTrim());
								break;
							default:
								throw new Exception(
										"DataManager - loadDataFromXMLFile: value type not found.");

							}
						}
					}
				}
			}
		} else {
			throw new Exception("DataManager - loadDataFromXMLFile: file not found.");
		}
	}

	private List<String> splitDescriptor(final String descriptor) {

		String d = descriptor;
		for (final String s : this.specialCharacters) {
			d = d.replace(s, " ");
		}
		for (final String s : this.discardedWords) {
			d = d.replace(" " + s + " ", " ");
		}
		final String[] descs = descriptor.split(" ");
		final List<String> out = new ArrayList<>();
		for (final String desc : descs) {
			if (desc.trim().length() > 0) {
				out.add(desc.trim().toLowerCase());
			}
		}
		return out;
	}
}
