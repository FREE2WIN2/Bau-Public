package de.AS.Bau.utils;

import org.bukkit.Bukkit;

public class Scheduler {

	int id;
	int xCoord;
	int y;
	int ymin;
	int z;
	public Scheduler() {}
	
	public void cancel() {
		Bukkit.getScheduler().cancelTask(id);
	}
	public void setX(int x) {
		xCoord = x;
	}
	public int getX() {
		return xCoord;
	}
	public void setY(int y) {
		this.y=y;
	}
	public int getY() {
		return y;
	}
	public void setZ(int z) {
		this.z=z;
	}
	public int getZ() {
		return z;
	}
	public void setYmin(int y) {
		ymin = y;
	}
	public int getYMin() {
		return ymin;
	}
	
	public void setTask(int task) {
		id = task;
	}
}
