package de.AS.Bau.WorldEdit;

import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import de.AS.Bau.Main;

public class UndoManager {

	Stack<Clipboard> undos;
	
	public UndoManager(Player p) {
		undos = new Stack<>();
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

	public void addUndo(Region rg, BlockVector3 at, World world) {
		rg.setWorld(world);
		BlockArrayClipboard board = new BlockArrayClipboard(rg);
		board.setOrigin(at);
		ForwardExtentCopy copy = new ForwardExtentCopy(rg.getWorld(), rg, board.getOrigin(), board,
				board.getOrigin());
		try {
			Operations.completeLegacy(copy);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		addUndo(board);
	}
	
	public Clipboard getUndo() {
		if(undos.size() == 0) {
			return null;
		}
		return undos.pop();
	}
}
