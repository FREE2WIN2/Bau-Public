package net.wargearworld.bau.tools.particles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
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

public class ParticlesShow {

	private Color clipboardColor;
	private Color selectionColor;
	private boolean active;
	private Scheduler scheduler;
	private Player player;
	private Particle particle;

	public ParticlesShow(Player p) {
		String uuid = p.getUniqueId().toString();
		player = p;
		scheduler = new Scheduler();
		particle = Particle.REDSTONE;
		if (!Particles.particleConfig.contains(uuid)) {
			clipboardColor = Particles.defaultClipboardColor;
			selectionColor = Particles.defaultSelectionColor;
			active = Particles.defaultState;
			saveClipboardColor();
			saveSelectionColor();
			saveActive();
		} else {
			clipboardColor = Particles.readColor(uuid + ".clipboard");
			selectionColor = Particles.readColor(uuid + ".selection");
			active = Particles.particleConfig.getBoolean(uuid + ".active");
		}

	}

	private void saveActive() {
		Particles.particleConfig.set(player.getUniqueId().toString() + ".active", active);
		Particles.saveConfig();
	}

	private void saveSelectionColor() {
		int r = selectionColor.getRed();
		int g = selectionColor.getGreen();
		int b = selectionColor.getBlue();
		Particles.particleConfig.set(player.getUniqueId().toString() + ".selection", r + " " + g + " " + b);
		Particles.saveConfig();

	}

	private void saveClipboardColor() {
		int r = clipboardColor.getRed();
		int g = clipboardColor.getGreen();
		int b = clipboardColor.getBlue();
		Particles.particleConfig.set(player.getUniqueId().toString() + ".clipboard", r + " " + g + " " + b);
		Particles.saveConfig();
	}

	/* setter */

	public void setSelectionColor(int r, int g, int b) {
		selectionColor = Color.fromRGB(r, g, b);
		saveSelectionColor();
	}

	public void setSelectionColor(Color color) {
		selectionColor = color;
		saveSelectionColor();
	}

	public void setClipboardColor(int r, int g, int b) {
		clipboardColor = Color.fromRGB(r, g, b);
		saveClipboardColor();
	}

	public void setClipboardColor(Color color) {
		clipboardColor = color;
		saveClipboardColor();
	}

	public void setactive(boolean active) {
		this.active = active;
		saveActive();
		if (active && player.getEquipment().getItemInMainHand().getType().equals(Material.WOODEN_AXE)) {
			showParticles();
		}
	}

	/* getter */

	public Color getClipboardColor() {
		return clipboardColor;
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public boolean isActive() {
		return active;
	}

	/* show particles */

	public void showParticles() {
		if (!active) {
			return;
		}
//		if (scheduler.getTask() >= 0) {
//			return;
//		}

		BukkitPlayer p = BukkitAdapter.adapt(player);
		LocalSession session = WorldEdit.getInstance().getSessionManager().get(p);

		scheduler.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				if (!active||player.getEquipment().getItemInMainHand().getType()!=Material.WOODEN_AXE) {
					stopParticles();
					return;
				}
				ClipboardHolder clipboardHolder = null;
				Region selection = null;

				/* Clipboard */

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

				/* Selection */
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
		}, 0, 20 * 1));
	}

	private void show(BlockVector3 min, BlockVector3 max, Color color) {
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

	/* stop show particles */

	public void stopParticles() {
		scheduler.cancel();
	}

}
