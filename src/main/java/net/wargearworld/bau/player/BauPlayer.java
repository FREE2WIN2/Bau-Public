package net.wargearworld.bau.player;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.wargearworld.StringGetter.Language;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.cannon_reloader.AutoCannonReloader;
import net.wargearworld.bau.tools.cannon_timer.CannonTimerBlock;
import net.wargearworld.bau.tools.testBlock.TestBlock;
import net.wargearworld.bau.tools.testBlock.testBlockEditor.TestBlockEditor;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.gui.WorldGUI;
import net.wargearworld.bau.world.plot.Plot;
import net.wargearworld.bau.worldedit.WorldEditHandler;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

//@Transactional
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
    /* Tools */
    private TestBlock testBlockSlave;
    private TestBlockEditor testBlockEditor;
    private AutoCannonReloader cannonReloader;
    private CannonTimerBlock copiedCannonTimerBlock;
    private File clipboardFile;

    private BauPlayer(UUID uuid) {
        this.uuid = uuid;
        configFile = new File(Main.getPlugin().getDataFolder(), "users/" + uuid.toString() + "/settings.yml");
        config = new YamlConfiguration();
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
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
        Player p = getBukkitPlayer();
        if (p == null)
            return null;

        Location loc = p.getLocation();
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

    public void setActivateTrailOnCannonTimer(boolean on) {
        config.set("activateTrailOnCannonTimer", on);
        saveConfig();
    }

    public boolean getActivateTrailOnCannonTimer() {
        return config.getBoolean("activateTrailOnCannonTimer");
    }

    public void sendMemberedGS() {
        Player p = getBukkitPlayer();
        if (p == null)
            return;
        WorldGUI.openMain(p,1);
    }

    public void sendMessage(String message) {
        Player p = getBukkitPlayer();
        if (p == null)
            return;
        p.sendMessage(message);
    }

    public net.wargearworld.db.model.Player getDbPlayer() {
        return EntityManagerExecuter.run(em -> {
            return em.find(net.wargearworld.db.model.Player.class, uuid);
        });
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

    public Set<World> getdbWorlds() {
        return EntityManagerExecuter.run(em -> {
            return em.find(net.wargearworld.db.model.Player.class, uuid).getWorlds();
        });
    }

    public boolean hasWorlds() {
        return EntityManagerExecuter.run(em -> {
            Set<World> plots = em.find(net.wargearworld.db.model.Player.class, uuid).getWorlds();
            return plots != null && !plots.isEmpty();
        });
    }

    public TestBlock getTestBlockSlave() {
        if (testBlockSlave == null)
            testBlockSlave = new TestBlock(uuid);
        return testBlockSlave;
    }

    public TestBlockEditor getTestBlockEditor() {
        if (testBlockEditor == null)
            testBlockEditor = new TestBlockEditor(uuid);
        return testBlockEditor;
    }

    public AutoCannonReloader getCannonReloader() {
        if (cannonReloader == null)
            cannonReloader = new AutoCannonReloader();
        return cannonReloader;
    }

    public void setMovingCannonTimerBlock(CannonTimerBlock cannonTimerBlock) {
        this.copiedCannonTimerBlock = cannonTimerBlock;
    }

    public CannonTimerBlock getCopiedCannonTimerBlock() {
        return copiedCannonTimerBlock;
    }

    public void loadClipboard() {
        Player p = getBukkitPlayer();
        if (p == null)
            return;
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
        clipboardFile = new File(Main.getPlugin().getDataFolder(), "users/" + uuid.toString() + "/clipboard.schem");
        if (!clipboardFile.exists())
            return;
        Clipboard clipboard = WorldEditHandler.createClipboard(clipboardFile);
        localSession.setClipboard(new ClipboardHolder(clipboard));
        MessageHandler.getInstance().send(p, "clipboard_restored");
    }

    public void saveClipboard() {
        Player p = getBukkitPlayer();
        if (p == null) {
            return;
        }
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
        try {
            WorldEditHandler.saveClipboardAsSchematic(Main.getPlugin().getDataFolder() + "/users/" + uuid.toString(), "clipboard.schem", localSession.getClipboard().getClipboard());
            MessageHandler.getInstance().send(p, "clipboard_saved");
        } catch (EmptyClipboardException e) {
        }

    }
}
