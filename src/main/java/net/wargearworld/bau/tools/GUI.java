package net.wargearworld.bau.tools;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.wargearworld.GUI_API.Executor;
import net.wargearworld.GUI_API.GUI.ArgumentList;
import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.Items.*;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.tools.cannon_reloader.AutoCannonReloader;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.worldedit.WorldGuardHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GUI implements CommandExecutor, Listener {

    public GUI(JavaPlugin plugin){
        plugin.getCommand("gui").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onclick(PlayerInteractEvent e) {

        Player p = (Player) e.getPlayer();
        ItemStack is = e.getItem();
        if (!(is == null)) {
            if (is.getItemMeta().getDisplayName().equals("§6GUI") && (e.getAction().equals(Action.RIGHT_CLICK_AIR)
                    || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
                p.performCommand("gui");
            } else if (is.getType().equals(Material.SPAWNER)) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Inventory inv;
        if (sender instanceof Player) {
            Player p = (Player) sender;
            openGUI(p);
        }
        return true;
    }

    public void openGUI(Player p) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        String worldname = getName(p.getWorld());
        String inventoryName = MessageHandler.getInstance().getString(p, "inventoryName").replace("%r", worldname);

        ChestGUI gui = new ChestGUI(45, inventoryName);

        Executor<ArgumentList> tntExecutor = s -> {
            Player player = s.getPlayer();
            player.performCommand("tnt");
            player.closeInventory();
        };
        Item tntActiveItem = ItemBuilder.build(tntExecutor, Material.TNT, 1, ItemType.DEFAULT, msgHandler.getString(p, "tntAllowed"))
                .addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1).addItemFLags(ItemFlag.HIDE_ENCHANTS);

        Item tntNotActiveItem = ItemBuilder.build(tntExecutor, Material.TNT, 1, ItemType.DEFAULT, msgHandler.getString(p, "tntDenied"));

        HeadItem playerHead = new HeadItem(p.getName(),msgHandler.getString(p, "guiMember").replace("%r", worldname), 1);
        playerHead.setExecutor(s -> {
            s.getPlayer().performCommand("gs info");
            s.getPlayer().closeInventory();
        });
        String rgID = WorldGuardHandler.getPlotId(p.getLocation());
        Item reset = ItemBuilder.build(s -> {
            Player player = s.getPlayer();
            player.closeInventory();
            PlotResetter.resetRegion(player, false);
        }, Material.BARRIER, 1, ItemType.DEFAULT, msgHandler.getString(p, "deletePlot",rgID.replace("plot","")));

        DefaultItem guiItem = new DefaultItem(Material.NETHER_STAR, "§6GUI", 1);
        guiItem.setCancelled(false);

        DefaultItem debugStick = new DefaultItem(new ItemStack(Material.DEBUG_STICK), s -> {});
        debugStick.setCancelled(false);

        Item teleporter = new DefaultItem(Material.ENDER_PEARL,msgHandler.getString(p,"gui_teleporter"),1).setExecutor(s->{PlotTeleporter.openInv(s.getPlayer());});

        Executor<ArgumentList> stoplag = s->{
            Player player = s.getPlayer();
            player.closeInventory();
        player.performCommand("sl");};
        Item stoplagOff = new DefaultItem(Material.REDSTONE,msgHandler.getString(p, "torchOff"),1).setExecutor(stoplag);
        Item stoplagOn = new DefaultItem(Material.GUNPOWDER,msgHandler.getString(p, "torchOn"),1).setExecutor(stoplag);

        Item trailGUI = new DefaultItem(Material.OBSERVER,msgHandler.getString(p,"tbs_gui_trail"),1).setExecutor(s->{s.getPlayer().performCommand("trail gui");});

        Item testBlockSlave = new DefaultItem(Material.WHITE_WOOL,msgHandler.getString(p,"testBlockSklaveGui"),1).setExecutor(s->{s.getPlayer().performCommand("tbs");});

        Material ferzuenerMaterial =  Material.valueOf(Main.getPlugin().getCustomConfig().getString("fernzuender"));
        Item fernzuenderItem = new DefaultItem(ferzuenerMaterial,msgHandler.getString(p,"fernzuender"),1);
        fernzuenderItem.setCancelled(false);

        Item cannonReloaderItem = new DefaultItem(AutoCannonReloader.toolMaterial,msgHandler.getString(p,"cannonReloader_guiName"),1);
        cannonReloaderItem.setCancelled(false);

        Item designToolOn = new DefaultItem(Material.WOODEN_SHOVEL,msgHandler.getString(p, "dtItemOn"),1).setExecutor(s->{s.getPlayer().performCommand("dt");s.getPlayer().closeInventory();});
        Item designToolOff = new DefaultItem(Material.WOODEN_SHOVEL,msgHandler.getString(p, "dtItemOff"),1).setExecutor(s->{s.getPlayer().performCommand("dt");s.getPlayer().closeInventory();});
        designToolOn.addEnchantment(Enchantment.SILK_TOUCH, 1).addItemFLags(ItemFlag.HIDE_ENCHANTS);
        /* Set Items into GUI */

        gui.setItem(0, guiItem);
        gui.setItem(8, guiItem);
        gui.setItem(36, guiItem);
        gui.setItem(44, guiItem);

        gui.setItem(19, s -> {
            ProtectedRegion rg = WorldGuardHandler.getRegion(s.getPlayer().getLocation());
            return (rg.getFlag(Main.TntExplosion) == State.DENY);
        }, tntNotActiveItem);
        gui.setItem(19, s -> {
            ProtectedRegion rg = WorldGuardHandler.getRegion(s.getPlayer().getLocation());
            return (rg.getFlag(Main.TntExplosion) == State.ALLOW);
        }, tntActiveItem);

        gui.setItem(13, reset);


        gui.setItem(3, trailGUI);
        gui.setItem(5, testBlockSlave);
        gui.setItem(11, fernzuenderItem);
        gui.setItem(15, cannonReloaderItem);
        gui.setItem(22, playerHead);
        gui.setItem(25,s->{return Stoplag.getStatus(p.getLocation());}, stoplagOff);
        gui.setItem(25,s->{return !Stoplag.getStatus(p.getLocation());}, stoplagOn);
        gui.setItem(29, debugStick);
        gui.setItem(31, teleporter);

        gui.setItem(33,s->{return BauPlayer.getBauPlayer(s.getPlayer()).getDT();}, designToolOn);
        gui.setItem(33,s->{return !BauPlayer.getBauPlayer(s.getPlayer()).getDT();}, designToolOff);

        gui.setItem(39, new DefaultItem(Material.MELON_SEEDS, msgHandler.getString(p, "gui_particles"), s->{s.getPlayer().performCommand("particles gui");}));
        gui.setItem(41, new DefaultItem(TntChest.getTNTChest(), s -> {}).setCancelled(false));

        gui.open(p);
    }

    public static String getName(World w) {
        if (w.getName().contains("test")) {
            return w.getName();
        } else {
            return WorldManager.getWorld(w.getUID()).getName();
        }
    }
}
