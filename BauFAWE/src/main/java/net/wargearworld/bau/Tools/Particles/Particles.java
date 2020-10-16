package net.wargearworld.bau.tools.particles;

import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.Arguments.DynamicListArgument;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.CommandManager.CommandNode;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
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

import static net.wargearworld.CommandManager.Arguments.IntegerArgument.integer;
import static net.wargearworld.CommandManager.Nodes.ArgumentNode.argument;
import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;

public class Particles implements TabExecutor, Listener {
    public static File particlesConfigFile;
    public static YamlConfiguration particleConfig;
    public static HashMap<UUID, ParticlesShow> playersParticlesShow = new HashMap<>();
    public static boolean defaultState;
    public static Color defaultClipboardColor;
    public static Color defaultSelectionColor;
    private static Particles instance;

    private CommandHandel commandHandle;

    public Particles() {
        Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
        Main.getPlugin().getCommand("particles").setExecutor(this);
        Main.getPlugin().getCommand("particles").setTabCompleter(this);
        defaultState = particleConfig.getBoolean("default.active");
        defaultClipboardColor = readColor("default.clipboard");
        defaultSelectionColor = readColor("default.selection");

        commandHandle = new CommandHandel("particles", Main.prefix, Main.getPlugin());
        commandHandle.setCallback(s -> {
            ParticlesShow particleShow = playersParticlesShow.get(s.getPlayer().getUniqueId());
            setActive(!particleShow.isActive(), s.getPlayer());
        });
        commandHandle.addSubNode(literal("on").setCallback(s -> {
            setActive(true, s.getPlayer());
        }));
        commandHandle.addSubNode(literal("an").setCallback(s -> {
            setActive(true, s.getPlayer());
        }));
        commandHandle.addSubNode(literal("off").setCallback(s -> {
            setActive(false, s.getPlayer());
        }));
        commandHandle.addSubNode(literal("aus").setCallback(s -> {
            setActive(false, s.getPlayer());
        }));

        commandHandle.addSubNode(literal("hilfe").setCallback(s -> {
            showHelp(s.getPlayer());
        }));
        commandHandle.addSubNode(literal("help").setCallback(s -> {
            showHelp(s.getPlayer());
        }));
        commandHandle.addSubNode(literal("gui").setCallback(s -> {
            ParticlesGUI.open(s.getPlayer());
        }));

        CommandNode r = argument("R", integer(0, 255));
        CommandNode g = argument("G", integer(0, 255));
        CommandNode b = argument("B", integer(0, 255));

        r.addSubNode(g.addSubNode(b.setCallback(s -> {
            setColor(s);
        })));
        CommandNode colorArg = argument("Color", Colors.asArgument());
        try {
            commandHandle.addSubNode(argument("Selection", new DynamicListArgument("Selection", s -> {
                return List.of("sel", "selection");
            }))
                    .addSubNode(colorArg.clone()
                            .setCallback(s -> {
                                setColor(ParticleContent.CLIPBOARD, s);
                            })));
            commandHandle.addSubNode(argument("Clipboard", new DynamicListArgument("Clipboard", s -> {
                return List.of("clip", "clipboard");
            }))
                    .addSubNode(colorArg.clone()
                            .setCallback(s -> {
                                setColor(ParticleContent.CLIPBOARD, s);
                            })));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void setColor(ArgumentList s) {
        Player p = s.getPlayer();
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
        ParticlesShow particleShow = playersParticlesShow.get(s.getPlayer().getUniqueId());
        Player p = s.getPlayer();
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

    private void setActive(boolean active, Player player) {
        ParticlesShow particleShow = playersParticlesShow.get(player.getUniqueId());
        particleShow.setactive(active);
        if (!active) {
            Main.send(player, "particles_toggledOff");
        } else {
            Main.send(player, "particles_toggledOn");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player p = (Player) sender;
        List<String> out = new ArrayList<>();
        commandHandle.tabComplete(p,MessageHandler.getInstance().getLanguage(p), args,out);
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
        if (!commandHandle.execute(p, MessageHandler.getInstance().getLanguage(p), args)) {
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
        playersParticlesShow.remove(event.getPlayer().getUniqueId());
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
