package net.wargearworld.bau.world.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.worldedit.Schematic;
import net.wargearworld.bau.worldedit.WorldEditHandler;

public abstract class Plot {

	private ProtectedRegion region;
	private String id;

	private Location middleNorth;
	private Schematic ground;
	
	private Clipboard undo; //Undo from Reset
	protected Plot(ProtectedRegion region, String id, Location middleNorth, Schematic ground) {
		this.region = region;
		this.id = id;
		this.middleNorth = middleNorth;
		this.ground = ground;
	}

	public ProtectedRegion getRegion() {
		return region;
	}

	public String getId() {
		return id;
	}

	/* SL */
	
	public boolean toggleSL() {
		return toggleFlag(Main.stoplag);
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

	
	/* TNT */
	
	public boolean toggleTNT() {
		return toggleFlag(Main.TntExplosion);
	}
	
	public void setTNT(boolean on) {
		if (on) {
			region.setFlag(Main.TntExplosion, State.ALLOW);
		} else {
			region.setFlag(Main.TntExplosion, State.DENY);
		}
	}
	
	public boolean getTNT(){
		return region.getFlag(Main.TntExplosion) == State.ALLOW;
	}
	
	/* General Flags */
	private boolean toggleFlag(StateFlag flag) {
		if (region.getFlag(flag) == State.ALLOW) {
			region.setFlag(flag, State.DENY);
			return false;
		} else {
			region.setFlag(flag, State.ALLOW);
			return true;
		}
	}


	public boolean reset(World world) {
		/* save Undo */
		Region rg = new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
		rg.setWorld(BukkitAdapter.adapt(world));
		BlockArrayClipboard board = new BlockArrayClipboard(rg);
		board.setOrigin(BukkitAdapter.adapt(middleNorth).toVector().toBlockPoint());
		ForwardExtentCopy copy = new ForwardExtentCopy(rg.getWorld(), rg, board.getOrigin(), board,
				board.getOrigin());
		try {
			Operations.completeLegacy(copy);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		undo = board;
		delundo();
		
		WorldEditHandler.pasteground(ground, middleNorth);
		return true;
		
	}
	
	private void delundo() {
		Bukkit.getScheduler().runTaskLater(Main.getPlugin(), ()->{undo = null;}, 20*60*1);
	}

	public boolean undo(World world) {
		/* save Undo */
		if(undo == null)
			return false;
		WorldEditHandler.pasteAsync(new ClipboardHolder(undo), undo.getOrigin(), world, true);
		undo = null;
		return true;
		
	}
	
	public Location getTeleportPoint() {
		Location out = middleNorth.clone();//clone
		return out.add(0.5, 0, 0);
	}

	public abstract PlotType getType();

}
