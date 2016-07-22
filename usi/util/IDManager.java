package usi.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import usi.gui.structure.GUI;

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

	public static IDManager getInstance() {

		return instance;
	}

	private IDManager(final GUI gui) {

		this.gui = gui;
	}

	private int getLastWid() {

		final List<String> ids = this.gui.getWindows().stream().map(e -> e.getId())
				.collect(Collectors.toList());

		Collections.sort(ids);
		if (ids.size() == 0) {
			return 0;
		}
		final String lastid = ids.get(ids.size() - 1);
		return Integer.valueOf(lastid.substring(1));
	}

	public String nextWindowId() {

		int ind = this.getLastWid();
		ind++;
		this.lastWid = ind;
		return "w" + ind;
	}

	public String nextAWId() {

		if (this.lastWid == this.getLastWid()) {
			this.lastAWid++;
			return "aw" + this.lastAWid;
		}
		final List<String> ids = this.gui.getAction_widgets().stream().map(e -> e.getId())
				.collect(Collectors.toList());

		Collections.sort(ids);
		if (ids.size() == 0) {
			return "aw1";
		}
		final String lastid = ids.get(ids.size() - 1);
		int ind = Integer.valueOf(lastid.substring(2));
		ind++;
		return "aw" + ind;
	}

	public String nextIWId() {

		if (this.lastWid == this.getLastWid()) {
			this.lastIWid++;
			return "iw" + this.lastIWid;
		}
		final List<String> ids = this.gui.getInput_widgets().stream().map(e -> e.getId())
				.collect(Collectors.toList());

		Collections.sort(ids);
		if (ids.size() == 0) {
			return "iw1";
		}
		final String lastid = ids.get(ids.size() - 1);
		int ind = Integer.valueOf(lastid.substring(2));
		ind++;
		return "iw" + ind;
	}

	public String nextSWId() {

		if (this.lastWid == this.getLastWid()) {
			this.lastSWid++;
			return "sw" + this.lastSWid;
		}
		final List<String> ids = this.gui.getSelectable_widgets().stream().map(e -> e.getId())
				.collect(Collectors.toList());

		Collections.sort(ids);
		if (ids.size() == 0) {
			return "sw1";
		}
		final String lastid = ids.get(ids.size() - 1);
		int ind = Integer.valueOf(lastid.substring(2));
		ind++;
		return "sw" + ind;
	}
}
