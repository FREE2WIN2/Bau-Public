package net.wargearworld.bau.tools;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.session.Session;
import net.wargearworld.CommandManager.ArgumentList;
import net.wargearworld.CommandManager.CommandHandel;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
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

import static net.wargearworld.CommandManager.Nodes.LiteralNode.literal;
import static net.wargearworld.CommandManager.Nodes.InvisibleNode.invisible;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutoCannonReloaderListener implements Listener, TabExecutor {
    public static final Material toolMaterial = Material
            .valueOf(Main.getPlugin().getCustomConfig().getString("tntReload.materialType"));

    private static AutoCannonReloaderListener instance;

    public static AutoCannonReloaderListener getInstance() {
        if (instance == null) {
            instance = new AutoCannonReloaderListener();
        }
        return instance;
    }

    private CommandHandel commandHandel;

    public AutoCannonReloaderListener() {
        commandHandel = new CommandHandel("tr", Main.prefix, Main.getPlugin());
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
        Player p = s.getPlayer();
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(s.getPlayer()).getCannonReloader();
        autoCannonReloader.deleteRecord(s.getPlayer(), true);
        try {
            World world = p.getWorld();
            Region rg = session.getSelection(BukkitAdapter.adapt(world));
            Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
                for (BlockVector3 block : rg) {
                    if (world.getBlockAt(block.getX(), block.getY(), block.getZ()).getType() == Material.TNT) {
                        autoCannonReloader.save(new Location(world, block.getX(), block.getY(), block.getZ()), p);
                    }
                }
            p.sendMessage(MessageHandler.getInstance().getString(p,"cannonreloader_regionSaved",autoCannonReloader.getSize()+ ""));
            });
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
    }

    private void reset(ArgumentList s, boolean b) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(s.getPlayer()).getCannonReloader();
        autoCannonReloader.deleteRecord(s.getPlayer(), b);
    }

    private void paste(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(s.getPlayer()).getCannonReloader();
        autoCannonReloader.pasteRecord(s.getPlayer());
    }

    private void stop(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(s.getPlayer()).getCannonReloader();
        autoCannonReloader.endRecord(s.getPlayer());
    }

    private void start(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(s.getPlayer()).getCannonReloader();
        autoCannonReloader.startRecord(s.getPlayer());
    }

    private void showhelp(ArgumentList s) {
        AutoCannonReloader autoCannonReloader = BauPlayer.getBauPlayer(s.getPlayer()).getCannonReloader();
        autoCannonReloader.showHelp(s.getPlayer());
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

        if (!commandHandel.execute(p, MessageHandler.getInstance().getLanguage(p), args))
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
        commandHandel.tabComplete(p, MessageHandler.getInstance().getLanguage(p), args, ret);
        return ret;
    }

    @EventHandler
    public void clickListener(PlayerInteractEvent event) {
        Action a = event.getAction();
        Player p = event.getPlayer();
        if (!event.getMaterial().equals(toolMaterial)) {
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


}
