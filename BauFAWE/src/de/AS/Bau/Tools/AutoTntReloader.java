package de.AS.Bau.Tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.AS.Bau.Main;

public class AutoTntReloader implements Listener {

	public static HashMap<UUID, HashSet<Location>> playersTntLocations = new HashMap<>();
	public static HashSet<UUID> playerRecord = new HashSet<>();
	public static HashSet<UUID> playerAntiSpam = new HashSet<>();
	private static int timeout = 20 * Main.getPlugin().getCustomConfig().getInt("tntReload.timeout");
	private static int maxTnt = Main.getPlugin().getCustomConfig().getInt("tntReload.maxTnt");
	public static Material toolMaterial = Material
			.valueOf(Main.getPlugin().getCustomConfig().getString("tntReload.materialType"));
	private static String prefix = "�8[�6TNTReloader�8] �r";

	@EventHandler
	public void clickListener(PlayerInteractEvent event) {
		Action a = event.getAction();
		Player p = event.getPlayer();
		if (!event.getMaterial().equals(toolMaterial)) {
			return;
		}
		if (a.equals(Action.RIGHT_CLICK_AIR)) {
			if (playersTntLocations.containsKey(p.getUniqueId())) {
				if (playerRecord.contains(p.getUniqueId())) {
					/* stop */
					endRecord(p);
				} else {
					/* paste */
					pasteRecord(p);
				}

			} else {
				/* start */
				startRecord(p);
			}
		} else if (a.equals(Action.LEFT_CLICK_AIR)) {
			/* delete */
			deleteRecord(p);
		}
	}

	@EventHandler
	public void registerTnt(BlockPlaceEvent event) {
		if (!event.getBlockPlaced().getType().equals(Material.TNT)) {
			return;
		}
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!playerRecord.contains(uuid)) {
			return;
		}

		HashSet<Location> set = new HashSet<>();
		if (!playersTntLocations.containsKey(uuid)) {
			playersTntLocations.put(uuid, set);
		}
		set = playersTntLocations.get(uuid);
		if (set.size() >= maxTnt) {
			Main.send(p, true, prefix, "tntReloader_maxTntOverload", String.valueOf(maxTnt));
			return;
		}

		set.add(event.getBlockPlaced().getLocation());
		if (set.size() == maxTnt) {
			Main.send(p, true, prefix, "tntReloader_maxTnt", String.valueOf(maxTnt));
		}
		playersTntLocations.put(uuid, set);
	}

	public static void startRecord(Player p) {
		playerRecord.add(p.getUniqueId());
		Main.send(p, true, prefix, "tntReloader_startRecord");
	}

	public static void endRecord(Player p) {
		playerRecord.remove(p.getUniqueId());
		Main.send(p, true, prefix, "tntReloader_endRecord");
	}

	public static void deleteRecord(Player p) {
		playerRecord.remove(p.getUniqueId());
		if (playersTntLocations.containsKey(p.getUniqueId())) {
			playersTntLocations.remove(p.getUniqueId());
			Main.send(p, true, prefix, "tntReloader_deleteRecord");
		}
	}

	private void pasteRecord(Player p) {
		UUID uuid = p.getUniqueId();
		if (playerAntiSpam.contains(uuid)) {
			Main.send(p, true, prefix, "tntReloader_antispam", String.valueOf(timeout));
			return;
		}

		for (Location loc : playersTntLocations.get(uuid)) {
			loc.getBlock().setType(Material.TNT);
		}
		Main.send(p, true, prefix, "tntReloader_pasteRecord");
		antispam(uuid);
	}

	private void antispam(UUID uuid) {
		playerAntiSpam.add(uuid);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				playerAntiSpam.remove(uuid);
			}
		}, timeout);
	}

}
