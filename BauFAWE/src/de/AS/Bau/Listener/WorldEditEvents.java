package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;

public class WorldEditEvents implements Listener {
	public WorldEditEvents(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
		String command = e.getMessage();
		Player p = e.getPlayer();
		String[] args = command.split(" ");
		
		if(command.equalsIgnoreCase("//rotate")) {
			e.setCancelled(true);
			rotateClipboard(p);
		}
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
					switch(bF){
					case NORTH:
						//-z
						int beruehrpktN=-(zdur*multiplier)+zmin;
						if(beruehrpktN<=-66) {
							e.setCancelled(true);
						}
						break;
					case EAST:
						//+x
						int beruehrpktE=(xdur*multiplier)+xmax;
						if(beruehrpktE>=-65) {
							e.setCancelled(true);
						}
						break;
					case SOUTH:
						//+z
						int beruehrpktS=(zdur*multiplier)+zmax;
						if(beruehrpktS>=99) {
							e.setCancelled(true);
						}
						break;
					case WEST:
						//-x
						int beruehrpktW=-(xdur*multiplier)+xmin;
						if(beruehrpktW<=-371) {
							e.setCancelled(true);
						}
						break;
					case NORTH_EAST:
						//-z+x
						int beruehrpktNEX=(xdur*multiplier)+xmax;
						int beruehrpktNEZ=-(zdur*multiplier)+zmin;
						if(beruehrpktNEX>=-65||beruehrpktNEZ<=-66) {
							e.setCancelled(true);
						}
						break;
					case NORTH_WEST:
						//-z-x
						int beruehrpktNWX=-(xdur*multiplier)+xmin;
						int beruehrpktNWZ=-(zdur*multiplier)+zmin;
						if(beruehrpktNWX<=-371||beruehrpktNWZ<=-66) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_EAST:
						//+z+x
						int beruehrpktSEX=(xdur*multiplier)+xmax;
						int beruehrpktSEZ=(zdur*multiplier)+zmax;
						if(beruehrpktSEX>=-65||beruehrpktSEZ>=99) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_WEST:
						//+z-x
						int beruehrpktSWX=-(xdur*multiplier)+xmin;
						int beruehrpktSWZ=(zdur*multiplier)+zmax;
						if(beruehrpktSWX<=-371||beruehrpktSWZ>=99) {
							e.setCancelled(true);
						}
						break;
					case DOWN:
						//-y
						int beruehrpktD=-(ydur*multiplier)+ymin;
						if(beruehrpktD<=7) {
							e.setCancelled(true);
						}
						break;
					case UP:
						//+y
						int beruehrpktU=(ydur*multiplier)+ymax;
						if(beruehrpktU>=69) {
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
					switch(bF){
					case NORTH:
						//-z
						int beruehrpktN=-(zdur-multiplier)+zmin;
						if(beruehrpktN<=-66) {
							e.setCancelled(true);
						}
						break;
					case EAST:
						//+x
						int beruehrpktE=(xdur+multiplier)+xmax;
						if(beruehrpktE>=-65) {
							e.setCancelled(true);
						}
						break;
					case SOUTH:
						//+z
						int beruehrpktS=(zdur+multiplier)+zmax;
						if(beruehrpktS>=99) {
							e.setCancelled(true);
						}
						break;
					case WEST:
						//-x
						int beruehrpktW=-(xdur-multiplier)+xmin;
						if(beruehrpktW<=-371) {
							e.setCancelled(true);
						}
						break;
					case NORTH_EAST:
						//-z+x
						int beruehrpktNEX=(xdur+multiplier)+xmax;
						int beruehrpktNEZ=-(zdur-multiplier)+zmin;
						if(beruehrpktNEX>=-65||beruehrpktNEZ<=-66) {
							e.setCancelled(true);
						}
						break;
					case NORTH_WEST:
						//-z-x
						int beruehrpktNWX=-(xdur-multiplier)+xmin;
						int beruehrpktNWZ=-(zdur-multiplier)+zmin;
						if(beruehrpktNWX<=-371||beruehrpktNWZ<=-66) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_EAST:
						//+z+x
						int beruehrpktSEX=(xdur+multiplier)+xmax;
						int beruehrpktSEZ=(zdur+multiplier)+zmax;
						if(beruehrpktSEX>=-65||beruehrpktSEZ>=99) {
							e.setCancelled(true);
						}
						break;
					case SOUTH_WEST:
						//+z-x
						int beruehrpktSWX=-(xdur-multiplier)+xmin;
						int beruehrpktSWZ=(zdur+multiplier)+zmax;
						if(beruehrpktSWX<=-371||beruehrpktSWZ>=99) {
							e.setCancelled(true);
						}
						break;
					case DOWN:
						//-y
						int beruehrpktD=-(ydur-multiplier)+ymin;
						if(beruehrpktD<=7) {
							e.setCancelled(true);
						}
						break;
					case UP:
						//+y
						int beruehrpktU=(ydur+multiplier)+ymax;
						if(beruehrpktU>=69) {
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

	private void rotateClipboard(Player p) {
		LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
		
			ClipboardHolder holder;
			try {
				holder = session.getClipboard();
				AffineTransform transform = new AffineTransform().rotateY(180);
				holder.setTransform(holder.getTransform().combine((Transform) transform));
				session.setClipboard(holder);
				p.sendMessage("§dThe clipboard copy has been rotatet by 180 degrees.");
			} catch (EmptyClipboardException e) {
				e.printStackTrace();
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
		System.out.println(yaw);
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
