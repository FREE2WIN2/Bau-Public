package de.AS.Bau.WorldEdit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;

import de.AS.Bau.Main;

public class Schematic {

	private File schemFile;
	
	/**
	 * creates a new Schematic out of the owner-File of the Schem-System
	 * 
	 * @param dir:  name of the directory relative to the schempath-directory If
	 *              null: file directly in the schempath-dir
	 * @param name: the name of the Schematic(WITH ENDING!)
	 */ 
	public Schematic(String dir, String name) {
		if(dir == null) {
			schemFile = new File(Main.getPlugin().getCustomConfig().getString("schempath") + "/" + name);
		}else {
			schemFile = new File(Main.getPlugin().getCustomConfig().getString("schempath") + "/" + dir + "/" + name);
		}
		
		if(!schemFile.exists()) {
			System.err.println("Schematic not fount: " + schemFile.getAbsolutePath());
		}
	}
	
	
	/**
	 * @return returns the Clipboard out of this schematic
	 *  if schematic not exists it returns null.
	 * 
	 */
	public Clipboard getClip() {
		ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
		try {
			ClipboardReader reader = format.getReader(new FileInputStream(schemFile));
			Clipboard clipboard = reader.read();
			return clipboard;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
