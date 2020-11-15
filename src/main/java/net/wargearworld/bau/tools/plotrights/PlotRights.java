package net.wargearworld.bau.tools.plotrights;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;


public class PlotRights implements Listener {

    public PlotRights() {
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("bau.rights.bypass")){
            return;
        }
        String message = event.getMessage();
        BauWorld world = WorldManager.get(player.getWorld());
        List<String> disallowedCommands = BauConfig.getInstance().getDisallowedCommands();
        if (world instanceof PlayerWorld) {
            PlayerWorld playerWorld = (PlayerWorld) world;
            if (!playerWorld.hasRights(player.getUniqueId())) {
                for (String command : disallowedCommands) {
                    if (message.startsWith(command)) {
                        event.setCancelled(true);
                        MessageHandler.getInstance().send(player, "plotrights_norights", playerWorld.getName());
                        return;
                    }
                }
            }

        }


    }
}
