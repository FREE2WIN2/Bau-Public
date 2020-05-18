package de.AS.Bau.utils;

import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;

import de.AS.Bau.Main;

public class UndoManager {

	Stack<Clipboard> undos;
	
	public UndoManager(Player p) {
		undos = new Stack<>();
		Main.playersUndoManager.put(p.getUniqueId(),this);
	}
	
	public void addUndo(Clipboard undoClipboard) {
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

	public void addUndo(Region rg) {
		undos.push(new BlockArrayClipboard(rg));
	}
	
	public Clipboard getUndo() {
		return undos.pop();
	}
}
