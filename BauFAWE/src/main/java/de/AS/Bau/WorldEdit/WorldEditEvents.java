package de.AS.Bau.WorldEdit;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.utils.CoordGetter;

public class WorldEditEvents implements Listener {
	public WorldEditEvents(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
		String command = e.getMessage();
		Player p = e.getPlayer();
		String[] args = command.split(" ");


		if (command.startsWith("//stack")) {
			
			if(args.length==2) {
				WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
				int multiplier = Integer.parseInt(args[1]);
				try {
					Region sel = wep.getSession(p).getSelection(BukkitAdapter.adapt(p.getWorld()));
					
					BlockFace bF = getFacing(p.getEyeLocation().getPitch(),p.getEyeLocation().getYaw());
					int zmin=sel.getMinimumPoint().getBlockZ();
					int zmax=sel.getMaximumPoint().getBlockZ();
					
					int ymin=sel.getMinimumPoint().getBlockY();
					int ymax=sel.getMaximumPoint().getBlockY();
					
					int xmin=sel.getMinimumPoint().getBlockX();
					int xmax=sel.getMaximumPoint().getBlockX();
						
					int ydur = ymax - ymin+1;
					int xdur = xmax - xmin+1;
					int zdur = zmax - zmin+1;
					
					BlockVector3 min = CoordGetter.getMinWorldVector();
					BlockVector3 max = CoordGetter.getMaxWorldVector();
					switch(bF){
					case NORTH:
						//-z
						int beruehrpktN=-(zdur*multiplier)+zmin;
						if(beruehrpktN<min.getZ()) {
							e.setCancelled(true);
						}
						break;
					case EAST:
						//+x
						int beruehrpktE=(xdur*multiplier)+xmax;
						if(beruehrpktE>max.getX()) {
							e.setCancelled(true);
						}
						break;
					case SOUTH:
						//+z
						int beruehrpktS=(zdur*multiplier)+zmax;
						if(beruehrpktS>max.getZ()) {
							e.setCancelled(true);
						}
						break;
					case WEST:
						//-x
						int beruehrpktW=-(xdur*multiplier)+xmin;
						if(beruehrpktW<min.getX()) {
							e.setCancelled(true);
						}
						break;
					case NORTH_EAST:
						//-z+x
						int beruehrpktNEX=(xdur*multiplier)+xmax;
						int beruehrpktNEZ=-(zdur*multiplier)+zmin;
						if(beruehrpktNEX>=max.getX()||beruehrpktNEZ<min.getZ()) {
							e.setCancelled(true);
						}
						break;
					case NORTH_WEST:
						//-z-x
						int beruehrpktNWX=-(xdur*multiplier)+xmin;
						int beruehrpktNWZ=-(zdur*multiplier)+zmin;
						if(beruehrpktNWX<min.getX()||beruehrpktNWZ<min.getBlockZ()) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_EAST:
						//+z+x
						int beruehrpktSEX=(xdur*multiplier)+xmax;
						int beruehrpktSEZ=(zdur*multiplier)+zmax;
						if(beruehrpktSEX>max.getX()||beruehrpktSEZ>max.getZ()) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_WEST:
						//+z-x
						int beruehrpktSWX=-(xdur*multiplier)+xmin;
						int beruehrpktSWZ=(zdur*multiplier)+zmax;
						if(beruehrpktSWX>max.getX()||beruehrpktSWZ>max.getZ()) {
							e.setCancelled(true);
						}
						break;
					case DOWN:
						//-y
						int beruehrpktD=-(ydur*multiplier)+ymin;
						if(beruehrpktD<min.getY()) {
							e.setCancelled(true);
						}
						break;
					case UP:
						//+y
						int beruehrpktU=(ydur*multiplier)+ymax;
						if(beruehrpktU>max.getY()) {
							e.setCancelled(true);
						}
						break;
					default:
						break;
					}
				} catch (IncompleteRegionException e1) {
					e1.printStackTrace();
				}
				
			}
			if(e.isCancelled()) {
				p.sendMessage(Main.prefix +StringGetterBau.getString(p,"noWE"));
			}
			
		} else if (command.startsWith("//move")) {
			if(args.length==2) {
				WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
				int multiplier = Integer.parseInt(args[1]);
				try {
					Region sel = wep.getSession(p).getSelection(BukkitAdapter.adapt(p.getWorld()));
					BlockFace bF = p.getFacing();
					int zmin=sel.getMinimumPoint().getBlockZ();
					int zmax=sel.getMaximumPoint().getBlockZ();
					
					int ymin=sel.getMinimumPoint().getBlockY();
					int ymax=sel.getMaximumPoint().getBlockY();
					
					int xmin=sel.getMinimumPoint().getBlockX();
					int xmax=sel.getMaximumPoint().getBlockX();
					
					int ydur = ymax - ymin;
					int xdur = xmax - xmin;
					int zdur = zmax - zmin;
					
					
					BlockVector3 min = CoordGetter.getMinWorldVector();
					BlockVector3 max = CoordGetter.getMaxWorldVector();
					switch(bF){
					case NORTH:
						//-z
						int beruehrpktN=-(zdur-multiplier)+zmin;
						if(beruehrpktN<min.getZ()) {
							e.setCancelled(true);
						}
						break;
					case EAST:
						//+x
						int beruehrpktE=(xdur+multiplier)+xmax;
						if(beruehrpktE>max.getX()) {
							e.setCancelled(true);
						}
						break;
					case SOUTH:
						//+z
						int beruehrpktS=(zdur+multiplier)+zmax;
						if(beruehrpktS>max.getZ()) {
							e.setCancelled(true);
						}
						break;
					case WEST:
						//-x
						int beruehrpktW=-(xdur-multiplier)+xmin;
						if(beruehrpktW<min.getX()) {
							e.setCancelled(true);
						}
						break;
					case NORTH_EAST:
						//-z+x
						int beruehrpktNEX=(xdur+multiplier)+xmax;
						int beruehrpktNEZ=-(zdur-multiplier)+zmin;
						if(beruehrpktNEX>max.getX()||beruehrpktNEZ<min.getZ()) {
							e.setCancelled(true);
						}
						break;
					case NORTH_WEST:
						//-z-x
						int beruehrpktNWX=-(xdur-multiplier)+xmin;
						int beruehrpktNWZ=-(zdur-multiplier)+zmin;
						if(beruehrpktNWX<min.getX()||beruehrpktNWZ<min.getZ()) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_EAST:
						//+z+x
						int beruehrpktSEX=(xdur+multiplier)+xmax;
						int beruehrpktSEZ=(zdur+multiplier)+zmax;
						if(beruehrpktSEX>max.getX()||beruehrpktSEZ>max.getZ()) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_WEST:
						//+z-x
						int beruehrpktSWX=(xdur+multiplier)+xmax;
						int beruehrpktSWZ=(zdur+multiplier)+zmax;
						if(beruehrpktSWX>max.getX()||beruehrpktSWZ>max.getZ()) {
							e.setCancelled(true);
						}
						break;
					case DOWN:
						//-y
						int beruehrpktD=-(ydur-multiplier)+ymin;
						if(beruehrpktD<min.getY()) {
							e.setCancelled(true);
						}
						break;
					case UP:
						//+y
						int beruehrpktU=(ydur+multiplier)+ymax;
						if(beruehrpktU>max.getY()) {
							e.setCancelled(true);
						}
						break;
					default:
						break;
					}
				} catch (IncompleteRegionException e1) {
					e1.printStackTrace();
				}
				
			}
			if(e.isCancelled()) {
				p.sendMessage(Main.prefix +StringGetterBau.getString(p,"noWE"));
			}
			
		}
		

	}

	private BlockFace getFacing(float pitch, float yaw) {
		//Pitch: oben/Unten
		//Yaw: nord, ost, süd, west..
		if(yaw < 0) {
			yaw = (yaw * (-1));
		}
		if(pitch < -67.5) {
			return BlockFace.UP;
		}else if(pitch > 67.5) {
			return BlockFace.DOWN;
		}
		if(yaw<22.5 ||yaw>337.5) {
			return BlockFace.SOUTH;
		}else if(yaw>292.5){
			return BlockFace.SOUTH_WEST;
		}else if(yaw>247.5) {
			return BlockFace.WEST;
		}else if(yaw>202.5){
			return BlockFace.NORTH_WEST;
		}else if(yaw>157.5){
			return BlockFace.NORTH;
		}else if(yaw>112.5){
			return BlockFace.NORTH_EAST;
		}else if(yaw>67.5){
			return BlockFace.EAST;
		}else if(yaw>22.5){
			return BlockFace.SOUTH_EAST;
		}
		return null;
	}
}
