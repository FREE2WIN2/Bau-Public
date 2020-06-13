package net.wargearworld.Bau.Tools.Particles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.utils.HelperMethods;

public class Particles implements CommandExecutor, Listener {
	public static File particlesConfigFile;
	public static YamlConfiguration particleConfig;
	public static HashMap<UUID, ParticlesShow> playersParticlesShow = new HashMap<>();
	public static boolean defaultState;
	public static Color defaultClipboardColor;
	public static Color defaultSelectionColor;
	private static Particles instance;

	public Particles() {
		defaultState = particleConfig.getBoolean("default.active");
		defaultClipboardColor = readColor("default.clipboard");
		defaultSelectionColor = readColor("default.selection");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		/*
		 * particles particles on|off particles clipboard 155 155 155 | clip particles
		 * selection 155 155 155 | sel
		 */
		if (!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		ParticlesShow particleShow = playersParticlesShow.get(p.getUniqueId());
		if (args.length == 0) {

			/* Toggle particles */
			if (particleShow.isActive()) {
				Main.send(p, "particles_toggledOff");
			} else {
				Main.send(p, "particles_toggledOn");
			}
			particleShow.setactive(!particleShow.isActive());

			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("an")) {
				particleShow.setactive(true);// und start?
				Main.send(p, "particles_toggledOn");
				return true;
			} else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("aus")) {
				particleShow.setactive(false);
				Main.send(p, "particles_toggledOff");
				return true;
			} else if (args[0].equalsIgnoreCase("hilfe") || args[0].equalsIgnoreCase("help")) {
				showHelp(p);
				return true;
			} else if (args[0].equalsIgnoreCase("gui")) {
				ParticlesGUI.open(p);
				return true;
			}
		}
		if (args.length == 2) {
			if (Colors.valueOf(args[1].toUpperCase()) == null) {
				Main.send(p, "particles_wrongColor");
			}
			if (args[0].equalsIgnoreCase("selection") || args[0].equalsIgnoreCase("sel")) {
				particleShow.setSelectionColor(Colors.valueOf(args[1].toUpperCase()).getColor());
				Main.send(p, "particles_selection_colorset", args[1]);
				return true;
			} else if (args[0].equalsIgnoreCase("clipboard") || args[0].equalsIgnoreCase("clip")) {
				particleShow.setClipboardColor(Colors.valueOf(args[1].toUpperCase()).getColor());
				Main.send(p, "particles_clipboard_colorset", args[1]);
				return true;
			} else {
				Main.send(p, "particles_wrongCommand");
			}
		}
		if (args.length == 4) {
			if (!HelperMethods.argsAreInt(args, 1, 3)) {
				Main.send(p, "particles_expectedInt", args[0]);
				return true;
			}
			if (!HelperMethods.argsArepositiveInt(args, 1, 3)) {
				Main.send(p, "particles_onlyPositiveInt");
				return true;
			}

			if (args[0].equalsIgnoreCase("selection") || args[0].equalsIgnoreCase("sel")) {
				particleShow.setSelectionColor(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Integer.parseInt(args[3]));
				Main.send(p, "particles_selection_colorsetRGB", args[1], args[2], args[3]);
				return true;
			} else if (args[0].equalsIgnoreCase("clipboard") || args[0].equalsIgnoreCase("clip")) {
				particleShow.setClipboardColor(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Integer.parseInt(args[3]));
				Main.send(p, "particles_clipboard_colorsetRGB", args[1], args[2], args[3]);
				return true;
			} else {
				Main.send(p, "particles_wrongCommand");
			}

		}
		if (args.length == 5) {
			if (!HelperMethods.argsAreInt(args, 1, 3)) {
				Main.send(p, "particles_expectedInt", args[0]);
				return true;
			}
			if (!HelperMethods.argsArepositiveInt(args, 1, 3)) {
				Main.send(p, "particles_onlyPositiveInt");
				return true;
			}

			if (args[0].equalsIgnoreCase("selection") || args[0].equalsIgnoreCase("sel")) {
				particleShow.setSelectionColor(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Integer.parseInt(args[3]));
				Main.send(p, "particles_selection_colorset", args[4]);
				return true;
			} else if (args[0].equalsIgnoreCase("clipboard") || args[0].equalsIgnoreCase("clip")) {
				particleShow.setClipboardColor(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
						Integer.parseInt(args[3]));
				Main.send(p, "particles_clipboard_colorset", args[4]);
				return true;
			} else {
				Main.send(p, "particles_wrongCommand");
			}

		}
		Main.send(p, "particles_wrongCommand");
		return true;
	}

	private void showHelp(Player p) {

	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		/* look if user has particle On */

		playersParticlesShow.put(p.getUniqueId(), new ParticlesShow(p));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		playersParticlesShow.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void switchHandListener(PlayerItemHeldEvent event) {
		Player p = event.getPlayer();
//		ItemStack old = p.getInventory().getItem(event.getPreviousSlot());
		ItemStack newItem = p.getInventory().getItem(event.getNewSlot());
		ParticlesShow particleShow = playersParticlesShow.get(p.getUniqueId());
		if (newItem == null) {
//			if (old == null) {
//				return;
//			} else if (old.getType().equals(Material.WOODEN_AXE)) {
//				particleShow.stopParticles();
//				return;
//			}
			return;
		}
		if (!newItem.getType().equals(Material.WOODEN_AXE)) {
//			if (old == null) {
//				return;
//			} else if (old.getType().equals(Material.WOODEN_AXE)) {
//				particleShow.stopParticles();
//				return;
//			}
			return;
		}

		particleShow.showParticles();

	}

	public static Color readColor(String path) {
		String[] args = particleConfig.getString(path).split(" ");
		int r = Integer.parseInt(args[0]);
		int g = Integer.parseInt(args[1]);
		int b = Integer.parseInt(args[2]);
		return Color.fromRGB(r, g, b);
	}

	public static void saveConfig() {
		try {
			particleConfig.save(particlesConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Particles getInstance() {
		if (instance == null) {
			return new Particles();
		}
		return instance;
	}

}
