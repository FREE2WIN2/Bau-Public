package net.wargearworld.bau.tools.particles;

import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.arguments.DynamicListArgument;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.CommandNode;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.DefaultPlayer;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.wargearworld.command_manager.arguments.IntegerArgument.integer;
import static net.wargearworld.command_manager.nodes.ArgumentNode.argument;
import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;

public class Particles implements TabExecutor, Listener {
	public static File particlesConfigFile;
	public static YamlConfiguration particleConfig;
	public static HashMap<UUID, ParticlesShow> playersParticlesShow = new HashMap<>();
	public static boolean defaultStateClipboard;
	public static boolean defaultStateSelection;
	public static Color defaultClipboardColor;
	public static Color defaultSelectionColor;
	private static Particles instance;

	private CommandHandel commandHandle;

	public Particles() {
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
		Main.getPlugin().getCommand("particles").setExecutor(this);
		Main.getPlugin().getCommand("particles").setTabCompleter(this);
		defaultStateClipboard = particleConfig.getBoolean("particles.clipboard.color");
		defaultStateSelection = particleConfig.getBoolean("particles.selection.color");
		defaultClipboardColor = readColor(DefaultPlayer.config.getString("particles.clipboard.color"));
		defaultSelectionColor = readColor(DefaultPlayer.config.getString("particles.selection.color"));

		commandHandle = new CommandHandel("particles", Main.prefix, MessageHandler.getInstance());
		commandHandle.addSubNode(literal("hilfe").setCallback(s -> {
			showHelp(getPlayer(s));
		}));
		commandHandle.addSubNode(literal("help").setCallback(s -> {
			showHelp(getPlayer(s));
		}));
		commandHandle.addSubNode(literal("gui").setCallback(s -> {
			ParticlesGUI.open(getPlayer(s));
		}));

			/*
			 * Clipboard
			 */

		CommandNode clipboard = argument("Clipboard", new DynamicListArgument("Clipboard", s -> {
			return List.of("clip", "clipboard");
		}));
		clipboard.setCallback(s -> {
			ParticlesShow particleShow = playersParticlesShow.get(getPlayer(s).getUniqueId());
			setActive(ParticleContent.CLIPBOARD, !particleShow.isClipboardActive(), getPlayer(s));
		});
		clipboard.addSubNode(literal("on").setCallback(s -> {
			setActive(ParticleContent.CLIPBOARD, true, getPlayer(s));
		}));
		clipboard.addSubNode(literal("an").setCallback(s -> {
			setActive(ParticleContent.CLIPBOARD, true, getPlayer(s));
		}));
		clipboard.addSubNode(literal("off").setCallback(s -> {
			setActive(ParticleContent.CLIPBOARD, false, getPlayer(s));
		}));
		clipboard.addSubNode(literal("aus").setCallback(s -> {
			setActive(ParticleContent.CLIPBOARD, false, getPlayer(s));
		}));

		/*
		 * Selection
		 */

		CommandNode selection = argument("Selection", new DynamicListArgument("Selection", s -> {
			return List.of("sel", "selection");
		}));
		selection.setCallback(s -> {
			ParticlesShow particleShow = playersParticlesShow.get(getPlayer(s).getUniqueId());
			setActive(ParticleContent.SELECTION,!particleShow.isSelectionActive(), getPlayer(s));
		});
		selection.addSubNode(literal("on").setCallback(s -> {
			setActive(ParticleContent.SELECTION, true, getPlayer(s));
		}));
		selection.addSubNode(literal("an").setCallback(s -> {
			setActive(ParticleContent.SELECTION, true, getPlayer(s));
		}));
		selection.addSubNode(literal("off").setCallback(s -> {
			setActive(ParticleContent.SELECTION, false, getPlayer(s));
		}));
		selection.addSubNode(literal("aus").setCallback(s -> {
			setActive(ParticleContent.SELECTION, false, getPlayer(s));
		}));
		commandHandle.addSubNode(selection);

		/*
		 * Color
		 */

		CommandNode r = argument("R", integer(0, 255));
		CommandNode g = argument("G", integer(0, 255));
		CommandNode b = argument("B", integer(0, 255));

		r.addSubNode(g.addSubNode(b.setCallback(s -> {
			setColor(s);
		})));
		CommandNode colorArg = argument("Color", Colors.asArgument());
		try {
			selection
					.addSubNode(colorArg.clone()
							.setCallback(s -> {
								setColor(ParticleContent.CLIPBOARD, s);
							}));
			clipboard
					.addSubNode(colorArg.clone()
							.setCallback(s -> {
								setColor(ParticleContent.CLIPBOARD, s);
							}));
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		commandHandle.addSubNode(clipboard);
	}


	private void setColor(ArgumentList s) {
		Player p = getPlayer(s);
		ParticlesShow particleShow = playersParticlesShow.get(p.getUniqueId());
		boolean selection = s.getString("Selection") != null;
		int r = s.getInt("R");
		int g = s.getInt("G");
		int b = s.getInt("B");
		if (selection) {
			particleShow.setSelectionColor(r, g, b);
			Main.send(p, "particles_selection_colorsetRGB", r + "", g + "", b + "");
		} else {
			particleShow.setClipboardColor(r, g, b);
			Main.send(p, "particles_selection_colorsetRGB", r + "", g + "", b + "");
		}
	}

	private void setColor(ParticleContent particleContent, ArgumentList s) {
		ParticlesShow particleShow = playersParticlesShow.get(getPlayer(s).getUniqueId());
		Player p = getPlayer(s);
		Colors colors = (Colors) s.getEnum("Color");
		Color color = colors.getColor();
		if (particleContent == ParticleContent.CLIPBOARD) {
			particleShow.setSelectionColor(color);
			Main.send(p, "particles_selection_colorset", colors.name());
		} else if (particleContent == ParticleContent.SELECTION) {
			particleShow.setClipboardColor(color);
			Main.send(p, "particles_clipboard_colorset", colors.name());
		}
	}

	protected static void setActive(ParticleContent content, boolean active, Player player) {
		ParticlesShow particleShow = playersParticlesShow.get(player.getUniqueId());
		if(content == ParticleContent.SELECTION){
		particleShow.setSelectionActive(active);
			if (!active) {
				Main.send(player, "particles_selection_toggledOff");
			} else {
				Main.send(player, "particles_selection_toggledOn");
			}
		}else{
			particleShow.setClipboardActive(active);
			if (!active) {
				Main.send(player, "particles_clipboard_toggledOff");
			} else {
				Main.send(player, "particles_clipboard_toggledOn");
			}
		}

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		Player p = (Player) sender;
		List<String> out = new ArrayList<>();
		BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
		commandHandle.tabComplete(commandPlayer, MessageHandler.getInstance().getLanguage(p), args,out);
		return out;
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
		BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
		if (!commandHandle.execute(commandPlayer, MessageHandler.getInstance().getLanguage(p), args)) {
			Main.send(p, "particles_wrongCommand");
		}
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

		playersParticlesShow.get(event.getPlayer().getUniqueId()).stop();
		playersParticlesShow.remove(event.getPlayer().getUniqueId());
	}

	public static Color readColor(String arg) {
		String[] args = arg.split(" ");
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
