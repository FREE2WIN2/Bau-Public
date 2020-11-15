package net.wargearworld.bau.config;

import net.wargearworld.bau.world.WorldTemplate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BauConfig {
    private static BauConfig instance;

    public static BauConfig getInstance() {
        return instance;
    }


    private FileConfiguration configuration;
    private File configFile;

    private int weMaxBlocksPerTick;
    private WorldTemplate defaultTemplate;
    private String schemPath;
    private int tntReloadMaxTnT;
    private Material tntReloadItem;
    private int tntReloadTimeout;
    private Material remoteDetonatorTool;
    private String cannonTimerDefaultBlock;
    private Material cannonTimerActiveBlock;
    private Material cannonTimerInactiveBlock;
    private Material cannonTimerTool;
    private int cannonTimerMaxTicks;
    private WorldTemplate testWorldsTemplates;
    private double worldprice;
    private int maxworlds;

    private Map<Integer, Sizes> sizes;
    private List<String> disallowedCommands;

    public BauConfig(FileConfiguration configuration, File file) {
        instance = this;
        this.configFile = file;
        this.configuration = configuration;
        sizes = new HashMap<>();
        loadContent();

    }

    private void loadContent() {
        this.weMaxBlocksPerTick = configuration.getInt("worldEdit.maxBlockPerSecond");
        this.defaultTemplate = WorldTemplate.getTemplate(configuration.getString("plottemplate"));
        this.schemPath = configuration.getString("schempath");
        this.tntReloadMaxTnT = configuration.getInt("tntReload.maxTnt");
        this.tntReloadItem = Material.valueOf(configuration.getString("tntReload.materialType").toUpperCase());
        this.tntReloadTimeout = configuration.getInt("tntReload.timeout");
        this.remoteDetonatorTool = Material.valueOf(configuration.getString("fernzuender").toUpperCase());
        this.cannonTimerDefaultBlock = configuration.getString("cannontimer.block.default");
        this.cannonTimerActiveBlock = Material.valueOf(configuration.getString("cannontimer.block.active").toUpperCase());
        this.cannonTimerInactiveBlock = Material.valueOf(configuration.getString("cannontimer.block.inactive").toUpperCase());
        this.cannonTimerTool = Material.valueOf(configuration.getString("cannontimer.tool").toUpperCase());
        this.cannonTimerMaxTicks = configuration.getInt("cannontimer.maxticks");
        this.testWorldsTemplates = WorldTemplate.getTemplate(configuration.getString("testworlds.template"));
        this.worldprice = configuration.getDouble("worldprice");
        this.maxworlds = configuration.getInt("maxworlds");

        ConfigurationSection sizeSection =configuration.getConfigurationSection("sizes");
        for(String configurationSection:sizeSection.getKeys(false)){
            int tier = Integer.valueOf(configurationSection);
            sizes.put(tier,new Sizes(tier,sizeSection.getConfigurationSection(configurationSection)));
        }
        this.disallowedCommands = configuration.getStringList("disallowedcommands");
    }


    public int getWeMaxBlocksPerTick() {
        return weMaxBlocksPerTick;
    }

    public WorldTemplate getDefaultTemplate() {
        return defaultTemplate;
    }

    public String getSchemPath() {
        return schemPath;
    }

    public int getTntReloadMaxTnT() {
        return tntReloadMaxTnT;
    }

    public Material getTntReloadItem() {
        return tntReloadItem;
    }

    public int getTntReloadTimeout() {
        return tntReloadTimeout;
    }

    public Material getRemoteDetonatorTool() {
        return remoteDetonatorTool;
    }

    public String getCannonTimerDefaultBlock() {
        return cannonTimerDefaultBlock;
    }

    public Material getCannonTimerActiveBlock() {
        return cannonTimerActiveBlock;
    }

    public Material getCannonTimerInactiveBlock() {
        return cannonTimerInactiveBlock;
    }

    public Material getCannonTimerTool() {
        return cannonTimerTool;
    }

    public WorldTemplate getTestWorldsTemplates() {
        return testWorldsTemplates;
    }

    public double getWorldprice() {
        return worldprice;
    }

    public int getMaxworlds() {
        return maxworlds;
    }

    public Map<Integer, Sizes> getSizes() {
        return sizes;
    }

    public Sizes getSize(int tier){
        return sizes.get(tier);
    }

    public List<String> getDisallowedCommands() {
        return disallowedCommands;
    }

    public int getCannonTimerMaxTicks() {
        return cannonTimerMaxTicks;
    }
}
