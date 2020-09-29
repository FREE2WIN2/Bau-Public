package net.wargearworld.Bau.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Listener.onPlayerMove;
import net.wargearworld.Bau.World.Plots.Plot;
import net.wargearworld.Bau.World.Plots.PlotPattern;
import net.wargearworld.Bau.utils.HelperMethods;
import net.wargearworld.Bau.utils.MethodResult;

public abstract class BauWorld {
	private UUID worldUUID;

	private int id;
	private String name;
	private HashMap<String, Plot> plots;
	private RegionManager regionManager;
	private WorldTemplate template;
	private File logFile;
	private Map<UUID, Date> members;

	public BauWorld(int id, World world) {
		System.out.println(world);
		this.name = world.getName().split("_",2)[1];
		System.out.println(name);
		this.id = id;
		this.worldUUID = world.getUID();
		regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
		String templateName = DBConnection.getTemplate(id);
		template = WorldTemplate.getTemplate(templateName);

		plots = new HashMap<>();
		for (PlotPattern plotPattern : template.getPlots()) {
			plots.put(plotPattern.getID(), plotPattern.toPlot(this));
		}

		
		logFile = new File(Main.getPlugin().getDataFolder(),"worlds/" + world.getName() + "/logs.txt");
		try {
			if (!logFile.exists())
				if(!logFile.getParentFile().exists())
					logFile.getParentFile().mkdirs();
				logFile.createNewFile();
//			config.load(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		members = loadMembers();
		checkForTimeoutMembership();
	}

	protected abstract Map<UUID, Date> loadMembers();

	
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
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
		System.out.println(plot.getTeleportPoint());
		p.teleport(plot.getTeleportPoint());
		onPlayerMove.playersLastPlot.put(p.getUniqueId(), plot.getId());
	}

	public abstract boolean isAuthorized(UUID uuid);
		
	protected Map<UUID, Date> getMembers() {
		return members;
	}

	public abstract void showInfo(Player p);


	public String getName(World w) {
		//TODO woaders
		if (w.getName().contains("test")) {
			return name;
		} else {
			return DBConnection.getName(name );
		}
	}

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
			message = message.replace("%r", a);
		}

		try (FileWriter writer = new FileWriter(logFile, true)) {
			writer.write(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkForTimeoutMembership() {
		Date date = new Date();
		for (Entry<UUID, Date> entry : members.entrySet()) {
			if (entry.getValue() != null && entry.getValue().after(date)) {
				removeMember(entry.getKey());
			}
		}
	}

	public void setTime(Integer time) {
		getWorld().setTime(time);
	}

	public abstract void removeMember(UUID member);

	public boolean newWorld() {
		removeAllMembers();
		regionManager = null;
		World world = WorldManager.createNewWorld(this);
		this.worldUUID = world.getUID();
		this.name = world.getName();
		
		regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
		return true;
	}
	public void setTemplate(String templateName) {
		this.template = WorldTemplate.getTemplate(templateName);
		DBConnection.setTemplate(id,template);
	}

	public void removeAllMembers() {
		for (UUID member : members.keySet()) {
			for (ProtectedRegion region : regionManager.getRegions().values()) {
				DefaultDomain members = region.getMembers();
				members.removePlayer(member);
				region.setMembers(members);
			}
		}
		try {
			regionManager.saveChanges();
		} catch (StorageException e) {
			e.printStackTrace();
		}
	}

	public void addAllMembers() {
		for (UUID member : members.keySet()) {
			for (ProtectedRegion region : regionManager.getRegions().values()) {
				DefaultDomain members = region.getMembers();
				members.addPlayer(member);
				region.setMembers(members);
			}
		}
		try {
			regionManager.saveChanges();
		} catch (StorageException e) {
			e.printStackTrace();
		}
	}
	
	protected enum WorldAction {
		ADD, ADDTEMP, REMOVE, NEW, DELETE;

		protected String getMessage() {
			switch (this) {
			case ADD:
				return "add %r(Name:%r)";
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

}
