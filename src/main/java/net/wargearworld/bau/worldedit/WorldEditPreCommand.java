package net.wargearworld.bau.worldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.event.WorldEditMoveEvent;
import net.wargearworld.bau.utils.HelperMethods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.tools.Stoplag;

public class WorldEditPreCommand implements Listener {

    @EventHandler
    public void PreCommandProcess(PlayerCommandPreprocessEvent event) {

        String command = event.getMessage();
        String[] args = command.split(" ");
        Player p = event.getPlayer();
        BauPlayer player = BauPlayer.getBauPlayer(p);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
        if ((command.startsWith("//paste") || (command.startsWith("//move")) || (command.startsWith("//stack"))) && player.getPasteState()) {
            Stoplag.getInstance().setStatusTemp(p.getLocation(), true, player.getPasteTime());
        }

        if (command.equalsIgnoreCase("//rotate")) {
            event.setCancelled(true);
            WorldEditHandler.rotateClipboard(p);
        }

        if (args[0].equalsIgnoreCase("//move")) {
            try {
                Region selection = localSession.getSelection(BukkitAdapter.adapt(p.getWorld()));
                Location loc = BukkitAdapter.adapt(p.getLocation());
                int movement = 1;
                if (args.length > 1 && HelperMethods.isInt(args[1])) {
                    movement = Integer.parseInt(args[1]);
                }
                BlockVector3 offset = loc.getDirectionEnum().toBlockVector().multiply(movement);
                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
                    Bukkit.getPluginManager().callEvent(new WorldEditMoveEvent(offset, selection, p));
                });
            } catch (IncompleteRegionException e) {
            }
        }
    }
}
