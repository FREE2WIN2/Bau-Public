package net.wargearworld.bau.tools;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.utils.Loc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class AutoCannonReloader {
    private static final int TIMEOUT = 20 * Main.getPlugin().getCustomConfig().getInt("tntReload.timeout");
    private static final int MAX_TNT = Main.getPlugin().getCustomConfig().getInt("tntReload.maxTnt");
    public static final Material toolMaterial = Material
            .valueOf(Main.getPlugin().getCustomConfig().getString("tntReload.materialType"));

    public static HashSet<Loc> tntLocations;
    private boolean recording;
    private boolean antiSpam;

    public AutoCannonReloader() {
        tntLocations = new HashSet<>();
        antiSpam = false;
        recording = false;
    }


    public void startRecord(Player p) {
        recording = true;
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_startRecord");
    }

    public void endRecord(Player p) {
        recording = false;
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_endRecord");
    }

    public void deleteRecord(Player p, boolean b) {
        if (!b) {
            MessageHandler msgHandler = MessageHandler.getInstance();
            TextComponent tc = new TextComponent(msgHandler.getString(p, "cannonreloader_delete_warning_text"));
            TextComponent clickTC = new TextComponent(msgHandler.getString(p, "cannonreloader_delete_warning_click"));
            clickTC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tr reset confirm"));
            clickTC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msgHandler.getString(p, "cannonreloader_delete_warning_hover")).create()));

            tc.addExtra(clickTC);
            p.spigot().sendMessage(tc);
            return;
        }
        recording = false;
        tntLocations.clear();
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_deleteRecord");
    }

    public void pasteRecord(Player p) {
        UUID uuid = p.getUniqueId();
        if (antiSpam) {
            Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_antispam",
                    String.valueOf(TIMEOUT / 20));
            return;
        }
        World world = p.getWorld();
        for (Loc loc : tntLocations) {
            Location location = loc.toLocation(world);
            if (location.getBlock().getType().isAir() || location.getBlock().isLiquid()) {
                location.getBlock().setType(Material.TNT);
            }
        }
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_pasteRecord");
        antispam(uuid);
    }

    private void antispam(UUID uuid) {
        antiSpam = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

            @Override
            public void run() {
                antiSpam = false;
            }
        }, TIMEOUT);
    }

    protected void showHelp(Player p) {
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_help1");
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_help2");
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_help3");
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_help4");
        Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_help5");
    }

    public boolean hasConent() {
        return !tntLocations.isEmpty();
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean isAntiSpam() {
        return antiSpam;
    }

    public void save(Location location, Player p) {
        if (tntLocations.size() >= MAX_TNT) {
            Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_maxTntOverload",
                    String.valueOf(MAX_TNT));
            return;
        }

        tntLocations.add(new Loc(location));
        if (tntLocations.size() == MAX_TNT) {
            Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_maxTnt",
                    String.valueOf(MAX_TNT));
        }
    }

    public void remove(Location location, Player p) {
        tntLocations.remove(new Loc(location));
    }

    public int getSize() {
        return tntLocations.size();
    }
}