package net.wargearworld.bau.world;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.wargearworld.GUI_API.Items.CustomHead;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.HeadItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.WorldTemplate;
import net.wargearworld.db.model.WorldTemplate_;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.tools.testBlock.testBlock.Facing;
import net.wargearworld.bau.world.plot.PlotPattern;
import net.wargearworld.bau.world.plot.PlotType;
import net.wargearworld.bau.worldedit.Schematic;
import net.wargearworld.bau.utils.Loc;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class LocalWorldTemplate implements Comparable {
    private static HashMap<String, LocalWorldTemplate> templates;

    public static void load() {
        templates = new HashMap<>();
        File dir = new File(Main.getPlugin().getDataFolder(), "worldConfigs");
        for (File file : dir.listFiles()) {
            LocalWorldTemplate template = new LocalWorldTemplate(file);
            templates.put(template.getName(), template);
        }
    }

    public static LocalWorldTemplate getTemplate(String name) {
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
    private Double price;

    private LocalWorldTemplate(File configFile) {
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
        return EntityManagerExecuter.run(em -> {

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<WorldTemplate> criteriaQuery = criteriaBuilder.createQuery(WorldTemplate.class);
            Root<WorldTemplate> root = criteriaQuery.from(WorldTemplate.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(WorldTemplate_.name), name));
            Query query = em.createQuery(criteriaQuery);

            WorldTemplate template = null;
            try {
                template = (WorldTemplate) query.getSingleResult();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            long id = template.getId();
            this.price = template.getCosts();
            return id;
        });
    }

    public Double getPrice() {
        return price;
    }

    public Item getItem(UUID playerUUID) {
        return EntityManagerExecuter.run(em -> {
            WorldTemplate plotTemplate = em.find(WorldTemplate.class, id);
            Item item;
            if (plotTemplate.getIcon().getValue() != null) {
                item = new HeadItem(new CustomHead(plotTemplate.getIcon().getValue()), s -> {
                });
            } else {
                Material mat = Material.valueOf(plotTemplate.getIcon().getMaterial().toUpperCase());
                item = new DefaultItem(mat,"");
            }
            return item;
        });
    }


    @Override
    public int compareTo(Object o) {
        if (o instanceof LocalWorldTemplate) {
            return name.compareTo(((LocalWorldTemplate) o).getName());
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalWorldTemplate that = (LocalWorldTemplate) o;
        return id == that.id;
    }
}
