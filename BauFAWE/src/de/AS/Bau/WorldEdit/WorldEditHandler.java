package de.AS.Bau.WorldEdit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

import de.AS.Bau.Main;
import de.AS.Bau.Tools.Stoplag;
import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.Scheduler;

public class WorldEditHandler {

	public final static int maxBlockChangePerTick = Main.getPlugin().getCustomConfig()
			.getInt("worldEdit.maxBlockPerSecond");
	private final static WorldEdit we = WorldEdit.getInstance();

	public static void pasten(String fileName, String rgID, Player p, boolean ignoreAir, boolean undo, boolean tbs) {
		Clipboard board = createClipboard(fileName);
		BlockVector3 at = CoordGetter.getPastePosition(rgID);

		pasteAsync(new ClipboardHolder(board), at, p, ignoreAir, 1, undo, tbs);
	}

	public static Clipboard createClipboard(String filename) {
		File schem = new File(Main.getPlugin().getConfig().getString("schempath") + "/TestBlockSklave" + "/" + filename + ".schem");
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

	public static void pasteOperationAsync(ClipboardHolder clipboardHolder, Player p, BlockVector3 to,
			boolean ignoreAir, boolean saveUndo) {
		Scheduler asyncScheduler = new Scheduler();
		EditSession es = we.getEditSessionFactory().getEditSession(BukkitAdapter.adapt(p.getWorld()), -1);
		Operation op = clipboardHolder.createPaste(es).ignoreAirBlocks(ignoreAir).to(to).build();
		LocalSession session = we.getSessionManager().get(BukkitAdapter.adapt(p));
		asyncScheduler.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < maxBlockChangePerTick; i++) {
					if (op == null) {
						session.remember(es);
						es.flushSession();
					}
					try {
						op.resume(new RunContext());
					} catch (WorldEditException e) {
						e.printStackTrace();
					}
				}
			}
		}, 0, 1));
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

		if (saveUndo) {
			if (tbs) {
				Region rg = new CuboidRegion(min.add(offset), max.add(offset));
				createUndo(rg, p, at);
			} else {
				// normal undo
				pasteOperationAsync(clipboardHolder, p, at, ignoreAir, saveUndo);
			}
		}

		Stoplag.setStatus(p.getLocation(), true);

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
							if (!(clipboard.getFullBlock(BlockVector3.at(x, y, z)).getBlockType().getMaterial().isAir()
									&& ignoreAir)) {
								try {
									world.setBlock(blockLoc.add(offset), clipboard.getFullBlock(blockLoc));
									blockChanged++;
								} catch (WorldEditException e) {
									e.printStackTrace();
								}
							}

						}
						zmins = zmin;
					}
					ymins = ymin;
				}
				/* all loops are over -> pasting is done */
				animation.cancel();
				Stoplag.setStatusTemp(p.getLocation(), true, 5);
			}

		}, 0, ticksPerPasteInterval));
	}

	public static void pasteAsync(String fileName, BlockVector3 at, Player p, boolean ignoreAir,
			int ticksPerPasteInterval, boolean saveUndo, boolean tbs) {
		Clipboard board = createClipboard(fileName);
		pasteAsync(new ClipboardHolder(board), at, p, ignoreAir, ticksPerPasteInterval, saveUndo, tbs);
	}

	public static void createUndo(Region rg, Player p, BlockVector3 at) {
		UndoManager manager;
		if (Main.playersUndoManager.containsKey(p.getUniqueId())) {
			manager = Main.playersUndoManager.get(p.getUniqueId());
		} else {
			manager = new UndoManager(p);
		}
		manager.addUndo(rg, at, BukkitAdapter.adapt(p.getWorld()));
	}

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

}
