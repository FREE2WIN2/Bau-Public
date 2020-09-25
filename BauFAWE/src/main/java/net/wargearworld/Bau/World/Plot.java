package net.wargearworld.Bau.World;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.Bau.Main;

public abstract class Plot {

	private ProtectedRegion region;
	private String id;

	protected Plot(ProtectedRegion region, String id) {
		this.region = region;
		this.id = id;
	}

	public ProtectedRegion getRegion() {
		return region;
	}

	public String getId() {
		return id;
	}

	public boolean toggleSL() {
		if (region.getFlag(Main.stoplag) == State.ALLOW) {
			region.setFlag(Main.stoplag, State.DENY);
			return false;
		} else {
			region.setFlag(Main.stoplag, State.ALLOW);
			return true;
		}
	}

	public void setSL(boolean on) {
		if (on) {
			region.setFlag(Main.stoplag, State.ALLOW);
		} else {
			region.setFlag(Main.stoplag, State.DENY);
		}
	}

	public boolean getSL() {
		return region.getFlag(Main.stoplag) == State.ALLOW;
	}

}
