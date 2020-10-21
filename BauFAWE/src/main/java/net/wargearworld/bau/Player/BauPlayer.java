package net.wargearworld.bau.player;

import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.wargearworld.StringGetter.Language;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.plots.Plot;
import net.wargearworld.db.model.PlotMember;
import net.wargearworld.thedependencyplugin.DependencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class BauPlayer {
    private static HashMap<UUID, BauPlayer> players = new HashMap<>();

    public static BauPlayer getBauPlayer(Player p) {
        if (p != null)
            return getBauPlayer(p.getUniqueId());
        return null;
    }

    public static BauPlayer getBauPlayer(UUID uuid) {
        BauPlayer player = players.get(uuid);
        if (player == null) {
            player = new BauPlayer(uuid);
            players.put(uuid, player);
        }
        return player;
    }

    public static void remove(UUID uniqueId) {
        players.remove(uniqueId);
    }

    private UUID uuid;
    FileConfiguration config;
    File configFile;
    private Language language;

    private BauPlayer(UUID uuid) {
        this.uuid = uuid;
        configFile = new File(Main.getPlugin().getDataFolder(), "users/" + uuid.toString() + "/settings.yml");
        config = new YamlConfiguration();
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                Files.copy(DefaultPlayer.configFile.toPath(), configFile.toPath());
            }
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        this.language = Language.valueOf(getDbPlayer().getCountryCode().toUpperCase());
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public String getName() {
        return getDbPlayer().getName();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Stoplag */

    public void setSLPaste(boolean active) {
        config.set("stoplag.paste", active);
        saveConfig();
    }

    public void setSLPasteTime(Integer time) {
        config.set("stoplag.pastetime", time);
        saveConfig();
    }

    public boolean getPasteState() {
        return config.getBoolean("stoplag.paste");
    }

    public int getPasteTime() {
        return config.getInt("stoplag.pastetime");
    }

    public Plot getCurrentPlot() {
        Location loc = getBukkitPlayer().getLocation();
        return WorldManager.get(loc.getWorld()).getPlot(loc);
    }

    public boolean toggleDT() {
        setDT(!getDT());
        return getDT();

    }

    public void setDT(boolean on) {
        config.set("dt", on);
        saveConfig();
    }

    public boolean getDT() {
        return config.getBoolean("dt");
    }

    public void sendMemberedGS() {
        Player p = getBukkitPlayer();
        if (p == null)
            return;

        ArrayList<net.wargearworld.db.model.Plot> memberedPlots = new ArrayList<>();
        memberedPlots.addAll(getDbPlayer().getPlots());
        for (PlotMember member : getDbPlayer().getMemberedPlots()) {
            memberedPlots.add(member.getPlot());
        }
        PlayerConnection pConn = ((CraftPlayer) p).getHandle().playerConnection;
        p.sendMessage(MessageHandler.getInstance().getString(p, "listGsHeading"));
        for (net.wargearworld.db.model.Plot plot : memberedPlots) {
            /* s == PlayerName */
            String name = plot.getName();
            String hover = MessageHandler.getInstance().getString(p, "listGsHover").replace("%r", name);
            String txt = "{\"text\":\"§7[§6" + name
                    + "§7]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/gs tp " + name
                    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + hover + "\"}}}";
            IChatBaseComponent txtc = ChatSerializer.a(txt);
            PacketPlayOutChat txtp = new PacketPlayOutChat(txtc);
            pConn.sendPacket(txtp);
        }
        p.sendMessage("§7----------------------------");
    }

    public void sendMessage(String message) {
        Player p = getBukkitPlayer();
        if (p == null)
            return;
        p.sendMessage(message);
    }

    public net.wargearworld.db.model.Player getDbPlayer() {
        return DBConnection.getPlayer(uuid);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public File getConfigFile() {
        return configFile;
    }

    public Language getLanguage() {
        return language;
    }

    public Set<net.wargearworld.db.model.Plot> getdbPlots(){
        EntityManager em = DependencyProvider.getEntityManager();
        net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class,uuid);
        Set<net.wargearworld.db.model.Plot> plots = dbPlayer.getPlots();
        em.close();
        return plots;
    }
    public boolean hasPlots(){
        EntityManager em = DependencyProvider.getEntityManager();
        net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class,uuid);
        Set<net.wargearworld.db.model.Plot> plots = dbPlayer.getPlots();
        boolean hasPlots = !plots.isEmpty();
        em.close();
        return hasPlots;
    }

}
