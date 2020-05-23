package de.AS.Bau.Tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;

public class AutoCannonReloader implements Listener, CommandExecutor {

	public static HashMap<UUID, HashSet<Location>> playersTntLocations = new HashMap<>();
	public static HashSet<UUID> playerRecord = new HashSet<>();
	public static HashSet<UUID> playerAntiSpam = new HashSet<>();
	private static int timeout = 20 * Main.getPlugin().getCustomConfig().getInt("tntReload.timeout");
	private static int maxTnt = Main.getPlugin().getCustomConfig().getInt("tntReload.maxTnt");
	public static Material toolMaterial = Material
			.valueOf(Main.getPlugin().getCustomConfig().getString("tntReload.materialType"));
	private static AutoCannonReloader instance;
	
	public static AutoCannonReloader getInstance() {
		if(instance == null) {
			return new AutoCannonReloader();
		}
		return instance;
	}
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		/*
		 * tr|tntReload|cannonReload|cr start , stop , paste , reset , help
		 * 
		 */
		if(!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		if(args.length == 0) {
			showHelp(p);
			return true;
		}
		if(args.length == 1) {
			switch(args[0].toLowerCase()) {
			case "start":
				startRecord(p);
				return true;
			case "stop":
				endRecord(p);
				return true;
			case "paste":
			case "reload":
				pasteRecord(p);
				return true;
			case "help":
				showHelp(p);
				return true;
			case "reset":
				return true;	
			}
		}
		
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_wrongCommand");
		return true;
	}

	


	@EventHandler
	public void clickListener(PlayerInteractEvent event) {
		Action a = event.getAction();
		Player p = event.getPlayer();
		if (!event.getMaterial().equals(toolMaterial)) {
			return;
		}
		if (a.equals(Action.RIGHT_CLICK_AIR)||a.equals(Action.RIGHT_CLICK_BLOCK)) {
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
		} else if (a.equals(Action.LEFT_CLICK_AIR)||a.equals(Action.LEFT_CLICK_BLOCK)) {
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
			Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_maxTntOverload", String.valueOf(maxTnt));
			return;
		}

		set.add(event.getBlockPlaced().getLocation());
		if (set.size() == maxTnt) {
			Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_maxTnt", String.valueOf(maxTnt));
		}
		playersTntLocations.put(uuid, set);
	}

	@EventHandler
	public void unregister(BlockBreakEvent event) {
		if (!event.getBlock().getType().equals(Material.TNT)) {
			return;
		}
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!playerRecord.contains(uuid)) {
			return;
		}

		if (!playersTntLocations.containsKey(uuid)) {
			return;
		}
		HashSet<Location> set = playersTntLocations.get(uuid);
		if(set.contains(event.getBlock().getLocation())) {
			set.remove(event.getBlock().getLocation());
			playersTntLocations.put(uuid, set);
		}
		
		
	}
	
	public static void startRecord(Player p) {
		playerRecord.add(p.getUniqueId());
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_startRecord");
	}

	public static void endRecord(Player p) {
		playerRecord.remove(p.getUniqueId());
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_endRecord");
	}

	public static void deleteRecord(Player p) {
		playerRecord.remove(p.getUniqueId());
		if (playersTntLocations.containsKey(p.getUniqueId())) {
			playersTntLocations.remove(p.getUniqueId());
			Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_deleteRecord");
		}
	}

	private void pasteRecord(Player p) {
		UUID uuid = p.getUniqueId();
		if (playerAntiSpam.contains(uuid)) {
			Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_antispam", String.valueOf(timeout/20));
			return;
		}

		for (Location loc : playersTntLocations.get(uuid)) {
			loc.getBlock().setType(Material.TNT);
		}
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_pasteRecord");
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


	private void showHelp(Player p) {
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_help1");
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_help2");
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_help3");
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_help4");
		Main.send(p, true, StringGetterBau.getString(p, "cannonReloader_prefix"), "tntReloader_help5");
	}
}
