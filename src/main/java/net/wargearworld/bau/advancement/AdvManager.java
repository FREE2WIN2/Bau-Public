package net.wargearworld.bau.advancement;

import net.wargearworld.bau.advancement.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvManager {

    public static void start(JavaPlugin plugin){
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new AdvCannonReload(),plugin);
        pm.registerEvents(new AdvCannonTimer(),plugin);
        pm.registerEvents(new AdvEnter(),plugin);
        pm.registerEvents(new AdvSaveTestBlock(),plugin);
        pm.registerEvents(new AdvSwitchTemplate(),plugin);
        pm.registerEvents(new AdvTestBlockEditor(),plugin);
    }
}
