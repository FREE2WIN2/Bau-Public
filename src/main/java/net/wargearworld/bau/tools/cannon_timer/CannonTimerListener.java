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

import java.util.ArrayList;
import java.util.List;

public class CannonTimerListener implements TabExecutor, Listener {
    public static Material toolMaterial;
    public static Material blockMaterial;
    public static Material activeMaterial;
    public static Material inactiveMaterial;
    public static final int MAX_TNT_PER_BLOCK = Main.getPlugin().getCustomConfig().getInt("cannontimer.maxamount");

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
        if (event.getItem().getType() != toolMaterial) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Material type = event.getClickedBlock().getType();
            if ((type == activeMaterial || type == inactiveMaterial) && event.getClickedBlock() != null) {
                //openGUI
                event.setCancelled(true);
                Location loc = event.getClickedBlock().getLocation();
                CannonTimerBlock cannonTimerBlock = bauWorld.getCannonTimer().getBlock(loc);
                CannonTimerGUI.open(event.getPlayer(), cannonTimerBlock,1);
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR){
            CannonTimer cannonTimer = bauWorld.getCannonTimer();
            cannonTimer.start(p);
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

        if (event.getBlock().getType() == blockMaterial) {
            //openGUI
            Location loc = event.getBlockPlaced().getLocation();
            CannonTimerBlock cannonTimerBlock = new CannonTimerBlock(loc);
            bauWorld.getCannonTimer().addBlock(loc, cannonTimerBlock);
            CannonTimerGUI.open(event.getPlayer(), cannonTimerBlock,1);
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
            bauWorld.getCannonTimer().removeBlock(loc);
            MessageHandler.getInstance().send(p, "cannonTimer_removed");
        }
    }
}
