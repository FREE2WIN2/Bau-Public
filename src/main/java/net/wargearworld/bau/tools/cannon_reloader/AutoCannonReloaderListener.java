package net.wargearworld.bau.tools.cannon_reloader;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.event.WorldEditMoveEvent;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.command_manager.player.BukkitCommandPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.command_manager.nodes.InvisibleNode.invisible;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutoCannonReloaderListener implements Listener, TabExecutor {
    private static AutoCannonReloaderListener instance;

    public static AutoCannonReloaderListener getInstance() {
        if (instance == null) {
            instance = new AutoCannonReloaderListener();
        }
        return instance;
    }

    private CommandHandel commandHandel;

    public AutoCannonReloaderListener() {
        commandHandel = new CommandHandel("tr", Main.prefix, MessageHandler.getInstance());
        commandHandel.setCallback(s -> {
            showhelp(s);
        });

        commandHandel.addSubNode(literal("start").setCallback(s -> {
            start(s);
        }));
        commandHandel.addSubNode(literal("save").setCallback(s -> {
            save(s);
        }));
        commandHandel.addSubNode(literal("stop").setCallback(s -> {
            stop(s);
        }));
        commandHandel.addSubNode(literal("reload").setCallback(s -> {
            paste(s);
        }));
        commandHandel.addSubNode(literal("paste").setCallback(s -> {
            paste(s);
        }));
        commandHandel.addSubNode(literal("help").setCallback(s -> {
            showhelp(s);
        }));
        commandHandel.addSubNode(literal("reset").setCallback(s -> {
            reset(s, false);
        }).addSubNode(invisible(literal("confirm").setCallback(s -> {
            reset(s, true);
        }))));
    }

    private void save(ArgumentList s) {
        Player p = getPlayer(s);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(getPlayer(s)).getCannonReloader();
        autoCannonReloader.deleteRecord(getPlayer(s), true);
        try {
            World world = p.getWorld();
            Region rg = session.getSelection(BukkitAdapter.adapt(world));
            Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
                for (BlockVector3 block : rg) {
                    if (world.getBlockAt(block.getX(), block.getY(), block.getZ()).getType() == Material.TNT) {
                        autoCannonReloader.save(new Location(world, block.getX(), block.getY(), block.getZ()), p);
                    }
                }
                p.sendMessage(MessageHandler.getInstance().getString(p, "cannonreloader_regionSaved", autoCannonReloader.getSize() + ""));
            });
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
    }


    private void reset(ArgumentList s, boolean b) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(getPlayer(s)).getCannonReloader();
        autoCannonReloader.deleteRecord(getPlayer(s), b);
    }

    private void paste(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(getPlayer(s)).getCannonReloader();
        autoCannonReloader.pasteRecord(getPlayer(s));
    }

    private void stop(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(getPlayer(s)).getCannonReloader();
        autoCannonReloader.endRecord(getPlayer(s));
    }

    private void start(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(getPlayer(s)).getCannonReloader();
        autoCannonReloader.startRecord(getPlayer(s));
    }

    private void showhelp(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(getPlayer(s)).getCannonReloader();
        autoCannonReloader.showHelp(getPlayer(s));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        /*
         * tr|tntReload|cannonReload|cr start , stop , paste , reset , help
         *
         */
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        if (!commandHandel.execute(commandPlayer, args))
            Main.send(p, true, MessageHandler.getInstance().getString(p, "cannonReloader_prefix"), "cannonReloader_wrongCommand");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player p = (Player) sender;
        List<String> ret = new ArrayList<>();
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandel.tabComplete(commandPlayer, args, ret);
        return ret;
    }

    @EventHandler
    public void clickListener(PlayerInteractEvent event) {
        Action a = event.getAction();
        Player p = event.getPlayer();
        if (!event.getMaterial().equals(BauConfig.getInstance().getTntReloadItem())) {
            return;
        }
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(p).getCannonReloader();
        if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (autoCannonReloader.hasConent()) {
                if (autoCannonReloader.isRecording()) {
                    /* stop */
                    autoCannonReloader.endRecord(p);
                } else {
                    /* paste */

                    autoCannonReloader.pasteRecord(p);
                    if (p.isSneaking()) {
                        /* change */
                        autoCannonReloader.startRecord(p);
                    }
                }

            } else {
                /* start */
                autoCannonReloader.startRecord(p);
            }
        } else if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK)) {
            /* delete */
            if (autoCannonReloader.hasConent())
                autoCannonReloader.deleteRecord(p, false);
        }
    }

    @EventHandler
    public void registerTnt(BlockPlaceEvent event) {
        if (!event.getBlockPlaced().getType().equals(Material.TNT)) {
            return;
        }
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(uuid).getCannonReloader();
        if (!autoCannonReloader.isRecording()) {
            return;
        }
        autoCannonReloader.save(event.getBlockPlaced().getLocation(), p);
    }

    @EventHandler
    public void unregister(BlockBreakEvent event) {
        if (!event.getBlock().getType().equals(Material.TNT)) {
            return;
        }
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(uuid).getCannonReloader();
        if (!autoCannonReloader.isRecording()) {
            return;
        }
        autoCannonReloader.remove(event.getBlock().getLocation(), p);

    }

    @EventHandler
    public void moveEvent(WorldEditMoveEvent event) {
        Player p = event.getPlayer();
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(p.getUniqueId()).getCannonReloader();
        autoCannonReloader.move(event.getRegion(), event.getOffset());

    }


}
