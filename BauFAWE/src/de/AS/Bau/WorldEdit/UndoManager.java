package de.AS.Bau.WorldEdit;

import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import de.AS.Bau.Main;

public class UndoManager {

	Stack<Clipboard> undos;
	
	public UndoManager(Player p) {
		undos = new Stack<>();
		Main.playersUndoManager.put(p.getUniqueId(),this);
		System.out.println("added " + p.getUniqueId());
	}
	
	private void addUndo(Clipboard undoClipboard) {
		undos.push(undoClipboard);
		
		removeUndoAfterTime(undoClipboard);
	}
	
	private void removeUndoAfterTime(Clipboard undoClipboard) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(),new Runnable() {
			
			@Override
			public void run() {
				if(undos.contains(undoClipboard)) {
					undos.remove(undoClipboard);
				}
			}
		}, 20*60*10); //10 min
		
	}

	public void addUndo(Region rg, int x, int y , int z, World world) {
		rg.setWorld(world);
		BlockStateClipboard board = new BlockStateClipboard(rg);
		board.setOrigin(BlockVector3.at(x, y, z));
		board = fillClip(board, rg);
		addUndo(board);
	}
	
	public Clipboard getUndo() {
		return undos.pop();
	}
	
	private BlockStateClipboard fillClip(BlockStateClipboard board,Region rg) {
		World world = rg.getWorld();
		int xmin = rg.getMinimumPoint().getX();
		int xmax = rg.getMaximumPoint().getX();
		int ymin = rg.getMinimumPoint().getY();
		int ymax = rg.getMaximumPoint().getY();
		int zmin = rg.getMinimumPoint().getZ();
		int zmax = rg.getMaximumPoint().getZ();
		for(int x = xmin;x<=xmax;x++) {
			for(int y = ymin;y<=ymax;y++) {
				for(int z = zmin;z<=zmax;z++) {
					BlockVector3 blockat = BlockVector3.at(x, y, z);
					try {
						board.setBlock(blockat, world.getBlock(blockat));
					} catch (WorldEditException e) {
						e.printStackTrace();
					}
				}	
			}
		}
		return board;
	}



}
