package net.wargearworld.Bau.Tools.Fernzuender;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.Switch.Face;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.StringGetterBau;
import net.wargearworld.Bau.utils.PlayerUtil;

public class Fernzuender {

	private Location loc;
	private UUID owner;
	
	public Fernzuender(UUID uuid) {
		loc = null;
		owner = uuid;
	}



	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public Location getLoc() {
		return loc;
	}

	public void activate() {
		Player p = PlayerUtil.getPlayer(owner);
		
		if(p == null) {
			return;
		}
		Block block = loc.getWorld().getBlockAt(loc);
		if(!(block.getBlockData() instanceof Switch||block.getBlockData() instanceof NoteBlock)) {
			return;
		}
		if(block.getBlockData() instanceof Switch) {
			activateSwitch(block,p);
		}else if(block.getBlockData() instanceof NoteBlock) {
			activateJuke(block,p);
		}

	}

	private void activateJuke(Block block, Player p) {
		NoteBlock note = (NoteBlock) block.getBlockData();
		note.setPowered(!note.isPowered());
		block.setBlockData(note);
		applyPhysics(block);
		Main.send(p, "fzNoteBlockActivate");
	}

	private void activateSwitch(Block block, Player p) {
		Block behind = getBlockBehind(block);
		Switch switchData = (Switch) block.getBlockData();
		if (block.getType().getKey().toString().toLowerCase().contains("button")) {
			int ticks;
			if (block.getType().getKey().toString().toLowerCase().contains("stone")) {
				ticks = 20;
			} else {
				ticks = 30;
			}
			switchData.setPowered(true);
			block.setBlockData(switchData, true);

			applyPhysics(block);
			applyPhysics(behind);

			// apllyPhysics

			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzButtonActivated"));
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

				@Override
				public void run() {
					switchData.setPowered(false);
					block.setBlockData(switchData, true);

					applyPhysics(block);
					applyPhysics(behind);

				}
			}, ticks);
		} else if (block.getType().equals(Material.LEVER)) {

			// set data

			boolean activate = !switchData.isPowered();
			switchData.setPowered(activate);
			block.setBlockData(switchData, true);

			// applyPhysics

			applyPhysics(block);
			applyPhysics(behind);

			// message

			if (activate) {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzLeverActivated"));
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzLeverDeactivated"));
			}
		}
		
	}

	private Block getBlockBehind(Block block) {
		BlockData bd = block.getState().getBlockData();
		Switch switchData = (Switch) bd;
		Switch.Face face = switchData.getFace();

		BlockFace bf = ((Directional) bd).getFacing();
		if (face.equals(Face.CEILING)) {
			bf = BlockFace.DOWN;
		} else if (face.equals(Face.FLOOR)) {
			bf = BlockFace.UP;
		}
		return block.getRelative(bf.getOppositeFace());
	}

	private static void applyPhysics(Block block) {
		((CraftWorld) block.getWorld()).getHandle().applyPhysics(
				new BlockPosition(block.getX(), block.getY(), block.getZ()), ((CraftBlock) block).getNMS().getBlock());
	}
}
