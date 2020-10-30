package net.wargearworld.bau.utils;


import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import org.bukkit.entity.Player;

public class CommandUtil {
    public static Player getPlayer(ArgumentList s){
        return ((BukkitCommandPlayer)s.getPlayer()).getPlayer();
    }
}
