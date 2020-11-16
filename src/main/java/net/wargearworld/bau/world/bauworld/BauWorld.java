package net.wargearworld.bau.world.bauworld;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.listener.PlayerMovement;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.tools.cannon_timer.CannonTimer;
import net.wargearworld.bau.tools.explosion_cache.ExplosionCache;
import net.wargearworld.bau.utils.HelperMethods;
import net.wargearworld.bau.utils.MethodResult;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.gui.WorldGUI;
import net.wargearworld.bau.world.plot.Plot;
import net.wargearworld.bau.world.plot.PlotType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public abstract class BauWorld {
    private UUID worldUUID;


    private String name;
    HashMap<String, Plot> plots;
    protected RegionManager regionManager;
    WorldTemplate template;
    private File logFile;
    private File worldSettingsDir;
    ExplosionCache explosionCache;
    String worldName;

    public BauWorld(World world) {
        this.name = world.getName().split("_", 2)[1];
        this.worldUUID = world.getUID();
        this.worldName = world.getName();
        regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        worldSettingsDir = new File(Main.getPlugin().getDataFolder(), "worlds/" + world.getName());
        if (!worldSettingsDir.exists())
            worldSettingsDir.mkdirs();
        logFile = new File(worldSettingsDir, "logs.txt");
        try {
            if (!logFile.exists())
                if (!logFile.getParentFile().exists())
                    logFile.getParentFile().mkdirs();
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        explosionCache = new ExplosionCache();
    }

    public String getName() {
        return name;
    }

    public Plot getPlot(String plotID) {
        return plots.get(plotID);
    }

    public Plot getPlot(Location loc) {
        BlockVector3 pos = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
        List<String> regionsIDs = regionManager.getApplicableRegionsIDs(pos);
        if(regionsIDs.size() < 1){
            return null;
        }
        return getPlot(regionsIDs.get(0));
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
        PlayerMovement.playersLastPlot.put(p.getUniqueId(), plot.getId());
        ScoreBoardBau.cmdUpdate(p);
    }

    public abstract boolean isAuthorized(UUID uuid);

    public void showInfo(Player p){
        WorldGUI.openWorldInfo(p,this);
    }

    public abstract Collection<UUID> getMembers();

    public abstract boolean isOwner(Player player);

    public abstract void addTemp(String playerName, int time);

    public abstract MethodResult add(String playerName, Date to);

    public void addPlayerToAllRegions(UUID uuidMember) {
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

    public void removeMemberFromAllRegions(UUID uuidMember) {
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
        WorldManager.createNewWorld(this);
        return true;
    }

    public void setTemplate(String templateName) {
        WorldTemplate template = WorldTemplate.getTemplate(templateName);
        setTemplate(template);
    }

    public WorldTemplate getTemplate() {
        return template;
    }

    public abstract void setTemplate(WorldTemplate template);

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

    public int getAmountOfPlots(PlotType type) {
        int out = 0;
        for (Plot plot : plots.values()) {
            if (plot.getType() == type) {
                out++;
            }
        }
        return out;
    }

    public abstract long getId();

    public abstract void leave(Player p);

    public void unload() {
        for (Plot plot : plots.values()) {
            plot.unload(this);
        }
    }

    public abstract String rename(String newName);

    protected void setWorldName(String newName) {
        this.worldName = newName;
    }

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

    public ExplosionCache getExplosionCache() {
        return explosionCache;
    }

    public abstract String getOwner();

    public abstract boolean isMember(UUID member);

    public abstract Collection<String> getMemberNames();

    public CannonTimer getCannonTimer(Location loc) {
        Plot plot = getPlot(loc);
        if(plot == null)
            return null;
        return plot.getCannonTimer();
    }

    public File getWorldSettingsDir() {
        return worldSettingsDir;
    }

    public String getWorldName() {
        return worldName;
    }
}
