package net.wargearworld.bau;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.wargearworld.CommandManager.CommandManager;
import net.wargearworld.GUI_API.GUI_API;
import net.wargearworld.bau.cmds.*;
import net.wargearworld.bau.communication.DatabaseCommunication;
import net.wargearworld.bau.listener.*;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.player.DefaultPlayer;
import net.wargearworld.bau.tabCompleter.TntReloaderTC;
import net.wargearworld.bau.tabCompleter.tbsTC;
import net.wargearworld.bau.tools.*;
import net.wargearworld.bau.tools.particles.Particles;
import net.wargearworld.bau.tools.testBlockSlave.TestBlockSlaveCore;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.DefaultTestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlockEditor.TestBlockEditorCore;
import net.wargearworld.bau.tools.waterremover.WaterRemoverListener;
import net.wargearworld.bau.tools.worldfuscator.WorldFuscatorIntegration;
import net.wargearworld.bau.world.WorldGUI;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.worldedit.SaWE;
import net.wargearworld.bau.worldedit.WorldEditPreCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main extends JavaPlugin {
    private static Main plugin;
    public static StateFlag TntExplosion;
    public static StateFlag stoplag;
    public static String schempath;

    private static File customConfigFile;
    private static YamlConfiguration customConfig;
    public static String prefix = "§8[§6Bau§8] §r";
    public static File tempAddConfigFile;
    public static YamlConfiguration tempAddConfig;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        createConfigs();

        registerCommands();
        registerListener();
        WorldManager.startCheckForTempAddRemoves();
        WorldManager.checkForWorldsToUnload();
        new CompassBar();
        CommandManager.registerPlugin(this, MessageHandler.getInstance());
        new GUI_API(this, MessageHandler.getInstance());
        new WorldGUI(this);
        DefaultTestBlock.generateDefaultTestBlocks();
        DatabaseCommunication.startRecieve();
        schempath = customConfig.getString("schempath");
        new WorldFuscatorIntegration(this);
//		doTests();
    }

    private void registerListener() {
        new onPlayerJoin(this);
        new SignListener(this);
        new ClickListener(this);
        new onPlayerQuit(this);
        new onPlayerMove(this);
        new onPlayerTeleport(this);
        new ExplosioneventListener(this);
        new SpawnEvent(this);
        new onPlayerRespawn(this);
        new WaterRemoverListener(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(TestBlockSlaveCore.getInstance(), this);
        pm.registerEvents(new eventsToCancel(), this);
        pm.registerEvents(new Stoplag(), this);
        pm.registerEvents(new WorldEditPreCommand(), this);
        pm.registerEvents(new DesignTool(), this);
        pm.registerEvents(new TntChest(), this);
        pm.registerEvents(new GUI(), this);
        pm.registerEvents(new TestBlockEditorCore(), this);
        pm.registerEvents(new SaWE(), this);
        pm.registerEvents(AutoCannonReloaderListener.getInstance(), this);
    }

    private void registerCommands() {
        new gs();
        new Particles();
        new WorldFuscatorIntegration(this);
        getCommand("tnt").setExecutor(new tnt());
        getCommand("gui").setExecutor(new GUI());
        getCommand("tbs").setExecutor(TestBlockSlaveCore.getInstance());
        getCommand("tbs").setTabCompleter(new tbsTC());
        getCommand("sl").setExecutor(new Stoplag());
        getCommand("dt").setExecutor(new DesignTool());
        getCommand("ds").setExecutor(new ds());
        getCommand("debugstick").setExecutor(new ds());
        getCommand("chest").setExecutor(new TntChest());
        getCommand("stats").setExecutor(new stats());
        getCommand("plotreset").setExecutor(new PlotResetter());
        getCommand("baureload").setExecutor(new Bau());
        getCommand("tr").setTabCompleter(new TntReloaderTC());
        getCommand("clear").setExecutor(new clear());
        getCommand("tr").setExecutor(AutoCannonReloaderListener.getInstance());
        getCommand("tr").setTabCompleter(AutoCannonReloaderListener.getInstance());
    }

    public void createConfigs() {

        customConfigFile = createConfigFile("config.yml");
        customConfig = createConfig(customConfigFile);

        tempAddConfigFile = createConfigFile("tempAddConfig.yml");
        tempAddConfig = createConfig(tempAddConfigFile);

        Particles.particlesConfigFile = createConfigFile("particles.yml");
        Particles.particleConfig = createConfig(Particles.particlesConfigFile);

        DefaultPlayer.configFile = createConfigFile("playerDefaults.yml");
        DefaultPlayer.config = createConfig(DefaultPlayer.configFile);
    }

    public static File createConfigFile(String string) {
        File configFile = new File(plugin.getDataFolder(), string);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(string, false);
        }
        return configFile;
    }

    public static YamlConfiguration createConfig(File configFile) {
        YamlConfiguration config = new YamlConfiguration();

        try {
            FileInputStream in = new FileInputStream(configFile);
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            config.load(reader);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return config;
    }

    public static Main getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            StateFlag flag = new StateFlag("TntExplosion", true);
            StateFlag stoplag = new StateFlag("stoplag", true);
            StateFlag waterRemoverFlag = new StateFlag("waterremover", false);
            StateFlag worldfuscatorFlag = new StateFlag("worldfuscator", false);
            registry.register(flag);
            registry.register(stoplag);
            registry.register(waterRemoverFlag);
            registry.register(worldfuscatorFlag);
            TntExplosion = flag; // only set our field if there was no error
            Main.stoplag = stoplag;
            WaterRemoverListener.waterRemoverFlag = waterRemoverFlag;
            WorldFuscatorIntegration.worldfuscatorFlag = worldfuscatorFlag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("TntExplosion");
            Flag<?> existingSL = registry.get("stoplag");
            Flag<?> existingWR = registry.get("waterremover");
            Flag<?> existingWF = registry.get("worldfuscator");
            if ((existing instanceof StateFlag) && existingSL instanceof StateFlag && existingWR instanceof StateFlag && existingWF instanceof StateFlag) {
                TntExplosion = (StateFlag) existing;
                stoplag = (StateFlag) existingSL;
                WaterRemoverListener.waterRemoverFlag = (StateFlag) existingWR;
                WorldFuscatorIntegration.worldfuscatorFlag = (StateFlag) existingWF;
            } else {
                System.out.println("Fehler");
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
        super.onLoad();
    }

    public FileConfiguration getCustomConfig() {
        return customConfig;
    }

    public File getCustomConfigFile() {
        return customConfigFile;
    }

    @Override
    public void onDisable() {
        for (World w : Bukkit.getServer().getWorlds()) {
            if (!w.getName().equals("world")) {
                for (Player a : w.getPlayers()) {
                    a.kickPlayer("Close");
                }
                unloadWorld(w.getName());
            }
        }
        super.onDisable();
    }

    public void unloadWorld(String worldName) {
        Bukkit.getServer().unloadWorld(worldName, true);
        // Main main = Main.getPlugin();
        // String path = main.getCustomConfig().getString("Config.path");
        // File source = new File(path+"/"+worldName);
        /*
         * File dir = new File(path + "/Worlds/"+worldName); dir.setWritable(true,
         * false); dir.setExecutable(true, false); dir.setReadable(true, false);
         * onPlayerJoin.copyFolder_raw(source, dir);
         */
        Bukkit.getServer().getWorlds().remove(Bukkit.getServer().getWorld(worldName));
        // deleteWorld(source);
    }

    public boolean deleteWorld(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteWorld(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public File getTempAddConfigFile() {
        return tempAddConfigFile;
    }

    public YamlConfiguration getTempAddConfig() {
        return tempAddConfig;
    }

    public static void send(Player p, String messageKey, String... args) {
        String message = Main.prefix + MessageHandler.getInstance().getString(p, messageKey);
        for (String rep : args) {
            message = message.replaceFirst("%r", rep);
        }
        p.sendMessage(message);
    }

    public static void send(Player p, boolean otherPrefix, String prefix, String messageKey, String... args) {
        String message = prefix + MessageHandler.getInstance().getString(p, messageKey);
        for (String rep : args) {
            message = message.replaceFirst("%r", rep);
        }
        p.sendMessage(message);
    }

    public static void send(BauPlayer p, String messageKey, String... args) {
        String message = prefix + MessageHandler.getInstance().getString(p, messageKey);
        for (String rep : args) {
            message = message.replaceFirst("%r", rep);
        }
        p.sendMessage(message);
    }

}
