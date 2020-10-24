package net.wargearworld.bau.world;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import net.wargearworld.db.model.PlotTemplate;
import net.wargearworld.thedependencyplugin.DependencyProvider;
import org.bukkit.WorldType;
import org.bukkit.*;
import org.bukkit.entity.Player;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;

import static net.wargearworld.bau.utils.HelperMethods.isInt;

public class WorldManager {

    private static HashMap<UUID, BauWorld> worlds = new HashMap<>();

    public static BauWorld get(World world) {
        return worlds.get(world.getUID());
    }

    public static BauWorld getWorld(String name, String owner) { // name is unique!
        return get(loadWorld(name, owner));
    }

	/*public static BauWorld getWorld(String name) { // name is unique!
		String uuid = DBConnection.getUUID(name);
		return get(loadWorld(name, uuid));
	}*/

    public static BauWorld getWorld(UUID worldUUID) {
        return worlds.get(worldUUID);
    }

    /**
     * @param worldName name which has the File(owner_name)
     * @return BauWorld
     */
    public static BauWorld getWorld(String worldName) {
        String[] split = worldName.split("_");
        return getWorld(split[1], split[0]);
    }

	/*public static List<TeamWorld> getTeamWorlds(int teamID) {
		//owner = teamID
		//name?
		List<TeamWorld> out= new ArrayList<>();
		for(String worldName : DBConnection.getWorldName(teamID)) {
			BauWorld world = getWorld(worldName, teamID + "");
			if(world instanceof TeamWorld) {
				out.add((TeamWorld)world);
			}
		}
		return out;
	}*/

    public final static WorldTemplate template = WorldTemplate
            .getTemplate(Main.getPlugin().getCustomConfig().getString("plottemplate"));

    public static World loadWorld(String worldName, String owner) {
        World w = Bukkit.getWorld(owner + "_" + worldName);
        if (w == null) {
            if (isInt(owner)) {
                //TeamPlot
//				worlds.put(w.getUID(), new TeamWorld(DBConnection.getID(worldName, owner), w, Integer.getInteger(owner)));
            } else {
                UUID ownerUuid = UUID.fromString(owner);
                System.out.println(worldName + " " + owner);
                long id = DBConnection.getPlot(ownerUuid, worldName).getId();
                if (id == 0)
                    createWorldDir(worldName, owner, true);

                WorldCreator wc = new WorldCreator(owner + "_" + worldName);
                wc.type(WorldType.NORMAL);
                w = Bukkit.getServer().createWorld(wc);
                w.setStorm(false);
                w.setThundering(false);
                w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                worlds.put(w.getUID(), new PlayerWorld(id, UUID.fromString(owner), w));
            }
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
            worlds.remove(world.getUID());
        }
        Bukkit.unloadWorld(world, true);
    }

    public static void createWorldDir(String worldName, String ownerUUID, boolean plotExists) {
        // File neu = new File(path + "/Worlds/" + uuid);
        File neu = new File(Bukkit.getWorldContainer(), ownerUUID + "_" + worldName);
        neu.mkdirs();
        neu.setExecutable(true, false);
        neu.setReadable(true, false);
        neu.setWritable(true, false);
        copyFolder_raw(template.getWorldDir(), neu);
        if (!plotExists) {
            EntityManager em = DependencyProvider.getEntityManager();
            em.getTransaction().begin();
            PlotTemplate dbTemplate = em.find(PlotTemplate.class, template.getId());
            Plot plot = new Plot();
            plot.setDefault(false);
            plot.setName(worldName);
            plot.setTemplate(dbTemplate);
            plot.setOwner(em.find(net.wargearworld.db.model.Player.class, UUID.fromString(ownerUUID)));
            em.persist(plot);
            em.getTransaction().commit();
            em.close();
        }
        // worldguard regionen
        File worldGuardWorldDir = new File(Bukkit.getWorldContainer(),
                "plugins/WorldGuard/worlds/" + ownerUUID + "_" + worldName);
        copyFolder_raw(template.getWorldguardDir(), worldGuardWorldDir);
    }

    public static boolean deleteWorld(World w) {
        Bukkit.getServer().unloadWorld(w, true);
        BauWorld world = worlds.get(w);

        if (w.getWorldFolder().exists()) {
            if (deleteDir(w.getWorldFolder())) { //TODO check if world ist instacne of PlayerWorld
                if (world instanceof PlayerWorld) {
                    EntityManager em = DependencyProvider.getEntityManager();
                    Plot plot = em.find(Plot.class, world.getId());
                    for (PlotMember plotMember : plot.getMembers()) {
                        plot.removeMember(plotMember);
                    }
                    plot.setTemplate(em.find(PlotTemplate.class, template.getId()));
                    em.merge(plot);
                    em.close();
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
        createWorldDir(name, world.getOwner(), true);
        return loadWorld(name, world.getOwner());

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


}
