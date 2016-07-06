package usi.util;

public class IDManager {

	private int nextWindId;
	private int nextAWId;
	private int nextIWId;
	private int nextSWId;

	private final int FIRSTID = 1;

	public IDManager() {

		this.nextWindId = this.FIRSTID;
		this.nextAWId = this.FIRSTID;
		this.nextIWId = this.FIRSTID;
		this.nextSWId = this.FIRSTID;
	}

	public String nextWindowId() {

		return "w" + this.nextWindId++;
	}

	public String nextAWId() {

		return "aw" + this.nextAWId++;
	}

	public String nextIWId() {

		return "iw" + this.nextIWId++;
	}

	public String nextSWId() {

		return "sw" + this.nextSWId++;
	}
}
