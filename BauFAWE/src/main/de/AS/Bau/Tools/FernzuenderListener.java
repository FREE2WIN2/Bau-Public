package de.AS.Bau.Tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.Switch.Face;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import net.minecraft.server.v1_15_R1.BlockPosition;

@SuppressWarnings("deprecation")
public class FernzuenderListener implements Listener {
	public static HashMap<UUID, Block> playersDetonator = new HashMap<>();
	public static HashSet<UUID> blockedForFZ = new HashSet<>();
	public static Material toolMaterial = Material.valueOf(Main.getPlugin().getCustomConfig().getString("fernzuender"));
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack is = event.getItem();
		if (!(is == null)) {
			if (is.getType().equals(toolMaterial)) {
				fernzuender(event);

			}
		}
	}

	private void fernzuender(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Action a = e.getAction();
		if (blockedForFZ.contains(p.getUniqueId())) {
			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noSpamFZ"));
			return;
		}
		if (a.equals(Action.RIGHT_CLICK_AIR)||a.equals(Action.LEFT_CLICK_AIR)) {
			if (playersDetonator.containsKey(p.getUniqueId())) {
				zuenden(e);
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "nothingSaved"));
			}
		} else if (a.equals(Action.RIGHT_CLICK_BLOCK) || a.equals(Action.LEFT_CLICK_BLOCK)) {
			// speichern oder zündeln!
			Block b = e.getClickedBlock();
			if (b.getBlockData() instanceof Switch) {
				playersDetonator.put(p.getUniqueId(), b);
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzSaved"));
				e.setCancelled(true);
			} else {
				if (playersDetonator.containsKey(p.getUniqueId())) {
					zuenden(e);
					e.setCancelled(true);
				} else {
					p.sendMessage(Main.prefix + StringGetterBau.getString(p, "nothingSaved"));
				}
			}
		}

	}

	private void zuenden(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block b = playersDetonator.get(p.getUniqueId());
		if(!(b.getBlockData() instanceof Switch)) {
			//TODO button weg
			return;
		}
		BlockData bd = b.getState().getBlockData();
		Switch switchData = (Switch) bd;
		
		Switch.Face face = switchData.getFace();
		Block behind;
		if (face.equals(Face.CEILING)) {
			behind = p.getWorld().getBlockAt(b.getX(), b.getY() + 1, b.getZ());
		} else if (face.equals(Face.FLOOR)) {
			behind = p.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ());
		} else {
			BlockFace bf = ((Directional) bd).getFacing();
			behind = b.getRelative(bf.getOppositeFace());
		}
		if (b.getType().getKey().toString().toLowerCase().contains("button")) {
			int ticks;
			if (b.getType().getKey().toString().toLowerCase().contains("stone")) {
				ticks = 20;
			} else {
				ticks = 30;
			}
			switchData.setPowered(true);
			b.setBlockData(switchData, true);

			applyPhysics(b);
			applyPhysics(behind);

			// apllyPhysics

			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzButtonActivated"));
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

				@Override
				public void run() {
					switchData.setPowered(false);
					b.setBlockData(switchData, true);

					applyPhysics(b);
					applyPhysics(behind);

				}
			}, ticks);
		} else if (b.getType().equals(Material.LEVER)) {

			// set data

			boolean activate = !((Switch) bd).isPowered();
			switchData.setPowered(activate);
			b.setBlockData(switchData, true);

			// applyPhysics

			applyPhysics(b);
			applyPhysics(behind);

			// message

			if (activate) {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzLeverActivated"));
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzLeverDeactivated"));
			}
		}

	}

	private void applyPhysics(Block block) {
		((CraftWorld) block.getWorld()).getHandle().applyPhysics(
				new BlockPosition(block.getX(), block.getY(), block.getZ()), ((CraftBlock) block).getNMS().getBlock());
	}
}
