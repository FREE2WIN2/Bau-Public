package net.wargearworld.bau.listener;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.ChangeSetExecutor;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.event.WorldEditMoveEvent;
import net.wargearworld.bau.utils.HelperMethods;
import net.wargearworld.bau.utils.Loc;
import net.wargearworld.bau.utils.Scheduler;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.plot.Plot;
import net.wargearworld.bau.worldedit.WorldEditHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.tools.Stoplag;

import java.util.Iterator;

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
                System.out.println("WE direction: " + loc.getDirectionEnum());
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

        if (args[0].toLowerCase().contains("/undo") || args[0].toLowerCase().contains("/redo")) {
            ChangeSetExecutor.Type type = ChangeSetExecutor.Type.REDO;
            if (args[0].toLowerCase().contains("/undo")) {
                type = ChangeSetExecutor.Type.UNDO;
            }
            int repeats = 1;
            if (args.length == 2 && HelperMethods.isInt(args[1])) {
                repeats = Integer.parseInt(args[1]);
            }
            event.setCancelled(true);
            Scheduler scheduler = new Scheduler();
            scheduler.setX(0);
            int finalRepeats = repeats;
            ChangeSetExecutor.Type finalType = type;
            scheduler.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
                int repeat = scheduler.getX();
                undo(p, localSession, finalType);
                repeat++;
                if (repeat == finalRepeats) {
                    scheduler.cancel();
                }
            }, 0, 1));
        }


        if (event.getMessage().startsWith("//cut") || event.getMessage().startsWith("//copy")) {
            if (p.getWorld().getName().startsWith("test")) {
                event.setCancelled(true);
            }
        }
    }

    private void undo(Player p, LocalSession localSession, ChangeSetExecutor.Type finalType) {
        EditSession editSession = getEditSession(localSession, finalType, p);
        if (editSession == null)
            return;
        ChangeSet changeSet = editSession.getChangeSet();
        if (changeSet == null || changeSet.size() == 0)
            return;
        Iterator<Change> iterator;
        iterator = getIterator(changeSet, finalType);
        World world = editSession.getWorld();
        org.bukkit.World bukkitWorld = BukkitAdapter.adapt(world);
        BauWorld bauWorld = WorldManager.get(bukkitWorld);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            while (iterator.hasNext()) {
                Change change = iterator.next();
                if (change instanceof BlockChange) {
                    BlockChange blockChange = (BlockChange) change;
                    BlockVector3 changed = blockChange.getPosition();
                    Loc from = Loc.getByBlockVector(changed);
                    org.bukkit.Location location = from.toLocation(bukkitWorld);
                    Plot plot = bauWorld.getPlot(location);
                    plot.getCannonTimer().undo(from);
                    for (Player player : bukkitWorld.getPlayers()) {
                        BauPlayer bauPlayer = BauPlayer.getBauPlayer(player);
                        bauPlayer.getCannonReloader().undo(from);
                    }

                }
            }
        });
    }

    private Iterator<Change> getIterator(ChangeSet changeSet, ChangeSetExecutor.Type finalType) {
        if (finalType == ChangeSetExecutor.Type.UNDO) {
            return changeSet.backwardIterator();
        } else {
            return changeSet.forwardIterator();
        }

    }

    private EditSession getEditSession(LocalSession localSession, ChangeSetExecutor.Type finalType, Player p) {
        if (finalType == ChangeSetExecutor.Type.UNDO) {
            return localSession.undo(null, BukkitAdapter.adapt(p));
        } else {
            return localSession.redo(null, BukkitAdapter.adapt(p));

        }
    }
}
