package net.wargearworld.bau.advancement.listener;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.advancement.AdvTemplate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class AdvEnter extends AdvTemplate {
    public AdvEnter() {
        super("enterbau");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> grantSingleCriterion(event.getPlayer()), 40);
    }
}
