package net.wargearworld.bau.listener;

import net.wargearworld.bau.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.tools.AutoCannonReloader;
import net.wargearworld.bau.tools.testBlockSlave.testBlockEditor.TestBlockEditorCore;

import java.util.UUID;

public class onPlayerQuit implements Listener {
    public onPlayerQuit(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeaveevent(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        onPlayerMove.playersLastPlot.remove(p.getUniqueId());
        ScoreBoardBau.getS(p).cancel();
        TestBlockEditorCore.playersTestBlockEditor.remove(p.getUniqueId());

        UUID uuid = p.getUniqueId();
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (BauPlayer.getBauPlayer(uuid).getBukkitPlayer() == null) {
                BauPlayer.remove(p.getUniqueId());
            }
        }, 10 * 60 * 60);//1hour
    }
}
