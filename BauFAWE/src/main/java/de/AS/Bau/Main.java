package de.AS.Bau;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import de.AS.Bau.HikariCP.DataSource;
import de.AS.Bau.Listener.ClickListener;
import de.AS.Bau.Listener.ExplosioneventListener;
import de.AS.Bau.Listener.SignListener;
import de.AS.Bau.Listener.SpawnEvent;
import de.AS.Bau.Listener.eventsToCancel;
import de.AS.Bau.Listener.onPlayerJoin;
import de.AS.Bau.Listener.onPlayerMove;
import de.AS.Bau.Listener.onPlayerQuit;
import de.AS.Bau.Listener.onPlayerRespawn;
import de.AS.Bau.Listener.onPlayerTeleport;
import de.AS.Bau.TabCompleter.TntReloaderTC;
import de.AS.Bau.TabCompleter.gsTC;
import de.AS.Bau.TabCompleter.particlesTC;
import de.AS.Bau.TabCompleter.tbsTC;
import de.AS.Bau.Tools.AutoCannonReloader;
import de.AS.Bau.Tools.DesignTool;
import de.AS.Bau.Tools.FernzuenderListener;
import de.AS.Bau.Tools.GUI;
import de.AS.Bau.Tools.PlotResetter;
import de.AS.Bau.Tools.Stoplag;
import de.AS.Bau.Tools.TntChest;
import de.AS.Bau.Tools.Particles.Particles;
import de.AS.Bau.Tools.Particles.ParticlesGUI;
import de.AS.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.DefaultTestBlock;
import de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor.TestBlockEditorCore;
import de.AS.Bau.WorldEdit.WorldEditEvents;
import de.AS.Bau.WorldEdit.WorldEditPreCommand;
import de.AS.Bau.cmds.Bau;
import de.AS.Bau.cmds.ds;
import de.AS.Bau.cmds.gs;
import de.AS.Bau.cmds.stats;
import de.AS.Bau.cmds.tnt;
import de.AS.Bau.utils.WorldHandler;

public class Main extends JavaPlugin {
	private static Main plugin;
	public static StateFlag TntExplosion;
	public static String schempath;

	private static File customConfigFile;
	private static YamlConfiguration customConfig;
	public static String prefix = "§8[§6Bau§8] §r";
	private static File tempAddConfigFile;
	private static YamlConfiguration tempAddConfig;

	@Override
	public void onEnable() {
		plugin = this;
		createConfigs();

		registerCommands();
		registerListener();

		gs.startCheckForTempAdd();
		WorldHandler.checkForWorldsToUnload();
		new DataSource();
		new StringGetterBau();
		DefaultTestBlock.generateDefaultTestBlocks();
		schempath = customConfig.getString("schempath");
		super.onEnable();
	}

	private void registerListener() {
		new onPlayerJoin(this);
		new SignListener(this);
		new WorldEditEvents(this);
		new ClickListener(this);
		new onPlayerQuit(this);
		new onPlayerMove(this);
		new onPlayerTeleport(this);
		new ExplosioneventListener(this);
		new SpawnEvent(this);
		new onPlayerRespawn(this);

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(TestBlockSlaveCore.getInstance(), this);
		pm.registerEvents(new eventsToCancel(), this);
		pm.registerEvents(new Stoplag(), this);
		pm.registerEvents(new WorldEditPreCommand(), this);
		pm.registerEvents(new DesignTool(), this);
		pm.registerEvents(new FernzuenderListener(), this);
		pm.registerEvents(new TntChest(), this);
		pm.registerEvents(new GUI(), this);
		pm.registerEvents(AutoCannonReloader.getInstance(), this);
		pm.registerEvents(Particles.getInstance(), this);
		pm.registerEvents(new ParticlesGUI(), this);
		pm.registerEvents(new TestBlockEditorCore(), this);
	}

	private void registerCommands() {
		getCommand("gs").setExecutor(new gs());
		getCommand("gs").setTabCompleter(new gsTC());
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
		getCommand("delcon").setExecutor(new PlotResetter());
		getCommand("baureload").setExecutor(new Bau());
		getCommand("particles").setExecutor(Particles.getInstance());
		getCommand("particles").setTabCompleter(new particlesTC());
		getCommand("tr").setExecutor(AutoCannonReloader.getInstance());
		getCommand("tr").setTabCompleter(new TntReloaderTC());
	}

	public void createConfigs() {

		customConfigFile = createConfigFile("config.yml");
		customConfig = createConfig(customConfigFile);

		tempAddConfigFile = createConfigFile("tempAddConfig.yml");
		tempAddConfig = createConfig(tempAddConfigFile);

		Stoplag.stoplagConfigFile = createConfigFile("stoplag.yml");
		Stoplag.stoplagConfig = createConfig(Stoplag.stoplagConfigFile);

		Particles.particlesConfigFile = createConfigFile("particles.yml");
		Particles.particleConfig = createConfig(Particles.particlesConfigFile);
	}

	private File createConfigFile(String string) {
		File configFile = new File(getDataFolder(), string);
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			saveResource(string, false);
		}
		return configFile;
	}

	private YamlConfiguration createConfig(File configFile) {
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
			registry.register(flag);
			TntExplosion = flag; // only set our field if there was no error
		} catch (FlagConflictException e) {
			Flag<?> existing = registry.get("TntExplosion");
			if (existing instanceof StateFlag) {
				TntExplosion = (StateFlag) existing;
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
		String message = Main.prefix + StringGetterBau.getString(p, messageKey);
		for (String rep : args) {
			message = message.replaceFirst("%r", rep);
		}
		p.sendMessage(message);
	}

	public static void send(Player p, boolean otherPrefix, String prefix, String messageKey, String... args) {
		String message = prefix + StringGetterBau.getString(p, messageKey);
		for (String rep : args) {
			message = message.replaceFirst("%r", rep);
		}
		p.sendMessage(message);
	}

}
