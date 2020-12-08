package net.wargearworld.bau.tools;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.wargearworld.GUI_API.Executor;
import net.wargearworld.GUI_API.GUI.ArgumentList;
import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.Items.*;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.CustomHeadValues;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.gui.WorldGUI;
import net.wargearworld.bau.world.plot.Plot;
import net.wargearworld.bau.worldedit.WorldGuardHandler;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.command_manager.player.CommandPlayer;
import net.wargearworld.command_manager.player.BukkitCommandPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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

import java.util.ArrayList;
import java.util.List;

import static net.wargearworld.bau.utils.CommandUtil.getPlayer;
import static net.wargearworld.command_manager.nodes.LiteralNode.literal;

public class GUI implements TabExecutor, Listener {

    private CommandHandel commandHandel;

    public GUI(JavaPlugin plugin) {
        plugin.getCommand("gui").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        commandHandel = new CommandHandel("gui", Main.prefix, MessageHandler.getInstance());
        commandHandel.setCallback(s -> {
            openGUI1(getPlayer(s));
        });
        commandHandel.addSubNode(literal("2").setCallback(s -> {
            openGUI2(getPlayer(s));
        }));
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
        if (sender instanceof Player) {
            Player p = (Player) sender;
            CommandPlayer commandPlayer = new BukkitCommandPlayer(p);
            commandHandel.execute(commandPlayer,  args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> out = new ArrayList<>();
        if (sender instanceof Player) {
            Player p = (Player) sender;
            CommandPlayer commandPlayer = new BukkitCommandPlayer(p);
            commandHandel.tabComplete(commandPlayer,  args, out);
        }
        return out;
    }

    public void openGUI2(Player p) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        String inventoryName = MessageHandler.getInstance().getString(p, "GUI_title", 2 + "");
        Plot plot = WorldManager.get(p.getWorld()).getPlot(p.getLocation());
        ChestGUI gui = new ChestGUI(27, inventoryName);
        Executor<ArgumentList> tntExecutor = createCommandExecutor("tnt");
        Item tntActiveItem = ItemBuilder.build(tntExecutor, Material.TNT, 1, ItemType.DEFAULT, msgHandler.getString(p, "tntAllowed"))
                .addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1).addItemFLags(ItemFlag.HIDE_ENCHANTS);

        Item tntNotActiveItem = ItemBuilder.build(tntExecutor, Material.TNT, 1, ItemType.DEFAULT, msgHandler.getString(p, "tntDenied"));
        Executor<ArgumentList> stoplag = createCommandExecutor("sl");


        Item stoplagOff = new DefaultItem(Material.GUNPOWDER, msgHandler.getString(p, "torchOff"), 1).setExecutor(stoplag);
        Item stoplagOn = new DefaultItem(Material.REDSTONE, msgHandler.getString(p, "torchOn"), 1).setExecutor(stoplag);

        Item trailGUI = new DefaultItem(Material.OBSERVER, msgHandler.getString(p, "tbs_gui_trail"), 1).setExecutor(s -> {
            s.getPlayer().performCommand("trail gui");
        });
        Item teleporter = new DefaultItem(Material.ENDER_PEARL, msgHandler.getString(p, "gui_teleporter"), 1).setExecutor(s -> {
            PlotTeleporter.openInv(s.getPlayer());
        });
        Item designToolOn = new DefaultItem(Material.WOODEN_SHOVEL, msgHandler.getString(p, "dtItemOn"), 1).setExecutor(s -> {
            s.getPlayer().performCommand("dt");
            s.getPlayer().closeInventory();
        });
        Item designToolOff = new DefaultItem(Material.WOODEN_SHOVEL, msgHandler.getString(p, "dtItemOff"), 1).setExecutor(s -> {
            s.getPlayer().performCommand("dt");
            s.getPlayer().closeInventory();
        });
        designToolOn.addEnchantment(Enchantment.SILK_TOUCH, 1).addItemFLags(ItemFlag.HIDE_ENCHANTS);


        Item prevPage = new HeadItem(new CustomHead(CustomHeadValues.ARROW_LEFT.getValue()), s -> {
            openGUI1(s.getPlayer());
        }).setName(msgHandler.getString(p, "gui_page1"));

        Item worldFuscatorActivate = new DefaultItem(Material.END_STONE, msgHandler.getString(p, "gui_wf_activate"), createCommandExecutor("wf"));
        Item worldFuscatorDeactivate = new DefaultItem(Material.END_STONE, msgHandler.getString(p, "gui_wf_deactivate"), createCommandExecutor("wf"));
        worldFuscatorDeactivate.addEnchantment(Enchantment.SILK_TOUCH, 1).addItemFLags(ItemFlag.HIDE_ENCHANTS);

        Item waterRemoverActivate = new DefaultItem(Material.WATER_BUCKET, msgHandler.getString(p, "gui_wr_activate"), createCommandExecutor("wr"));
        Item waterRemoverDeactivate = new DefaultItem(Material.BUCKET, msgHandler.getString(p, "gui_wr_deactivate"), createCommandExecutor("wr"));

        Item heads = new HeadItem(new CustomHead(CustomHeadValues.KING.getValue()), createCommandExecutor("head")).setName(msgHandler.getString(p, "gui_head"));

        Item guiItem = new DefaultItem(Material.NETHER_STAR, "§6GUI", 1).setCancelled(false);
        Item particleGUI = new DefaultItem(Material.MELON_SEEDS, msgHandler.getString(p, "gui_particles"), s -> {
            s.getPlayer().performCommand("particles gui");
        });
        Item tntChest = new DefaultItem(TntChest.getTNTChest(), s -> {}).setCancelled(false);

        gui.setItem(0, prevPage);
        gui.setItem(2, s -> plot.isWorldFuscated(), worldFuscatorDeactivate);
        gui.setItem(2, s -> !plot.isWorldFuscated(), worldFuscatorActivate);

        gui.setItem(4, trailGUI);
        gui.setItem(6, s -> Stoplag.getStatus(p.getLocation()), stoplagOff);
        gui.setItem(6, s -> !Stoplag.getStatus(p.getLocation()), stoplagOn);
        gui.setItem(8, guiItem);

        gui.setItem(10, s -> !plot.getTNT(), tntNotActiveItem);
        gui.setItem(10, s -> plot.getTNT(), tntActiveItem);
        gui.setItem(13, tntChest);
        gui.setItem(16, s -> plot.isWaterRemoverActive(), waterRemoverDeactivate);
        gui.setItem(16, s -> !plot.isWaterRemoverActive(), waterRemoverActivate);


        gui.setItem(18, guiItem);
        gui.setItem(20, heads);
        gui.setItem(22, particleGUI);
        gui.setItem(24, s -> BauPlayer.getBauPlayer(s.getPlayer()).getDT(), designToolOn);
        gui.setItem(24, s -> !BauPlayer.getBauPlayer(s.getPlayer()).getDT(), designToolOff);

        gui.setItem(26, guiItem);
        gui.open(p);
    }

    public void openGUI1(Player p) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        String inventoryName = MessageHandler.getInstance().getString(p, "GUI_title", 1 + "");
        BauWorld bauWorld = WorldManager.get(p.getWorld());

        ChestGUI gui = new ChestGUI(27, inventoryName);

        HeadItem playerHead = new HeadItem(p.getUniqueId(), msgHandler.getString(p, "guiMember").replace("%r", bauWorld.getName()), 1);
        playerHead.setExecutor(s -> {
            s.getPlayer().performCommand("gs info");
            s.getPlayer().closeInventory();
        });
        String rgID = WorldGuardHandler.getPlotId(p.getLocation());
        Item reset = ItemBuilder.build(s -> {
            Player player = s.getPlayer();
            player.closeInventory();
            PlotResetter.resetRegion(player, rgID, false);
        }, Material.BARRIER, 1, ItemType.DEFAULT, msgHandler.getString(p, "deletePlot", rgID.replace("plot", "")));


        Item guiItem = new DefaultItem(Material.NETHER_STAR, "§6GUI", 1).setCancelled(false);

        Item debugStick = new DefaultItem(new ItemStack(Material.DEBUG_STICK), s -> {
        }).setCancelled(false);

        Item testBlockSlave = new DefaultItem(Material.WHITE_WOOL, msgHandler.getString(p, "testBlockSklaveGui"), 1).setExecutor(s -> {
            s.getPlayer().performCommand("tbs");
        });

        Material ferzuenerMaterial = BauConfig.getInstance().getRemoteDetonatorTool();
        Item fernzuenderItem = new DefaultItem(ferzuenerMaterial, msgHandler.getString(p, "fernzuender"), 1).setCancelled(false);
//        fernzuenderItem.addLore(msgHandler.getString(p,"remote_detonator_lore_1")).addLore(msgHandler.getString(p,"remote_detonator_lore_2"));

        Item cannonReloaderItem = new DefaultItem(BauConfig.getInstance().getTntReloadItem(), msgHandler.getString(p, "cannonReloader_guiName"), 1).setCancelled(false);
        cannonReloaderItem.addLore(msgHandler.getString(p, "cannon_reloader_cannon_rightklick")).addLore(msgHandler.getString(p, "cannon_reloader_lore_leftklick"));

        Item nextPage = new HeadItem(new CustomHead(CustomHeadValues.ARROW_RIGHT.getValue()), s -> {
            openGUI2(s.getPlayer());
        }).setName(msgHandler.getString(p, "gui_page2"));

        Item worldGUI = new HeadItem(new CustomHead(CustomHeadValues.GLOBE2.getValue()), s -> {
            WorldGUI.openMain(s.getPlayer(), 1);
        }).setName(msgHandler.getString(p, "gui_worldGUI_icon"));

        Item cannonTimerClock = new DefaultItem(BauConfig.getInstance().getCannonTimerTool(), msgHandler.getString(p, "gui_cannon_timer_tool"), s -> {
        }).setCancelled(false).addLore(msgHandler.getString(p, "gui_cannon_timer_tool_lore_leftklick")).addLore(msgHandler.getString(p, "gui_cannon_timer_tool_lore_rightklick"));
        Item cannonTimerShulker = new DefaultItem(BauConfig.getInstance().getCannonTimerActiveBlock(), msgHandler.getString(p, "gui_cannon_timer_block"), s -> {
        }).setCancelled(false).addLore(msgHandler.getString(p, "gui_cannon_timer_block_lore"));
        /* Set Items into GUI */

        gui.setItem(0, guiItem);
        gui.setItem(2, debugStick);
        gui.setItem(4, reset);
        gui.setItem(6, cannonReloaderItem);
        gui.setItem(8, nextPage);

        gui.setItem(10, worldGUI);
        gui.setItem(13, playerHead);
        gui.setItem(16, cannonTimerClock);

        gui.setItem(18, guiItem);
        gui.setItem(20, fernzuenderItem);
        gui.setItem(22, testBlockSlave);
        gui.setItem(24, cannonTimerShulker);
        gui.setItem(26, guiItem);
        gui.open(p);
    }

    public static String getName(World w) {
        if (w.getName().contains("test")) {
            return w.getName();
        } else {
            return WorldManager.getWorld(w.getUID()).getName();
        }
    }

    private Executor<ArgumentList> createCommandExecutor(String command) {
        return s -> {
            s.getPlayer().closeInventory();
            s.getPlayer().performCommand(command);
        };
    }
}
