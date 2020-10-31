package net.wargearworld.bau.world;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.db.model.PlotTemplate;
import net.wargearworld.db.model.PlotTemplate_;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Facing;
import net.wargearworld.bau.world.plot.PlotPattern;
import net.wargearworld.bau.world.plot.PlotType;
import net.wargearworld.bau.worldedit.Schematic;
import net.wargearworld.bau.utils.Loc;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class WorldTemplate {
    private static HashMap<String, WorldTemplate> templates;

    public static void load() {
        templates = new HashMap<>();
        File dir = new File(Main.getPlugin().getDataFolder(), "worldConfigs");
        for (File file : dir.listFiles()) {
            WorldTemplate template = new WorldTemplate(file);
            templates.put(template.getName(), template);
        }
    }

    public static WorldTemplate getTemplate(String name) {
        if (templates == null)
            load();
        return templates.get(name);
    }


    private File configFile;
    private FileConfiguration config;
    private File worldguardDir;
    private File worldDir;
    private String name;
    private long id;
    private List<PlotPattern> plots;
    private String spawnPlotID;

    private WorldTemplate(File configFile) {
        name = configFile.getName().split("\\.")[0];
        worldguardDir = new File(Bukkit.getPluginManager().getPlugin("WorldGuard").getDataFolder(), "worlds/" + name);
        worldDir = new File(Bukkit.getWorldContainer(), name);
        this.configFile = configFile;
        config = new YamlConfiguration();
        try {
            config.load(this.configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        this.id = readTemplateId(name);
    }

    public String getName() {
        return name;
    }

    public File getWorldDir() {
        return worldDir;
    }

    public File getWorldguardDir() {
        return worldguardDir;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public List<PlotPattern> getPlots() {
        if (plots == null) {
            plots = new ArrayList<>();
            for (String id : config.getConfigurationSection("plots").getValues(false).keySet()) {
                Loc middleNorth = Loc.getByString(config.getString("middle." + id));
                PlotType type = getType(id);
                Schematic ground = new Schematic("/TestBlockSklave", config.getString("plotreset.schemfiles." + id) + ".schem", Facing.NORTH);
                plots.add(new PlotPattern(middleNorth, ground, type, id));
            }
        }
        return plots;
    }

    public String getSpawnPlotID() {
        if (spawnPlotID == null) {
            spawnPlotID = config.getString("spawn");
        }
        return spawnPlotID;
    }

    public PlotType getType(String plotID) {
        return PlotType.valueOf(config.getString("plots." + plotID).split(" ")[0].toUpperCase());
    }

    public int getTier(String plotID) {
        return Integer.parseInt(config.getString("plots." + plotID).split(" ")[1]);
    }

    public long getId() {
        return id;
    }

    private long readTemplateId(String name) {
        EntityManager em = CDI.current().select(EntityManager.class).get();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<PlotTemplate> criteriaQuery = criteriaBuilder.createQuery(PlotTemplate.class);
        Root<PlotTemplate> root = criteriaQuery.from(PlotTemplate.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(PlotTemplate_.name), name));
        Query query = em.createQuery(criteriaQuery);

        PlotTemplate template = null;
        try {
            template = (PlotTemplate) query.getSingleResult();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        long id = template.getId();
        return id;
    }
}
