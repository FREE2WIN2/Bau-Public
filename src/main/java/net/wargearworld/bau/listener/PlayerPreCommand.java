package net.wargearworld.bau.listener;

import net.wargearworld.bau.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerPreCommand implements Listener {

    @EventHandler
    public void onPlayerPreCommand(PlayerCommandPreprocessEvent event){
        Player p = event.getPlayer();
        if(!p.getWorld().getName().startsWith("test")){
            return;
        }
        if(event.getMessage().startsWith("//cut")|| event.getMessage().startsWith("//copy")){
            event.setCancelled(true);
        }
    }

}
