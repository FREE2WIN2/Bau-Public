package de.AS.Bau.WorldEdit;

import java.io.File;

import com.sk89q.worldedit.extent.clipboard.Clipboard;

import de.AS.Bau.Main;
import de.AS.Bau.Tools.TestBlockSlave.TestBlock.Facing;

public class Schematic {

	private Facing facing;
	private Clipboard board;
	
	
	/**
	 * creates a new Schematic out of the owner-File of the Schem-System
	 * 
	 * @param dir:  name of the directory relative to the schempath-directory If
	 *              null: file directly in the schempath-dir
	 * @param name: the name of the Schematic(WITH ENDING!)
	 */ 
 	public Schematic(String dir, String name, Facing facing) {
		File schemFile;
 		if(dir == null) {
			schemFile = new File(Main.getPlugin().getCustomConfig().getString("schempath") + "/" + name);
		}else {
			schemFile = new File(Main.getPlugin().getCustomConfig().getString("schempath") + "/" + dir + "/" + name);
		}
		
		if(!schemFile.exists()) {
			System.err.println("Schematic not fount: " + schemFile.getAbsolutePath());
		}else {
			board = WorldEditHandler.createClipboard(schemFile);
		}
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
}
