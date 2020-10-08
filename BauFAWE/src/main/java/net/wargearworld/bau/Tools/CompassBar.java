package net.wargearworld.bau.tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.StringGetter.Language;

public class CompassBar {

	BukkitTask task;

	public CompassBar() {
		task = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					Location loc = p.getLocation();
					Face face = Face.fromYaw(loc.getYaw());
					MessageHandler.getInstance().sendHotBar(p, "compass_bar", loc.getBlockX() + "",
							face.name(MessageHandler.getInstance().getLanguage(p)), loc.getBlockZ() + "");
				}
			}
		}, 0, 1);
	}

	private enum Face {
		N, E, S, W;

		public static Face fromYaw(float yaw) {
			float yaws = Math.abs(yaw);
			if (yaws > 225 && yaws <= 315) {
				if (yaw < 0) {
					return W;
				} else {
					return E;
				}
			}
			if (yaws > 135 && yaws <= 225) {
				return N;
			}
			if (yaws > 45 && yaws <= 135) {
				if (yaw < 0) {
					return E;
				} else {
					return W;
				}
			}
			return S;

		}

		public String name(Language lang) {
			if (this == E) {
				if (lang == Language.DE) {
					return "O";
				} else {
					return "E";
				}
			}
			return this.name();
		}
	}

}
