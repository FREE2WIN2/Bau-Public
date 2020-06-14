package net.wargearworld.Bau.WorldEdit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.Tools.Stoplag;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlockSlaveCore;
import net.wargearworld.Bau.Tools.TestBlockSlave.TestBlock.Facing;
import net.wargearworld.Bau.utils.CoordGetter;
import net.wargearworld.Bau.utils.Scheduler;

public class WorldEditHandler {

	public final static int maxBlockChangePerTick = Main.getPlugin().getCustomConfig()
			.getInt("worldEdit.maxBlockPerSecond");

	/* all Clipboard creator */

	public static Clipboard createClipboard(File file) {
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try {
			ClipboardReader reader = format.getReader(new FileInputStream(file));
			Clipboard clipboard = reader.read();
			return clipboard;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Clipboard createClipboardOutOfRegion(Region rg, BlockVector3 origin, World world) {
		rg.setWorld(world);
		BlockArrayClipboard board = new BlockArrayClipboard(rg);
		board.setOrigin(origin);
		ForwardExtentCopy copy = new ForwardExtentCopy(rg.getWorld(), rg, board.getOrigin(), board, board.getOrigin());
		try {
			Operations.completeLegacy(copy);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		return board;
	}

	/* saving .schem files */

	public static void saveClipboardAsSchematic(String path, String name, Clipboard board) {
		File folder = new File(path);
		if (!folder.exists()) {
			try {
				folder.mkdirs();
				folder.setExecutable(true, false);
				folder.setReadable(true, false);
				folder.setWritable(true, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!name.endsWith(".schem")) {
			name += ".schem";
		}
		File file = new File(folder, name);
		try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
			writer.write(board);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* undo */

	public static void createUndo(Region rg, Player p, BlockVector3 at) {
		TestBlockSlaveCore.getSlave(p).getUndoManager().addUndo(rg, at, BukkitAdapter.adapt(p.getWorld()));
	}

	/* Clipboard manipulation */

	public static void rotateClipboard(Player p) {
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

	public static Clipboard rotateClipboard(Clipboard board) {
		AffineTransform transform = new AffineTransform().rotateY(180);
		FlattenedClipboardTransform transformClip = FlattenedClipboardTransform.transform(board, transform);
		return transformClip.getClip(transformClip.getTransformedRegion());
	}

	/* all paste Methods */

	public static void pasteground(Schematic schem, String rgID, Player p, boolean ignoreAir) {
		BlockVector3 at = CoordGetter.getTBSPastePosition(rgID, schem.getFacing(), p.getWorld().getName());
		pasteAsync(new ClipboardHolder(schem.getClip()), at, p, ignoreAir, 1, false, false);

	}

	public static void pasteTestBlock(Schematic schem, Facing facingto, String rgID, Player p, boolean saveUndo) {
		BlockVector3 at = CoordGetter.getTBSPastePosition(rgID, facingto, p.getWorld().getName());
		Clipboard board = schem.getClip();
		if (schem.getFacing() != facingto) {
			board = rotateClipboard(board);
		}

		pasteAsync(new ClipboardHolder(board), at, p, false, 1, saveUndo, true);

	}

	/**
	 * @param clipboard             -> Clipboard to paste
	 * @param x                     -> x-coordinate of paste-position
	 * @param y                     -> y-coordinate of paste-position
	 * @param z                     -> z-coordinate of paste-position
	 * @param p                     -> player which paste something
	 * @param ignoreAir             -> true if paste -a
	 * @param ticksPerPasteInterval -> Speed of paste: min 1
	 * @param saveUndo              -> Save overwritten Region to undo it
	 * @param tbs                   -> true if paste as testblocksklave, false if it
	 *                              used to normal worldeditOperation
	 */
	public static void pasteAsync(ClipboardHolder clipboardHolder, BlockVector3 at, Player p, boolean ignoreAir,
			int ticksPerPasteInterval, boolean saveUndo, boolean tbs) {

		/* Get Clipboard out of the ClipboardHolder with the transform */

		FlattenedClipboardTransform result = FlattenedClipboardTransform.transform(clipboardHolder.getClipboard(),
				clipboardHolder.getTransform());
		Clipboard clipboard = result.getClip(result.getTransformedRegion());
		/* offset from origin pasteloc and new pasteloc -> have to be added */

		if (clipboard == null) {
			System.err.println("Clipboard TBS -> null");
			return;
		}

		BlockVector3 offset = at.subtract(clipboard.getOrigin());
		BlockVector3 min = clipboard.getMinimumPoint();
		BlockVector3 max = clipboard.getMaximumPoint();
		World world = BukkitAdapter.adapt(p.getWorld());
		String originRegion = WorldGuardHandler.getPlotId(clipboard.getOrigin().add(offset), world);

		if (saveUndo) {
			if (tbs) {
				Region rg = new CuboidRegion(min.add(offset), max.add(offset));
				createUndo(rg, p, at);
			}
		}

		Scheduler animation = new Scheduler();
		int xmin = min.getX();
		int xmax = max.getX();
		int ymin = min.getY();
		int ymax = max.getY();
		int zmin = min.getZ();
		int zmax = max.getZ();

		animation.setX(xmin);
		animation.setY(ymin);
		animation.setZ(zmin);
		if (tbs && !saveUndo && !ignoreAir&Stoplag.getStatus(p.getLocation())) {
			Stoplag.setStatusTemp(p.getLocation(), true, Stoplag.getPasteTime(p.getLocation()));
		}
		animation.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				int blockChanged = 0;
				int xmins = animation.getX();
				int ymins = animation.getY();
				int zmins = animation.getZ();
				for (int x = xmins; x <= xmax; x++) {
					for (int y = ymins; y <= ymax; y++) {
						for (int z = zmins; z <= zmax; z++) {

							if (blockChanged > maxBlockChangePerTick) {
								animation.setX(x);
								animation.setY(y);
								animation.setZ(z);
								return;
							}

							/* set block in world out of schematic */

							BlockVector3 blockLoc = BlockVector3.at(x, y, z);
							if (WorldGuardHandler.getPlotId(blockLoc.add(offset), world).equals(originRegion) || !tbs) {
								if (!(clipboard.getFullBlock(blockLoc).getBlockType().getMaterial().isAir()
										&& ignoreAir)) {
									try {
										world.setBlock(blockLoc.add(offset), clipboard.getFullBlock(blockLoc));
										blockChanged++;
									} catch (WorldEditException e) {
										e.printStackTrace();
									}
								}
							} else {
								blockChanged++;
							}

						}
						zmins = zmin;
					}
					ymins = ymin;
				}
				/* all loops are over -> pasting is done */
				animation.cancel();
			}

		}, 0, ticksPerPasteInterval));
	}

}
