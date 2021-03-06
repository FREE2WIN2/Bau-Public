package net.wargearworld.bau.world;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.World_;
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
        if (world == null)
            return null;
        return worlds.get(world.getUID());
    }

    public static BauWorld getPlayerWorld(String name, UUID owner) { // name is unique!
        return get(loadWorld(name, owner));
    }

    public static BauWorld getTeamWorld(Team team) { // name is unique!
        return get(loadWorld(team));
    }

    public static boolean containsTeamWorld(Team team) {
        return Bukkit.getWorld("team_" + team.getId()) != null;
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
        return getPlayerWorld(split[1], UUID.fromString(split[0]));
    }

    public static World loadWorld(String worldName, UUID owner) {
        String bukkitWorldName = owner + "_" + worldName;
        World w = Bukkit.getWorld(bukkitWorldName);
        if (w == null) {
            net.wargearworld.db.model.World dbWorld = readPlot(owner, worldName);
            if (dbWorld == null)
                return null;
            long id = dbWorld.getId();
            if (id == 0)
                return null;
            if (!new File(Bukkit.getWorldContainer(), bukkitWorldName).exists()) {
                EntityManagerExecuter.run(em -> {
                    createWorldDir(bukkitWorldName, LocalWorldTemplate.getTemplate(em.find(net.wargearworld.db.model.World.class, id).getTemplate().getName()));
                });
            }

            WorldCreator wc = new WorldCreator(bukkitWorldName);
            wc.type(WorldType.NORMAL);
            w = Bukkit.getServer().createWorld(wc);
            w.setStorm(false);
            w.setThundering(false);
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            worlds.put(w.getUID(), new PlayerWorld(id, owner, w));
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
        if (!world.getName().contains("test") && !world.getName().contains("world") && world.getName().length() != 5) {
            BauWorld bauWorld = worlds.get(world.getUID());
            if (bauWorld != null) {
                worlds.get(world.getUID()).unload();
                worlds.remove(world.getUID());
            }
        }
        Bukkit.unloadWorld(world, true);
    }

    public static void createWorldDir(String worldName, LocalWorldTemplate localWorldTemplate) {
        // File neu = new File(path + "/Worlds/" + uuid);
        File neu = new File(Bukkit.getWorldContainer(), worldName);
        neu.mkdirs();
        neu.setExecutable(true, false);
        neu.setReadable(true, false);
        neu.setWritable(true, false);
        copyFolder_raw(localWorldTemplate.getWorldDir(), neu);
        // worldguard regionen
        File worldGuardWorldDir = new File(Bukkit.getWorldContainer(),
                "plugins/WorldGuard/worlds/" + worldName);
        copyFolder_raw(localWorldTemplate.getWorldguardDir(), worldGuardWorldDir);
    }

    public static boolean deleteWorld(World w) {
        BauWorld world = worlds.get(w);
        Bukkit.getServer().unloadWorld(w, false);

        if (w.getWorldFolder().exists()) {
            if (deleteDir(w.getWorldFolder())) { //TODO check if world ist instacne of PlayerWorld
                if (world != null) {
                    worlds.remove(w.getUID());
                }

                File file = new File(Bukkit.getWorldContainer(), "plugins/WorldGuard/worlds/" + w.getName());
                deleteDir(file);

                File persistDir = new File(Main.getPlugin().getDataFolder(), "worlds/" + w.getName());
                deleteDir(persistDir, "ser");
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

    public static boolean deleteDir(File path, String... strings) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDir(files[i], strings);
                } else {
                    boolean deleted = false;
                    for (String end : strings) {
                        if (files[i].getName().endsWith(end)) {
                            files[i].delete();
                            deleted = true;
                            break;
                        }
                    }
                    if (!deleted && strings.length == 0) {
                        files[i].delete();
                    }
                }
            }
        }
        if (path.listFiles().length == 0) {
            return (path.delete());
        } else {
            return true;
        }
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

    public static void createNewWorld(BauWorld world) {
        World oldWorld = world.getWorld();
        for (Player p : oldWorld.getPlayers()) {
            p.kickPlayer("GS DELETE");
        }
        deleteWorld(oldWorld);
    }

    public static void renameWorld(BauWorld world, String newName) {
        World oldWorld = world.getWorld();
        world.unload();
        Bukkit.getServer().unloadWorld(oldWorld, true);
        Bukkit.getServer().unloadWorld(oldWorld, false);
        worlds.remove(oldWorld.getUID());
        for (Player p : oldWorld.getPlayers()) {
            p.kickPlayer("GS Rename!");
        }
        String newWorldName = world.rename(newName);
        File newWorld = new File(Bukkit.getWorldContainer(), newWorldName);
        oldWorld.getWorldFolder().renameTo(newWorld);
        File uid = new File(newWorld, "uid.dat");
        if (uid.exists()) uid.delete();

        File session = new File(newWorld, "session.lock");
        if (session.exists()) session.delete();

        new File(Main.getPlugin().getDataFolder(), "worlds/" + oldWorld.getName()).renameTo(new File(Main.getPlugin().getDataFolder(), "worlds/" + newWorldName));
        //delete

        File worldGuardDir = new File(Bukkit.getWorldContainer(), "plugins/WorldGuard/worlds/" + oldWorld.getName());
        worldGuardDir.renameTo(new File(Bukkit.getWorldContainer(),
                "plugins/WorldGuard/worlds/" + newWorldName));
        File persistDir = new File(Main.getPlugin().getDataFolder(), "worlds/" + oldWorld.getName());
        persistDir.renameTo(new File(Main.getPlugin().getDataFolder(), "worlds/" + newWorldName));
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

    public static net.wargearworld.db.model.World readPlot(UUID owner, String name) {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<net.wargearworld.db.model.World > criteriaQuery = criteriaBuilder.createQuery(net.wargearworld.db.model.World .class);
            Root<net.wargearworld.db.model.World > root = criteriaQuery.from(net.wargearworld.db.model.World .class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(World_.name), name), criteriaBuilder.equal(root.get(World_.owner), em.find(net.wargearworld.db.model.Player.class, owner)));
            Query query = em.createQuery(criteriaQuery);
            net.wargearworld.db.model.World  plot = null;
            try {
                plot = (net.wargearworld.db.model.World ) query.getSingleResult();
            } catch (Exception exception) {
            }
            return plot;
        });
    }


    public static World loadDefaultWorld(UUID uuid) {
        String worldName = PlayerDAO.getDefaultWorldName(uuid);
        if (worldName == null)
            return null;
        return loadWorld(worldName, uuid);
    }



}
