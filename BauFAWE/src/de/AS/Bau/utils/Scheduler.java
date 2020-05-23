package de.AS.Bau.utils;

import org.bukkit.Bukkit;

public class Scheduler {

	int id;
	int x;
	int y;
	int ymin;
	int z;

	public Scheduler() {
		id = -1;
	}

	public void cancel() {
		Bukkit.getScheduler().cancelTask(id);
		id = -1;
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
		if(id == -1) {
			id = task;
		}else {
			Bukkit.getScheduler().cancelTask(task);
		}
	}

	public int getTask() {
		return id;
	}
}
