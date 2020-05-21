package de.AS.Bau.Tools.Particles;

import org.bukkit.Color;
import org.bukkit.entity.Player;

import de.AS.Bau.utils.CoordGetter;
import de.AS.Bau.utils.Scheduler;

public class ParticlesShow {

	private Color clipboardColor;
	private Color SelectionColor;
	private boolean active;
	private Scheduler scheduler;
	
	public ParticlesShow(Player p) {
		String uuid = p.getUniqueId().toString();
		scheduler = new Scheduler();
		if(!ClipboardParticles.particleConfig.contains(uuid)) {
			ClipboardParticles.
		}
		active = ClipboardParticles.particleConfig.getBoolean(uuid + ".active");
	}
}
