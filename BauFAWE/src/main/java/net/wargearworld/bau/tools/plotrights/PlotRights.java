package net.wargearworld.bau.tools.plotrights;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.PlayerWorld;
import net.wargearworld.bau.world.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;


public class PlotRights implements Listener {
    private final List<String> disallowedCommands;

    public PlotRights() {
        disallowedCommands = Main.getPlugin().getCustomConfig().getStringList("disallowedcommands");
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();
        BauWorld world = WorldManager.get(player.getWorld());
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
