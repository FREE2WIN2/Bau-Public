package de.AS.Bau;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import de.AS.Bau.Listener.ClickListener;
import de.AS.Bau.Listener.ExplosioneventListener;
import de.AS.Bau.Listener.OnInvClick;
import de.AS.Bau.Listener.PlayerPreCommandProcess;
import de.AS.Bau.Listener.SpawnEvent;
import de.AS.Bau.Listener.WorldEditEvents;
import de.AS.Bau.Listener.WorldLoad;
import de.AS.Bau.Listener.chestAndsignListener;
import de.AS.Bau.Listener.onPlayerJoin;
import de.AS.Bau.Listener.onPlayerMove;
import de.AS.Bau.Listener.onPlayerQuit;
import de.AS.Bau.Listener.onPlayerRespawn;
import de.AS.Bau.Listener.onPlayerTeleport;
import de.AS.Bau.Listener.stoplag;
import de.AS.Bau.TabCompleter.gsTC;
import de.AS.Bau.TabCompleter.tbsTC;
import de.AS.Bau.Tools.TestBlockSklave;
import de.AS.Bau.cmds.Bau;
import de.AS.Bau.cmds.chest;
import de.AS.Bau.cmds.delcon;
import de.AS.Bau.cmds.ds;
import de.AS.Bau.cmds.dt;
import de.AS.Bau.cmds.gs;
import de.AS.Bau.cmds.gui;
import de.AS.Bau.cmds.sl;
import de.AS.Bau.cmds.stats;
import de.AS.Bau.cmds.tnt;
import de.AS.Bau.utils.UndoManager;
import de.AS.Bau.utils.worldCheck;


public class Main extends JavaPlugin {
	private static Main plugin;
	public static StateFlag TntExplosion;
	private static File customConfigFile;
	private static YamlConfiguration customConfig;
	public static String prefix = "§8[§6Bau§8] §r";
	
	private static File tempAddConfigFile;
	private static YamlConfiguration tempAddConfig;
	
	public static Map<UUID,UndoManager> playersUndoManager = new HashMap<>();
@Override
public void onEnable() {
	plugin = this;
	registerCommands();
	registerListener();
	
	
	configcreate();
	createTempAddConfig();
	gs.startCheckForTempAdd();
	super.onEnable();
}
private void registerListener() {
	new onPlayerJoin(this);
	new chestAndsignListener(this);
	new OnInvClick(this);
	new WorldEditEvents(this);
	new ClickListener(this);
	new onPlayerQuit(this);
	new stoplag(this);
	new onPlayerMove(this);
	new onPlayerTeleport(this);
	new ExplosioneventListener(this);
	new SpawnEvent(this);
	new PlayerPreCommandProcess();
	new WorldLoad(this);
	new onPlayerRespawn(this);
	new worldCheck();
	
	PluginManager pm = Bukkit.getPluginManager();
	pm.registerEvents(new TestBlockSklave(), this);
	
}
private void registerCommands() {
	getCommand("gs").setExecutor(new gs());
	getCommand("gs").setTabCompleter(new gsTC());
	getCommand("tnt").setExecutor(new tnt());
	getCommand("gui").setExecutor(new gui());
	getCommand("tbs").setExecutor(new TestBlockSklave());
	getCommand("tbs").setTabCompleter(new tbsTC());
	getCommand("sl").setExecutor(new sl());
	getCommand("dt").setExecutor(new dt());
	getCommand("ds").setExecutor(new ds());
	getCommand("debugstick").setExecutor(new ds());
	getCommand("chest").setExecutor(new chest());
	getCommand("stats").setExecutor(new stats());
	getCommand("delcon").setExecutor(new delcon());
	getCommand("bau").setExecutor(new Bau());
	
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
public void configcreate() {
	customConfigFile = new File(getDataFolder(), "config.yml");
    if (!customConfigFile.exists()) {
        customConfigFile.getParentFile().mkdirs();
        saveResource("config.yml", false);
     }

    customConfig= new YamlConfiguration();
    try {
        customConfig.load(customConfigFile);
    } catch (IOException | InvalidConfigurationException e) {
        e.printStackTrace();
    }
}
public FileConfiguration getCustomConfig() {
	return customConfig;
}
public File getCustomConfigFile() {
	return customConfigFile;
}
@Override
public void onDisable() {
	for(World w:Bukkit.getServer().getWorlds()) {
		if(!w.getName().equals("world")) {
			for(Player a:w.getPlayers()) {
				a.kickPlayer("Close");
			}
			unloadWorld(w.getName());
		}
	}
	super.onDisable();
}
public void unloadWorld(String worldName) {
	Bukkit.getServer().unloadWorld(worldName, true);
	//Main main = Main.getPlugin();
	//String path = main.getCustomConfig().getString("Config.path");
	//File source = new File(path+"/"+worldName);
	/*File dir = new File(path + "/Worlds/"+worldName);
	dir.setWritable(true, false);
	dir.setExecutable(true, false);
	dir.setReadable(true, false);
	onPlayerJoin.copyFolder_raw(source, dir);
	*/
	Bukkit.getServer().getWorlds().remove(Bukkit.getServer().getWorld(worldName));
	//deleteWorld(source);
}

public boolean deleteWorld(File path) {
      if(path.exists()) {
          File files[] = path.listFiles();
          for(int i=0; i<files.length; i++) {
              if(files[i].isDirectory()) {
                  deleteWorld(files[i]);
              } else {
                  files[i].delete();
              }
          }
      }
      return(path.delete());
}

private void createTempAddConfig() {
	tempAddConfigFile = new File(getDataFolder(), "tempAddConfig.yml");
    if (!tempAddConfigFile.exists()) {
    	tempAddConfigFile.getParentFile().mkdirs();
        saveResource("tempAddConfig.yml", false);
     }

    tempAddConfig= new YamlConfiguration();
    try {
    	tempAddConfig.load(tempAddConfigFile);
    } catch (IOException | InvalidConfigurationException e) {
        e.printStackTrace();
    }
}
public FileConfiguration getTempAddConfig() {
	return tempAddConfig;
}
public File getTempAddConfigFile() {
	return tempAddConfigFile;
}


}
