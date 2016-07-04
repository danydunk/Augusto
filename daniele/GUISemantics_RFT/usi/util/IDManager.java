package usi.util;

public class IDManager {

	private int nextWindId;
	private int nextWidId;

	private final int FIRSTID = 1;

	public IDManager() {

		this.nextWindId = this.FIRSTID;
		this.nextWidId = this.FIRSTID;
	}

	public String nextWindowId() {

		return "wind" + this.nextWindId++;
	}

	public String nextWidgetId() {

		return "wid" + this.nextWidId++;
	}
}
