package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.world.BauWorld;
import net.wargearworld.bau.world.PlayerWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;

public class CannonTimerListener implements TabExecutor, Listener {
    public static Material toolMaterial;
    public static Material blockMaterial;
    public static Material activeMaterial;
    public static Material inactiveMaterial;
    public static final int MAX_TNT_PER_BLOCK = Main.getPlugin().getCustomConfig().getInt("cannontimer.maxamount");
    public static final int MAX_TICKS = 100;
    private CommandHandel commandHandel;

    public CannonTimerListener(Main main) {
        toolMaterial = Material.valueOf(main.getCustomConfig().getString("cannontimer.tool"));
        blockMaterial = Material.valueOf(main.getCustomConfig().getString("cannontimer.block.default"));
        activeMaterial = Material.valueOf(main.getCustomConfig().getString("cannontimer.block.active"));
        inactiveMaterial = Material.valueOf(main.getCustomConfig().getString("cannontimer.block.inactive"));
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

        updatePaperNMS();

    }

    /**
     * @author tormbav
     */
    private void updatePaperNMS() {
        try {
            // Get the server package version.
            // In 1.14, the package that the server class CraftServer is in, is called "org.bukkit.craftbukkit.v1_14_R1".
            String packageVersion = Main.getPlugin().getServer().getClass().getPackage().getName().split("\\.")[3];
            // Convert a Material into its corresponding Item by using the getItem method on the Material.
            Class<?> magicClass = Class.forName("org.bukkit.craftbukkit." + packageVersion + ".util.CraftMagicNumbers");
            Method method = magicClass.getDeclaredMethod("getItem", Material.class);
            Object item = method.invoke(null, Material.PAPER);
            // Get the maxItemStack field in Item and change it.
            Class<?> itemClass = Class.forName("net.minecraft.server." + packageVersion + ".Item");
            Field field = itemClass.getDeclaredField("maxStackSize");
            field.setAccessible(true);
            field.setInt(item, MAX_TICKS);
            // Change the maxStack field in the Material.
            Field mf = Material.class.getDeclaredField("maxStack");
            mf.setAccessible(true);
            mf.setInt(Material.PAPER, MAX_TICKS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        commandHandel.execute(commandPlayer, MessageHandler.getInstance().getLanguage(p), args);
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
        commandHandel.tabComplete(commandPlayer, MessageHandler.getInstance().getLanguage(p), args, out);
        return out;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        BauWorld bauWorld = WorldManager.get(p.getWorld());
        if (bauWorld instanceof PlayerWorld) {
            if (!((PlayerWorld) bauWorld).hasRights(p.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
        CannonTimer cannonTimer = bauWorld.getCannonTimer(p.getLocation());
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        Location blockLocation = event.getClickedBlock().getLocation();
            Material type = event.getClickedBlock().getType();
            if (type == blockMaterial || type == activeMaterial || type == inactiveMaterial) {
                //openGUI
                event.setCancelled(true);
                CannonTimerBlock cannonTimerBlock = cannonTimer.getBlock(blockLocation);
                if (p.isSneaking() && event.getItem().getType() == toolMaterial) {
                    BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
                    try {
                        bauPlayer.setMovingCannonTimerBlock(cannonTimerBlock.clone());
                    } catch (CloneNotSupportedException e) {
                    }
                    MessageHandler.getInstance().send(p, "cannonTimer_copied");
                    return;
                }
                if (cannonTimerBlock != null)
                    CannonTimerGUI.openMain(event.getPlayer(), cannonTimerBlock, 1);
            } else {
                if (event.getItem().getType() == toolMaterial) {
                    cannonTimer.start(p);
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() !=null &&event.getItem().getType() == toolMaterial && p.isSneaking()) {
            event.setCancelled(true);
            BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
            CannonTimerBlock cannonTimerBlock = bauPlayer.getCopiedCannonTimerBlock();
            if (cannonTimerBlock == null) {
                MessageHandler.getInstance().send(p, "cannonTimer_no_copy");
                return;
            }
            cannonTimer.setBlock(event.getClickedBlock().getLocation(), cannonTimerBlock);
            MessageHandler.getInstance().send(p, "cannonTimer_pasted");
            return;
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
        if (event.getBlock().getType().name().contains("SHULKER_BOX")) {
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
        if (type == blockMaterial || type == activeMaterial || type == inactiveMaterial) {
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
}
