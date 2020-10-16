package net.wargearworld.bau.tools.particles;

import net.wargearworld.bau.player.BauPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.worldedit.FlattenedClipboardTransform;
import net.wargearworld.bau.utils.Scheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import static net.wargearworld.bau.player.BauPlayer.getBauPlayer;
public class ParticlesShow {

	private Color clipboardColor;
	private Color selectionColor;
	private boolean clipboardActive;
	private boolean selectionActive;
	private BukkitTask task;
	private UUID uuid;
	private Particle particle;

	public ParticlesShow(Player p) {
		BauPlayer player = BauPlayer.getBauPlayer(p);
		uuid = player.getUuid();
		particle = Particle.REDSTONE;
		FileConfiguration config = player.getConfig();
		if (!config.contains("particles")) {
			clipboardColor = Particles.defaultClipboardColor;
			selectionColor = Particles.defaultSelectionColor;
			clipboardActive = Particles.defaultStateClipboard;
			selectionActive = Particles.defaultStateSelection;
			saveClipboardColor(player);
			saveSelectionColor(player);
			saveActive(player);
		} else {
			clipboardColor = readColor(config.getString("particles.clipboard.color"));
			selectionColor = readColor(config.getString("particles.selection.color"));
			clipboardActive = config.getBoolean("particles.clipboard.active");
			selectionActive = config.getBoolean("particles.selection.active");
		}

	}

	public static Color readColor(String arg) {
		String[] args = arg.split(" ");
		int r = Integer.parseInt(args[0]);
		int g = Integer.parseInt(args[1]);
		int b = Integer.parseInt(args[2]);
		return Color.fromRGB(r, g, b);
	}
	private void saveActive(BauPlayer player) {
		player.getConfig().set("particles.clipboard.active", clipboardActive);
		player.getConfig().set("particles.selection.active", selectionActive);
		player.saveConfig();
	}

	private void saveSelectionColor(BauPlayer player) {
		int r = selectionColor.getRed();
		int g = selectionColor.getGreen();
		int b = selectionColor.getBlue();
		player.getConfig().set("particles.selection.color", r + " " + g + " " + b);
		player.saveConfig();
	}

	private void saveClipboardColor(BauPlayer player) {
		int r = clipboardColor.getRed();
		int g = clipboardColor.getGreen();
		int b = clipboardColor.getBlue();
		player.getConfig().set("particles.clipboard.color", r + " " + g + " " + b);
		player.saveConfig();
	}

	/* setter */

	public void setSelectionColor(int r, int g, int b) {
		selectionColor = Color.fromRGB(r, g, b);
		saveSelectionColor(getBauPlayer(uuid));
	}

	public void setSelectionColor(Color color) {
		selectionColor = color;
		saveSelectionColor(getBauPlayer(uuid));
	}

	public void setClipboardColor(int r, int g, int b) {
		clipboardColor = Color.fromRGB(r, g, b);
		saveClipboardColor(getBauPlayer(uuid));
	}

	public void setClipboardColor(Color color) {
		clipboardColor = color;
		saveClipboardColor(getBauPlayer(uuid));
	}

	public void setClipboardActive(boolean active) {
		this.clipboardActive = active;
		saveActive(getBauPlayer(uuid));
			showParticles();
	}

	public void setSelectionActive(boolean active) {
		this.selectionActive = active;
		saveActive(getBauPlayer(uuid));
			showParticles();
	}
	/* getter */

	public Color getClipboardColor() {
		return clipboardColor;
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public boolean isClipboardActive() {
		return clipboardActive;
	}

	public boolean isSelectionActive() {
		return selectionActive;
	}
	/* show particles */

	public void showParticles() {
		if(task != null)
			return;
		Player player = getBauPlayer(uuid).getBukkitPlayer();
		BukkitPlayer p = BukkitAdapter.adapt(player);
		LocalSession session = WorldEdit.getInstance().getSessionManager().get(p);

		task = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				if (player.getEquipment().getItemInMainHand().getType()!=Material.WOODEN_AXE) {
					return;
				}
				ClipboardHolder clipboardHolder = null;
				Region selection = null;

				/* Clipboard */

					if(clipboardActive) {
						try {
							clipboardHolder = session.getClipboard();
							FlattenedClipboardTransform transformed = FlattenedClipboardTransform
									.transform(clipboardHolder.getClipboard(), clipboardHolder.getTransform());
							Clipboard clipboard = transformed.getClip(transformed.getTransformedRegion());
							Location loc = player.getLocation();
							BlockVector3 offset = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
									.subtract(clipboard.getOrigin());
							show(clipboard.getMinimumPoint().add(offset), clipboard.getMaximumPoint().add(offset),
									clipboardColor);
						} catch (EmptyClipboardException e) {
						}
					}

				/* Selection */
				if(selectionActive) {
					try {
						if (session.getSelectionWorld() == null) {
							return;
						}
						if (session.getSelection(session.getSelectionWorld()) != null) {
							selection = session.getSelection(session.getSelectionWorld());
							show(selection.getMinimumPoint(), selection.getMaximumPoint(), selectionColor);
						}
					} catch (IncompleteRegionException e) {
					}
				}
			}
		}, 0, 15 * 1);
	}

	private void show(BlockVector3 min, BlockVector3 max, Color color) {
		Player player = getBauPlayer(uuid).getBukkitPlayer();
		if(player == null)
		return;

		DustOptions options = new DustOptions(color, 1.0f);
		int count = 1;
		int xmin = min.getBlockX();
		int xmax = max.getBlockX() + 1;
		int ymin = min.getBlockY();
		int ymax = max.getBlockY() + 1;
		int zmin = min.getBlockZ();
		int zmax = max.getBlockZ() + 1;
		for (double x = xmin; x <= xmax; x += 1) {
			player.spawnParticle(particle, x, ymin, zmin, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, x, ymax, zmin, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, x, ymin, zmax, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, x, ymax, zmax, count, 0, 0, 0, 0, options);
		}
		for (double y = ymin; y <= ymax; y += 1) {
			player.spawnParticle(particle, xmin, y, zmin, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, xmax, y, zmin, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, xmin, y, zmax, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, xmax, y, zmax, count, 0, 0, 0, 0, options);
		}
		for (double z = zmin; z <= zmax; z += 1) {
			player.spawnParticle(particle, xmin, ymin, z, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, xmin, ymax, z, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, xmax, ymin, z, count, 0, 0, 0, 0, options);
			player.spawnParticle(particle, xmax, ymax, z, count, 0, 0, 0, 0, options);
		}
	}

}
