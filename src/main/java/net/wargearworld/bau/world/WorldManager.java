package net.wargearworld.bau.world;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import net.wargearworld.db.model.PlotTemplate;
import net.wargearworld.db.model.Plot_;
import org.bukkit.WorldType;
import org.bukkit.*;
import org.bukkit.entity.Player;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;

public class WorldManager {

    private static HashMap<UUID, BauWorld> worlds = new HashMap<>();

    public static BauWorld get(World world) {
        return worlds.get(world.getUID());
    }

    public static BauWorld getPlayerWorld(String name, String owner) { // name is unique!
        return get(loadWorld(name, owner));
    }

    public static BauWorld getTeamWorld(Team team) { // name is unique!
        return get(loadWorld(team));
    }

    public static BauWorld getWorld(UUID worldUUID) {
        return worlds.get(worldUUID);
    }

    /**
     * @param worldName name which has the File(owner_name)
     * @return BauWorld
     */
    public static BauWorld getPlayerWorld(String worldName) {//UUID_Spielername
        String[] split = worldName.split("_");
        return getPlayerWorld(split[1], split[0]);
    }

    public final static WorldTemplate template = WorldTemplate
            .getTemplate(Main.getPlugin().getCustomConfig().getString("plottemplate"));


    public static World loadWorld(String worldName, String owner) {
        World w = Bukkit.getWorld(owner + "_" + worldName);
        if (w == null) {
            UUID ownerUuid = UUID.fromString(owner);
            System.out.println(worldName + " " + owner);
            long id = readPlot(ownerUuid, worldName).getId();
            if (id == 0)
                createWorldDir(owner + "_" + worldName, template);

            WorldCreator wc = new WorldCreator(owner + "_" + worldName);
            wc.type(WorldType.NORMAL);
            w = Bukkit.getServer().createWorld(wc);
            w.setStorm(false);
            w.setThundering(false);
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            worlds.put(w.getUID(), new PlayerWorld(id, UUID.fromString(owner), w));
        }
        return w;
    }

    private static World loadWorld(Team team) {
        World w = Bukkit.getWorld("team_" + team.getId());
        if (w == null) {
            if (!new File(Bukkit.getWorldContainer(), "team_" + team.getId()).exists()) {
                createWorldDir("team_" + team.getId(), team.getTemplate());
            }
            WorldCreator wc = new WorldCreator("team_" + team.getId());
            wc.type(WorldType.NORMAL);
            w = Bukkit.getServer().createWorld(wc);
            w.setStorm(false);
            w.setThundering(false);
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            worlds.put(w.getUID(), new TeamWorld(team, w));
        }
        return w;
    }

    public static void unloadWorld(String worldName) {
        undloadWorld(Bukkit.getWorld(worldName));
    }

    public static void undloadWorld(World world) {
        if (world == null) {
            return;
        }
        if (!world.getName().contains("test") && !world.getName().contains("world")) {
            worlds.get(world.getUID()).unload();
            worlds.remove(world.getUID());
        }
        Bukkit.unloadWorld(world, true);
    }

    public static void createWorldDir(String worldName, WorldTemplate worldTemplate) {
        // File neu = new File(path + "/Worlds/" + uuid);
        File neu = new File(Bukkit.getWorldContainer(), worldName);
        neu.mkdirs();
        neu.setExecutable(true, false);
        neu.setReadable(true, false);
        neu.setWritable(true, false);
        copyFolder_raw(worldTemplate.getWorldDir(), neu);
        // worldguard regionen
        File worldGuardWorldDir = new File(Bukkit.getWorldContainer(),
                "plugins/WorldGuard/worlds/" + worldName);
        copyFolder_raw(worldTemplate.getWorldguardDir(), worldGuardWorldDir);
    }

    public static boolean deleteWorld(World w) {
        Bukkit.getServer().unloadWorld(w, true);
        BauWorld world = worlds.get(w);

        if (w.getWorldFolder().exists()) {
            if (deleteDir(w.getWorldFolder())) { //TODO check if world ist instacne of PlayerWorld
                if (world instanceof PlayerWorld) {
                    EntityManagerExecuter.run(em -> {
                        Plot plot = em.find(Plot.class, world.getId());
                        for (PlotMember plotMember : plot.getMembers()) {
                            plot.removeMember(plotMember);
                        }
                        plot.setTemplate(em.find(PlotTemplate.class, template.getId()));
                        em.merge(plot);
                    });

                }
                if (!w.getName().contains("test") && !w.getName().contains("world")) {
                    worlds.remove(w.getUID());
                }

                File file = new File(Bukkit.getWorldContainer(), "plugins/WorldGuard/worlds/" + w.getName());
                file.delete();
                return true;
            }
        }
        return false;

    }

    public static void copyFolder_raw(File sourceFolder, File destinationfolder) {
        if (sourceFolder.isDirectory()) {
            // Verify if destinationFolder is already present, if not then create it
            if (!destinationfolder.exists()) {
                destinationfolder.mkdir();
            }
            // Get all files from source directory
            String files[] = sourceFolder.list();
            // Iterate over all files and copy them to destinationFolder one by one
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationfolder, file);
                // Recursive function call
                copyFolder_raw(srcFile, destFile);
            }
        } else {
            // Copy the file content from one place to another
            try {
                Files.copy(sourceFolder.toPath(), destinationfolder.toPath(), StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean deleteDir(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDir(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static void checkForWorldsToUnload() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            for (World w : Bukkit.getServer().getWorlds()) {
                if (w.getPlayers().size() == 0 && !w.getName().equals("world")) {
                    undloadWorld(w);
                }
            }
        }, 20 * 60, 20 * 60);
    }

    public static World createNewWorld(BauWorld world) {
        world.setTemplate(template.getName());
        String name = world.getName();
        World oldWorld = loadWorld(name, world.getOwner());
        for (Player p : oldWorld.getPlayers()) {
            p.kickPlayer("GS DELETE");
        }
        deleteWorld(oldWorld);
        createWorldDir(world.getWorldName(), world.getTemplate());
        return loadWorld(name, world.getOwner());

    }

    public static World renameWorld(BauWorld world, String newName) {
        world.setTemplate(template.getName());
        World oldWorld = loadWorld(world);
        worlds.remove(oldWorld.getUID());
        for (Player p : oldWorld.getPlayers()) {
            p.kickPlayer("GS Rename!");
        }
        world.rename(newName);
        deleteWorld(oldWorld);
        copyFolder_raw(new File(Bukkit.getWorldContainer(), world.getWorldName()), new File(Bukkit.getWorldContainer(), world.rename(newName)));
        String newWorldName = world.getWorldName();
        // worldguard regionen
        File worldGuardWorldDir = new File(Bukkit.getWorldContainer(),
                "plugins/WorldGuard/worlds/" + newWorldName);
        copyFolder_raw(world.getTemplate().getWorldguardDir(), worldGuardWorldDir);
        return loadWorld(newWorldName, world.getOwner());

    }

    private static World loadWorld(BauWorld world) {
        return Bukkit.getWorld(world.getWorldUUID());
    }

    public static void startCheckForTempAddRemoves() {
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Runnable() {

            @Override
            public void run() {
                for (BauWorld world : worlds.values()) {
                    world.checkForTimeoutMembership();
                }
            }
        }, 0, 1 * 20 * 60);

    }

    private static Plot readPlot(UUID owner, String name) {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Plot> criteriaQuery = criteriaBuilder.createQuery(Plot.class);
            Root<Plot> root = criteriaQuery.from(Plot.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(Plot_.name), name), criteriaBuilder.equal(root.get(Plot_.owner), BauPlayer.getBauPlayer(owner).getDbPlayer()));
            Query query = em.createQuery(criteriaQuery);

            Plot plot = null;
            try {
                plot = (Plot) query.getSingleResult();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return plot;
        });
    }


}
