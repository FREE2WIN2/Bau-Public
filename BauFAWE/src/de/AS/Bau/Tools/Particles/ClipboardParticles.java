package de.AS.Bau.Tools.Particles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ClipboardParticles implements CommandExecutor, Listener {
	public static File particlesConfigFile;
	public static YamlConfiguration particleConfig;
	public static HashMap<UUID,ParticlesShow> playersParticlesShow = new HashMap<>();
	public static boolean defaultState;
	public static Color defaultClipboardColor;
	public static Color defaultSelectionColor;
	
	public ClipboardParticles() {
		defaultState = particleConfig.getBoolean("default.active");
		defaultClipboardColor = readColor("default.clipboard");
		defaultSelectionColor = readColor("default.selection");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
		/* 
		 * particles clipboard 155 155 155 | clip
		 * particles selection 155 155 155 | sel
		 * particles
		 * particles on|off
		 */
		if(!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player)sender;
		if(args.length == 0) {
			/* Toggle particles */
			
			
		}
		
		
		return false;
	}
	
	@EventHandler 
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		/* look if user has particle On */
	}
	
	public static Color readColor(String path) {
		String[] args = particleConfig.getString(path).split(" ");
		int r = Integer.parseInt(args[0]);
		int g = Integer.parseInt(args[1]);
		int b = Integer.parseInt(args[2]);
		return Color.fromRGB(r, g, b);
	}

}
