package net.wargearworld.Bau.WorldEdit;

import java.io.File;

import org.bukkit.Location;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;

public class Schematic {

	private Facing facing;
	private Clipboard board;
	private File file;
	
	/**
	 * creates a new Schematic out of the owner-File of the Schem-System
	 * 
	 * @param dir:  name of the directory relative to the schempath-directory If
	 *              null: file directly in the schempath-dir
	 * @param name: the name of the Schematic(WITH ENDING!)
	 */ 
	
	
	public Schematic(String dir, String name) {
 		if(dir == null) {
			file = new File(Main.getPlugin().getCustomConfig().getString("schempath") + "/" + name);
		}else {
			file = new File(Main.getPlugin().getCustomConfig().getString("schempath") + "/" + dir + "/" + name);
		}
		if(!file.exists()) {
			System.err.println("Schematic not fount: " + file.getAbsolutePath());
		}else {
			board = WorldEditHandler.createClipboard(file);
		}
	}
 	public Schematic(String dir, String name, Facing facing) {
 		this(dir,name);
		this.facing = facing;
	}
	
	public void setFacing(Facing facing) {
		this.facing = facing;
	}
	
	public Facing getFacing() {
		return facing;
	}
 	
	/**
	 * @return returns the Clipboard out of this schematic
	 *  if schematic not exists it returns null.
	 * 
	 */
	public Clipboard getClip() {
		return board;
	}

	public void setClipboard(Clipboard clipboard) {
		this.board = clipboard;
	}

	public File getFile() {
		return file;
	}
	
	public void paste(Location at,boolean ignoreAir) {
		WorldEditHandler.pasteAsync(new ClipboardHolder(board), at, ignoreAir);
	}
	
}
