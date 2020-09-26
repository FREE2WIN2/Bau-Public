package net.wargearworld.Bau.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.JsonCreater;

public class BauWorld {
	private UUID worldUUID;

	private int id;
	private HashMap<String,Plot> plots;
	private RegionManager regionManager;
	private WorldTemplate template;
	
	private File configFile;
	private FileConfiguration config;
	
	private Set<UUID> members;
	private String owner; //cpuld be an team!
	public BauWorld(int id,String owner, World world) {
		this.worldUUID = world.getUID();
		this.id = id;

		regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
		String templateName = DBConnection.getTemplate(id);
		template = WorldTemplate.getTemplate(templateName);
		
		plots = new HashMap<>();
		for(PlotPattern plotPattern:template.getPlots()) {
			plots.put(plotPattern.getID(), plotPattern.toPlot(this));
		}
		
		configFile = new File(Main.getPlugin().getDataFolder(),"worlds/" + id + "/settings.yml");
		config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		members = DBConnection.getMembers(id);
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
		p.teleport(plot.getTeleportPoint());
	}	
	public boolean isAuthorized(UUID uuid) {
		return owner.equalsIgnoreCase(uuid.toString())||members.contains(uuid);
	}

	public void showInfo(Player p) {
		boolean isOwner = owner.equals(p.getUniqueId().toString());
		Set<String> memberlist = DBConnection.getMember(p.getUniqueId().toString());
		Main.send(p, "memberListHeader", getName(p.getWorld()));
		for (String memberUUID : memberlist) {
			String memberName = DBConnection.getName(memberUUID);
			String hover = MessageHandler.getInstance().getString(p, "memberHoverRemove").replace("%r", memberName);
			JsonCreater remove = new JsonCreater("§7[§6" + memberName + "§7]");
			if (isOwner) {
				remove.addClickEvent("/gs remove " + memberName, ClickAction.SUGGEST_COMMAND).addHoverEvent(hover);
			}
			remove.send(p);
		}
		if (isOwner) {
			new JsonCreater("§a[+]§r  ").addClickEvent("/gs add ", ClickAction.SUGGEST_COMMAND)
					.addHoverEvent(MessageHandler.getInstance().getString(p, "addMemberHover")).send(p);
		}
		Main.send(p, "timeShow", p.getWorld().getTime() + "");
	}
	public String getName(World w) {
		if (w.getName().contains("test")) {
			return w.getName();
		} else {
			return DBConnection.getName(w.getName());
		}
	}

	public boolean isOwner(Player player) {
		return owner.equals(player.getUniqueId().toString());
	}
}
