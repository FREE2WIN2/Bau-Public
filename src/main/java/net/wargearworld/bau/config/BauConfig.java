package net.wargearworld.bau.config;

import net.wargearworld.bau.world.WorldTemplate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class BauConfig {

    private int weMaxBlocksPerSecond;
    private WorldTemplate defaultTemplate;
    private String schemPath;
    private int tntReloadMaxTnT;
    private Material tntReloadItem;
    private int tntReloadTimeout;
    private Material remoteDetonatorTool;
    public BauConfig(FileConfiguration configuration) {

    }
}
