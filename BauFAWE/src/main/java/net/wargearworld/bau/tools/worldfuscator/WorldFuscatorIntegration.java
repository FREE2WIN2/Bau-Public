package net.wargearworld.bau.tools.worldfuscator;

import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.pro_crafting.mc.worldfuscator.Configuration;
import com.pro_crafting.mc.worldfuscator.ConfigurationService;
import com.pro_crafting.mc.worldfuscator.engine.BlockTranslator;
import com.pro_crafting.mc.worldfuscator.engine.WorldFuscatorEngine;

public class WorldFuscatorIntegration {
    public static StateFlag worldfuscatorFlag;

    private final WGAFightGuard guard = new WGAFightGuard();
    private final WorldFuscatorEngine engine;
    private final BlockTranslator translator = new BlockTranslator();

    public WorldFuscatorIntegration(JavaPlugin plugin) {
        ConfigurationService.saveDefaultConfiguration(plugin, "worldfuscator-config.yml");
        YamlConfiguration yamlConfiguration = ConfigurationService.loadConfigurationFile(plugin, "worldfuscator-config.yml");
        Configuration configuration = new Configuration(yamlConfiguration);

        translator.updateConfiguration(configuration, guard);
        engine = new WorldFuscatorEngine(plugin, translator);
    }

    public void start() {
        engine.start();
    }
}
