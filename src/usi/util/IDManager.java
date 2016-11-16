package src.usi.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import src.usi.gui.structure.GUI;

public class IDManager {

	private static IDManager instance;

	private final GUI gui;

	private int lastAWid = 0;
	private int lastIWid = 0;
	private int lastSWid = 0;
	private int lastWid = 0;

	public static void create(final GUI gui) {

		instance = new IDManager(gui);
	}

	public static void create() {

		instance = new IDManager(null);
	}

	public static IDManager getInstance() {

		return instance;
	}

	private IDManager(final GUI gui) {

		this.gui = gui;
	}

	public void calculateLastIDs() {

		if (this.gui == null) {
			return;
		}
		// window
		List<Integer> ids = this.gui.getWindows().stream()
				.map(e -> Integer.valueOf(e.getId().replace("w", ""))).collect(Collectors.toList());
		Collections.sort(ids);
		if (ids.size() == 0) {
			this.lastWid = 0;
		} else {
			this.lastWid = ids.get(ids.size() - 1);
		}
		// AWS
		ids = this.gui.getAction_widgets().stream()
				.map(e -> Integer.valueOf(e.getId().replace("aw", "")))
				.collect(Collectors.toList());
		Collections.sort(ids);
		if (ids.size() == 0) {
			this.lastAWid = 0;
		} else {
			this.lastAWid = ids.get(ids.size() - 1);
		}
		// IWS
		ids = this.gui.getInput_widgets().stream()
				.map(e -> Integer.valueOf(e.getId().replace("iw", "")))
				.collect(Collectors.toList());
		Collections.sort(ids);
		if (ids.size() == 0) {
			this.lastIWid = 0;
		} else {
			this.lastIWid = ids.get(ids.size() - 1);
		}
		// SWS
		ids = this.gui.getSelectable_widgets().stream()
				.map(e -> Integer.valueOf(e.getId().replace("sw", "")))
				.collect(Collectors.toList());
		Collections.sort(ids);
		if (ids.size() == 0) {
			this.lastSWid = 0;
		} else {
			this.lastSWid = ids.get(ids.size() - 1);
		}
	}

	public String nextWindowId() {

		// this.calculateLastIDs();
		return "w" + (++this.lastWid);
	}

	public String nextAWId() {

		return "aw" + (++this.lastAWid);
	}

	public String nextIWId() {

		return "iw" + (++this.lastIWid);
	}

	public String nextSWId() {

		return "sw" + (++this.lastSWid);
	}
}
