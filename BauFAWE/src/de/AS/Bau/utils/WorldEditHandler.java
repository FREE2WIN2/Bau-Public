package de.AS.Bau.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

import de.AS.Bau.Main;

public class WorldEditHandler {

	public static void pasten(String fileName, int x, int y, int z, Player p, boolean b) {
		try {

			Clipboard clipboard = createClipboard(fileName);

			EditSession editSession = (EditSession) WorldEdit.getInstance().getEditSessionFactory()
					.getEditSession(BukkitAdapter.adapt(p.getWorld()), -1);
			editSession.setFastMode(false);
			editSession.enableStandardMode();
			Operation operation;

			operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(x, y, z))
					.ignoreAirBlocks(true).build();
			try {
				Operations.complete(operation);
				WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p)).remember(editSession);
				editSession.flushSession();
			} catch (WorldEditException e) {
				p.sendMessage(
						Main.prefix + "irgendetwas ist schiefgelaufen, überprüfe ob alle dummys eingespeichert sind!");
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Clipboard createClipboard(String filename) {
		String path = Main.getPlugin().getCustomConfig().getString("Config.path");
		File pathFile = new File(path);
		File schem = new File(pathFile.getParentFile().getAbsolutePath() + "/schematics/TestBlockSklave" + "/" + filename + ".schem");
		ClipboardFormat format = ClipboardFormats.findByFile(schem);
		try {
			ClipboardReader reader = format.getReader(new FileInputStream(schem));
			Clipboard clipboard = reader.read();
			return clipboard;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * @param clipboard             -> Clipboard to paste
	 * @param x                     -> x-coordinate of paste-position
	 * @param y                     -> y-coordinate of paste-position
	 * @param z                     -> z-coordinate of paste-position
	 * @param p                     -> player which paste something
	 * @param ignoreAir             -> true if paste -a
	 * @param ticksPerPasteInterval -> Speed of paste: min 1
	 */
	public static void pasteAsync(Clipboard clipboard, int x, int y, int z, Player p, boolean ignoreAir,
			int ticksPerPasteInterval,boolean saveUndo) {
		// offset from origin pasteloc and new pasteloc -> have to be added
		if(clipboard == null) {
			System.err.println("Clipboard TBS -> null");
			return;
		}
		BlockVector3 offset = BlockVector3.at(x, y, z).subtract(clipboard.getOrigin());

		BlockVector3 min = clipboard.getMinimumPoint();
		BlockVector3 max = clipboard.getMaximumPoint();
		if(saveUndo) {
			Region rg = new CuboidRegion(min.add(offset), max.add(offset));
			createUndo(rg,p);
		}
		World world = BukkitAdapter.adapt(p.getWorld());
		Scheduler animation = new Scheduler();
		int xmin = min.getX();
		int xmax = max.getX();
		int ymin = min.getY();
		int ymax = max.getY();
		int zmin = min.getZ();
		int zmax = max.getZ();
		animation.setZ(zmin);

		animation.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				int z = animation.getZ();
				for (int x = xmin; x <= xmax; x++) {
					for (int y = ymin; y <= ymax; y++) {

						try {
							BlockVector3 blockLoc = BlockVector3.at(x, y, z).add(offset);
							world.setBlock(blockLoc, clipboard.getFullBlock(BlockVector3.at(x, y, z)));
						} catch (WorldEditException e) {
							e.printStackTrace();
						}
					}
				}

				if (z >= zmax) {
					animation.cancel();
				}
				z += 1;
				animation.setZ(z);
			}

		}, 0, ticksPerPasteInterval));
	}

	public static void pasteAsync(String fileName, int x, int y, int z, Player p, boolean ignoreAir,
			int ticksPerPasteInterval,boolean saveUndo) {
		Clipboard board = createClipboard(fileName);
		pasteAsync(board, x, y, z, p, ignoreAir, ticksPerPasteInterval,saveUndo);
	}

	public static void createUndo(Region rg, Player p) {
		UndoManager manager;
		if(!Main.playersUndoManager.containsKey(p.getUniqueId())) {
			manager = new UndoManager(p);
		}else {
			manager = Main.playersUndoManager.get(p.getUniqueId());
		}
		manager.addUndo(rg);
	}
		
}
