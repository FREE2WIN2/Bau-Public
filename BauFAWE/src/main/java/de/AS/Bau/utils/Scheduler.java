package de.AS.Bau.utils;

import org.bukkit.Bukkit;

public class Scheduler {

	int id;
	int x;
	int y;
	int ymin;
	int z;
	boolean running;
	public Scheduler() {
		running = false;
	}

	public void cancel() {
		Bukkit.getScheduler().cancelTask(id);
		running = false;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setZ(int z) {
		this.z = z;
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
		running = true;
	}

	public int getTask() {
		return id;
	}
	
	public boolean isRunning() {
		return running;
	}
}
