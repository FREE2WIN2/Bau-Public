package net.wargearworld.bau.tools.testBlockSlave;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.math.BlockVector3;

public class TestBlockSlaveParticles {

	public static void showTBParticlesShield(Player p, BlockVector3 min, BlockVector3 max) {

		int xmin = min.getX();
		int xmax = max.getX() + 1;

		int ymin = min.getY();
		int ymax = max.getY() + 1;

		int zmin = min.getZ();
		int zmax = max.getZ() + 1;

		/*
		 * X-Y Ebenen
		 */
		int count = 1;
		Particle particle_type = Particle.REDSTONE;
		DustOptions options = new DustOptions(Color.FUCHSIA, 1);
		int stepMatrix = 5;
		for (double x = xmin; x <= xmax; x += stepMatrix) {
			for (double y = ymin; y <= ymax; y += stepMatrix) {
				p.spawnParticle(particle_type, x, y, zmin, count, 0, 0, 0, 0, options);
				p.spawnParticle(particle_type, x, y, zmax, count, 0, 0, 0, 0, options);
			}
		}

		/*
		 * X-Z Ebenen
		 */

		for (double z = zmin; z <= zmax; z += stepMatrix) {
			for (double x = xmin; x <= xmax; x += stepMatrix) {
				p.spawnParticle(particle_type, x, ymax, z, count, 0, 0, 0, 0, options);
			}
		}

		/*
		 * Y-Z Ebenen
		 */

		for (double y = min.getY(); y <= max.getY(); y += stepMatrix) {
			for (double z = min.getZ(); z <= max.getZ(); z += stepMatrix) {
				p.spawnParticle(particle_type, xmin, y, z, count, 0, 0, 0, 0, options);
				p.spawnParticle(particle_type, xmax, y, z, count, 0, 0, 0, 0, options);
			}
		}

		for (double x = xmin; x <= xmax; x += 1) {
			p.spawnParticle(particle_type, x, ymin, zmin, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, x, ymax, zmin, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, x, ymin, zmax, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, x, ymax, zmax, count, 0, 0, 0, 0, options);
		}
		for (double y = ymin; y <= ymax; y += 1) {
			p.spawnParticle(particle_type, xmin, y, zmin, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, xmax, y, zmin, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, xmin, y, zmax, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, xmax, y, zmax, count, 0, 0, 0, 0, options);
		}
		for (double z = zmin; z <= zmax; z += 1) {
			p.spawnParticle(particle_type, xmin, ymin, z, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, xmin, ymax, z, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, xmax, ymin, z, count, 0, 0, 0, 0, options);
			p.spawnParticle(particle_type, xmax, ymax, z, count, 0, 0, 0, 0, options);
		}

	}

}
