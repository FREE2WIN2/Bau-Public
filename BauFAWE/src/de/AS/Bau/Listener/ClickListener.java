package de.AS.Bau.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.cmds.dt;
import net.minecraft.server.v1_15_R1.BlockPosition;

@SuppressWarnings("deprecation")
public class ClickListener implements Listener {
	public static HashMap<UUID, Block> playersDetonator = new HashMap<>();
	public static HashMap<Location, Integer> redstoneWireActive = new HashMap<>();
	public static HashSet<UUID> blockedForFZ = new HashSet<>();

	public ClickListener(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onclick(PlayerInteractEvent e) {
		Player p = (Player) e.getPlayer();
		ItemStack is = e.getItem();
		if (!(is == null)) {
			if (is.getItemMeta().getDisplayName().equals("§6GUI") && (e.getAction().equals(Action.RIGHT_CLICK_AIR)
					|| e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
				p.performCommand("gui");
			} else if (is.getType().equals(Material.SPAWNER)) {
				e.setCancelled(true);
			} else if (is.getType().equals(Material.WOODEN_HOE)) {
				// fernzünder
				fernzuender(e);


			} else {
				// dt
				ItemStack inhands = p.getItemInHand();

				if (inhands.getType().isBlock() && dt.playerHasDtOn.get(p.getUniqueId()) == true && e.getClickedBlock() != null) {
					Location locBlock = e.getClickedBlock().getLocation();
					Action action = e.getAction();
					if ((-65 > locBlock.getBlockX() && locBlock.getBlockX() > -371)
							|| (locBlock.getBlockY() < 69 && locBlock.getBlockY() > 8)
							|| (locBlock.getBlockZ() < 99 && locBlock.getBlockZ() > -65)) {
						if (action.equals(Action.RIGHT_CLICK_BLOCK) && !p.isSneaking()) {
							e.getClickedBlock().setType(inhands.getType());
							e.setCancelled(true);
						}

					}
				}
			}
		}
	}

	private void fernzuender(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Action a = e.getAction();
		if (!blockedForFZ.contains(p.getUniqueId())) {
			if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_AIR)) {
				// zünden
				if (playersDetonator.containsKey(p.getUniqueId())) {
					zuenden(e);

					// spam block

//					blockedForFZ.add(p);
//					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
//
//						@Override
//						public void run() {
//							blockedForFZ.remove(p);
//
//						}
//					}, 20 * 5);
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
		} else {
			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "noSpamFZ"));
		}

	}

	private void zuenden(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block b = playersDetonator.get(p.getUniqueId());
		BlockData bd = b.getState().getBlockData();
		Switch switchData = (Switch) bd;
		Switch.Face face = switchData.getFace();
		Block behind;
		if(face.equals(Face.CEILING)) {
			behind = p.getWorld().getBlockAt(b.getX(), b.getY() +1, b.getZ());
		}else if(face.equals(Face.FLOOR)) {
			behind = p.getWorld().getBlockAt(b.getX(), b.getY() -1, b.getZ());
		}else {
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
			((CraftWorld) b.getWorld()).getHandle().applyPhysics(new BlockPosition(b.getX(), b.getY(), b.getZ()),
					((CraftBlock) b).getNMS().getBlock());
			((CraftWorld) b.getWorld()).getHandle().applyPhysics(
					new BlockPosition(behind.getX(), behind.getY(), behind.getZ()),
					((CraftBlock) behind).getNMS().getBlock());

			// apllyPhysics

			p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzButtonActivated"));
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

				@Override
				public void run() {
					switchData.setPowered(false);
					b.setBlockData(switchData, true);
					new BlockPhysicsEvent(b, switchData);
					((CraftWorld) b.getWorld()).getHandle().applyPhysics(
							new BlockPosition(b.getX(), b.getY(), b.getZ()), ((CraftBlock) b).getNMS().getBlock());
					((CraftWorld) b.getWorld()).getHandle().applyPhysics(
							new BlockPosition(behind.getX(), behind.getY(), behind.getZ()),
							((CraftBlock) behind).getNMS().getBlock());

				}
			}, ticks);
		} else if (b.getType().equals(Material.LEVER)) {

			// set data

			boolean activate = !((Switch) bd).isPowered();
			switchData.setPowered(activate);
			b.setBlockData(switchData, true);

			// applyPhysics

			((CraftWorld) b.getWorld()).getHandle().applyPhysics(new BlockPosition(b.getX(), b.getY(), b.getZ()),
					((CraftBlock) b).getNMS().getBlock());
			((CraftWorld) b.getWorld()).getHandle().applyPhysics(
					new BlockPosition(behind.getX(), behind.getY(), behind.getZ()),
					((CraftBlock) behind).getNMS().getBlock());

			// message

			if (activate) {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzLeverActivated"));
			} else {
				p.sendMessage(Main.prefix + StringGetterBau.getString(p, "fzLeverDeactivated"));
			}
		}

	}
}
