package net.wargearworld.bau.tools.cannon_timer;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.event.WorldEditMoveEvent;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.Loc;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.player.BukkitCommandPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.ArrayList;
import java.util.List;

import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;

public class CannonTimerListener implements TabExecutor, Listener {
    private CommandHandel commandHandel;

    public CannonTimerListener(Main main) {
        main.getCommand("cannontimer").setExecutor(this);
        main.getCommand("cannontimer").setTabCompleter(this);
        Bukkit.getPluginManager().registerEvents(this, main);

        commandHandel = new CommandHandel("cannontimer", Main.prefix, MessageHandler.getInstance());
        commandHandel.addSubNode(literal("activate").setCallback(s -> {
            Player p = getPlayer(s);
            CannonTimer cannonTimer = getCannonTimer(p);
            cannonTimer.start(p);
        }));

        commandHandel.addSubNode(literal("move")
                .addSubNode(literal("start")
                        .setCallback(s -> {
                            Player p = getPlayer(s);
                            CannonTimer cannonTimer = getCannonTimer(p);
                            cannonTimer.startMove(p);
                        }))
                .addSubNode(literal("end")
                        .setCallback(s -> {
                            Player p = getPlayer(s);
                            CannonTimer cannonTimer = getCannonTimer(p);
                            cannonTimer.place(p);
                        })).addSubNode(literal("undo")
                        .setCallback(s -> {
                            Player p = getPlayer(s);
                            CannonTimer cannonTimer = getCannonTimer(p);
                            cannonTimer.undoMoving(p);
                        })));

        commandHandel.addSubNode(literal("toggleAutoActivateTrail").setCallback(s -> {
            BauPlayer bauPlayer = BauPlayer.getBauPlayer(s.getPlayer().getUUID());
            bauPlayer.setActivateTrailOnCannonTimer(!bauPlayer.getActivateTrailOnCannonTimer());
            MessageHandler.getInstance().send(bauPlayer, "cannonTimer_settings_autoTrail_" + bauPlayer.getActivateTrailOnCannonTimer());
        }));

        commandHandel.addSubNode(literal("start").setCallback(s -> {
            Player p = getPlayer(s);
            WorldManager.get(p.getWorld()).getCannonTimer(p.getLocation()).start(p);
        }));

    }

    private CannonTimer getCannonTimer(Player p) {
        return WorldManager.get(p.getWorld()).getCannonTimer(p.getLocation());
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player p = (Player) commandSender;
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandel.execute(commandPlayer, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> out = new ArrayList<>();
        if (!(commandSender instanceof Player)) {
            return out;
        }
        Player p = (Player) commandSender;
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandel.tabComplete(commandPlayer, args, out);
        return out;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        BauWorld bauWorld = WorldManager.get(p.getWorld());
        if (bauWorld == null) {
            return;
        }
        if (bauWorld instanceof PlayerWorld) {
            if (!((PlayerWorld) bauWorld).hasRights(p.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }

        CannonTimer cannonTimer = bauWorld.getCannonTimer(p.getLocation());
        BauConfig bauConfig = BauConfig.getInstance();
        Material toolMaterial = bauConfig.getCannonTimerTool();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location blockLocation = event.getClickedBlock().getLocation();
            Material type = event.getClickedBlock().getType();
            if (type.name().contains(bauConfig.getCannonTimerDefaultBlock()) || type == bauConfig.getCannonTimerActiveBlock() || type == bauConfig.getCannonTimerInactiveBlock()) {
                //openGUI
                CannonTimerBlock cannonTimerBlock = cannonTimer.getBlock(blockLocation);
                if (cannonTimerBlock == null) {
                    return;
                }
                if (p.isSneaking() && event.getItem() != null && event.getItem().getType() == toolMaterial) {
                    event.setCancelled(true);
                    BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
                    try {
                        bauPlayer.setMovingCannonTimerBlock(cannonTimerBlock.clone());
                    } catch (CloneNotSupportedException e) {
                    }
                    MessageHandler.getInstance().send(p, "cannonTimer_copied");
                    return;
                }
                if (!p.isSneaking()) {
                    event.setCancelled(true);
                    CannonTimerGUI.openMain(event.getPlayer(), cannonTimerBlock, 1);
                }
            } else {
                if (event.getItem() != null && event.getItem().getType() == toolMaterial) {
                    cannonTimer.start(p);
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == toolMaterial && p.isSneaking()) {
            event.setCancelled(true);
            BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
            CannonTimerBlock cannonTimerBlock = bauPlayer.getCopiedCannonTimerBlock();
            if (cannonTimerBlock == null) {
                MessageHandler.getInstance().send(p, "cannonTimer_no_copy");
                return;
            }
            try {
                CannonTimerBlock clonedCannonTimerBlock = cannonTimerBlock.clone();
                Loc loc = Loc.getByLocation(event.getClickedBlock().getLocation());
                clonedCannonTimerBlock.setLoc(loc);
                cannonTimer.setBlock(loc, clonedCannonTimerBlock);
                MessageHandler.getInstance().send(p, "cannonTimer_pasted");
                return;
            } catch (CloneNotSupportedException e) {
            }

        } else if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getItem().getType() == toolMaterial) {
            cannonTimer.start(p);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        BauWorld bauWorld = WorldManager.get(p.getWorld());
        if (bauWorld instanceof PlayerWorld) {
            if (!((PlayerWorld) bauWorld).hasRights(p.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getBlockPlaced().getType().name().contains("SHULKER_BOX")) {
            //openGUI
            Location loc = event.getBlockPlaced().getLocation();
            CannonTimerBlock cannonTimerBlock = new CannonTimerBlock(loc);
            bauWorld.getCannonTimer(loc).addBlock(loc, cannonTimerBlock);
            CannonTimerGUI.openMain(event.getPlayer(), cannonTimerBlock, 1);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        BauWorld bauWorld = WorldManager.get(p.getWorld());
        if (bauWorld instanceof PlayerWorld) {
            if (!((PlayerWorld) bauWorld).hasRights(p.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
        Material type = event.getBlock().getType();
        BauConfig bauConfig = BauConfig.getInstance();
        Material toolMaterial = bauConfig.getCannonTimerTool();

        if (type.name().contains(bauConfig.getCannonTimerDefaultBlock()) || type == bauConfig.getCannonTimerActiveBlock() || type == bauConfig.getCannonTimerInactiveBlock()) {
            //openGUI
            Location loc = event.getBlock().getLocation();
            if (event.getPlayer().getEquipment().getItemInMainHand().getType() == toolMaterial) {
                event.setCancelled(true);
                CannonTimerBlock cannonTimerBlock = bauWorld.getCannonTimer(loc).getBlock(loc);
                if (cannonTimerBlock == null) {
                    event.setCancelled(false);
                    return;
                }
                if (cannonTimerBlock.isActive()) {
                    cannonTimerBlock.setActive(false, p.getWorld());
                    MessageHandler.getInstance().send(p, "cannonTimer_deactivated");
                } else {
                    cannonTimerBlock.setActive(true, p.getWorld());
                    MessageHandler.getInstance().send(p, "cannonTimer_activated");
                }
            } else {
                if (bauWorld.getCannonTimer(loc).getBlock(loc) != null) {
                    bauWorld.getCannonTimer(loc).removeBlock(loc);
                    MessageHandler.getInstance().send(p, "cannonTimer_removed");
                }
            }
        }
    }

    @EventHandler
    public void onWorldeditMove(WorldEditMoveEvent event) {

            Player p = event.getPlayer();
            BauWorld bauWorld = WorldManager.get(p.getWorld());
            if (bauWorld instanceof PlayerWorld) {
                if (!((PlayerWorld) bauWorld).hasRights(p.getUniqueId())) {
                    return;
                }
            }

            BlockVector3 offset = event.getOffset();
            Region origin = event.getRegion();
            Location location = p.getLocation();
            CannonTimer cannonTimer = bauWorld.getCannonTimer(location);
            if (cannonTimer == null)
                return;

            for (BlockVector3 block : origin) {
                Loc loc = Loc.getByBlockVector(block);
                CannonTimerBlock cannonTimerBlock = cannonTimer.getBlock(loc);
                if (cannonTimerBlock != null) {
                    cannonTimer.moveBlock(cannonTimerBlock, Loc.getByBlockVector(offset));
                }
            }
    }
}
