package net.wargearworld.bau.world;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import net.wargearworld.bau.world.plots.PlotType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.listener.onPlayerMove;
import net.wargearworld.bau.world.plots.Plot;
import net.wargearworld.bau.utils.HelperMethods;
import net.wargearworld.bau.utils.MethodResult;

public abstract class BauWorld {
    private UUID worldUUID;


    private String name;
    HashMap<String, Plot> plots;
    protected RegionManager regionManager;
    private WorldTemplate template;
    private File logFile;
    public BauWorld(World world) {
        this.name = world.getName().split("_", 2)[1];
        this.worldUUID = world.getUID();
        regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        logFile = new File(Main.getPlugin().getDataFolder(), "worlds/" + world.getName() + "/logs.txt");
        try {
            if (!logFile.exists())
                if (!logFile.getParentFile().exists())
                    logFile.getParentFile().mkdirs();
            logFile.createNewFile();
//			config.load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public Plot getPlot(String plotID) {
        return plots.get(plotID);
    }

    public Plot getPlot(Location loc) {
        BlockVector3 pos = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
        return getPlot(regionManager.getApplicableRegionsIDs(pos).get(0));
    }

    public World getWorld() {
        return Bukkit.getWorld(worldUUID);
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public void spawn(Player p) {
        Plot plot = plots.get(template.getSpawnPlotID());
        p.teleport(plot.getTeleportPoint());
        onPlayerMove.playersLastPlot.put(p.getUniqueId(), plot.getId());
    }

    public abstract boolean isAuthorized(UUID uuid);

    public abstract void showInfo(Player p);

    public abstract boolean isOwner(Player player);

    public abstract void addTemp(String playerName, int time);

    public abstract MethodResult add(String playerName, Date to);

    protected void addPlayerToAllRegions(UUID uuidMember) {
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            DefaultDomain members = region.getMembers();
            members.addPlayer(uuidMember);
            region.setMembers(members);
        }
        try {
            regionManager.saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    protected void removeMemberFromAllRegions(UUID uuidMember) {
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            DefaultDomain members = region.getMembers();
            members.removePlayer(uuidMember);
            region.setMembers(members);
        }
        try {
            regionManager.saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    protected void log(WorldAction action, String... args) {
        String message = HelperMethods.getTime() + action.getMessage();
        for (String a : args) {
            message = message.replaceFirst("%r", a);
        }

        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void checkForTimeoutMembership();

    public void setTime(Integer time) {
        getWorld().setTime(time);
    }

    public abstract void removeMember(UUID member);

    public boolean newWorld() {
        removeAllMembersFromRegions();
        regionManager = null;
        World world = WorldManager.createNewWorld(this);
        this.worldUUID = world.getUID();
        this.name = world.getName();

        regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        return true;
    }

    public abstract void setTemplate(String templateName);

    public WorldTemplate getTemplate() {
        return template;
    }

    protected void setTemplate(WorldTemplate template) {
        this.template = template;
    }

    public abstract void removeAllMembersFromRegions();


    public abstract void addAllMembersToRegions();

    public Collection<Plot> getPlots(PlotType type) {
        List<Plot> out = new ArrayList<>();
        for (Plot plot : plots.values()) {
            if (plot.getType() == type) {
                out.add(plot);
            }
        }
        return out;
    }

    public int getAmountOfPlots(PlotType type){
        int out = 0;
        for (Plot plot : plots.values()) {
            if (plot.getType() == type) {
                out ++;
            }
        }
        return out;
    }

    public abstract long getId();

    protected enum WorldAction {
        ADD, ADDTEMP, REMOVE, NEW, DELETE;

        protected String getMessage() {
            switch (this) {
                case ADD:
                    return "add %r(Name: %r)";
                case ADDTEMP:
                    return ADD.getMessage() + " for %r hours";
                case DELETE:
                    return "deleted";
                case NEW:
                    return "NEW GS (Removed everyone)";
                case REMOVE:
                    return "removed %r(Name: %r)";
                default:
                    return "";
            }

        }
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }

    protected abstract String getOwner();

    public abstract boolean isMember(UUID member);
}
