package net.wargearworld.bau.tools.particles;

import net.wargearworld.GUI_API.Executor;
import net.wargearworld.GUI_API.GUI.ArgumentList;
import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.DefaultItem;
import net.wargearworld.GUI_API.Items.Item;
import net.wargearworld.bau.MessageHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static net.wargearworld.GUI_API.Items.ItemBuilder.build;

public class ParticlesGUI implements Listener {

    public static void open(Player p) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        GUI gui = new ChestGUI(9, "§6Particles§7GUI");
        gui.setCloseable(false);
        Item clipOn = new DefaultItem(Material.MAP, msgHandler.getString(p, "particles_gui_clipOn"), s -> {
        });
        Item clipOff = new DefaultItem(Material.FILLED_MAP, msgHandler.getString(p, "particles_gui_clipOff"), s -> {
        });
        Item selOn = new DefaultItem(Material.STICK, msgHandler.getString(p, "particles_gui_selOn"), s -> {
        });
        Item selOff = new DefaultItem(Material.BLAZE_ROD, msgHandler.getString(p, "particles_gui_selOff"), s -> {
        });
        Item colorClip = new DefaultItem(Material.GLOBE_BANNER_PATTERN, msgHandler.getString(p, "particles_gui_changeColorClipboard"), s -> {
        });
        Item colorSel = new DefaultItem(Material.WOODEN_AXE,
                msgHandler.getString(p, "particles_gui_changeColorSelection"), s -> {
        });

        ParticlesShow particlesShow = Particles.playersParticlesShow.get(p.getUniqueId());
        gui.setItem(1, s -> {
            return particlesShow.isClipboardActive();
        }, clipOff);
        gui.setItem(1, s -> {
            return !particlesShow.isClipboardActive();
        }, clipOn);

        gui.setItem(3, s -> {
            return particlesShow.isSelectionActive();
        }, selOn);
        gui.setItem(3, s -> {
            return !particlesShow.isSelectionActive();
        }, selOff);

        gui.setItem(5, colorClip);

        gui.setItem(7, colorSel);
        gui.open(p);
    }

    private static void openColorGUI(String name, Player p) {
        ChestGUI gui = new ChestGUI(9, name);
        Executor<ArgumentList> executor = s -> {
            s.getPlayer().performCommand("particles clipboard " + getColor(s.getClicked()));
        };
        gui.addItem(build(Material.BLACK_DYE, executor));
        gui.addItem(build(Material.BLUE_DYE, executor));
        gui.addItem(build(Material.BROWN_DYE, executor));
        gui.addItem(build(Material.CYAN_DYE, executor));
        gui.addItem(build(Material.GRAY_DYE, executor));
        gui.addItem(build(Material.GREEN_DYE, executor));
        gui.addItem(build(Material.LIGHT_BLUE_DYE, executor));
        gui.addItem(build(Material.LIGHT_GRAY_DYE, executor));
        gui.addItem(build(Material.LIME_DYE, executor));
        gui.addItem(build(Material.MAGENTA_DYE, executor));
        gui.addItem(build(Material.ORANGE_DYE, executor));
        gui.addItem(build(Material.PINK_DYE, executor));
        gui.addItem(build(Material.PURPLE_DYE, executor));
        gui.addItem(build(Material.RED_DYE, executor));
        gui.addItem(build(Material.WHITE_DYE, executor));
        gui.addItem(build(Material.YELLOW_DYE, executor));
        gui.open(p);
    }

    @EventHandler
    public void onClickInInventory(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        Player p = (Player) event.getWhoClicked();
        if (clicked == null) {
            return;
        }
        if (event.getView().getTitle().equals("§6Particles§7GUI")) {
            String clickedName = clicked.getItemMeta().getDisplayName();
            if (clickedName.equalsIgnoreCase(MessageHandler.getInstance().getString(p, "particles_gui_toggleOn"))) {
                p.performCommand("particles on");
                p.closeInventory();
            } else if (clickedName.equalsIgnoreCase(MessageHandler.getInstance().getString(p, "particles_gui_toggleOff"))) {
                p.performCommand("particles off");
                p.closeInventory();
            } else if (clickedName
                    .equalsIgnoreCase(MessageHandler.getInstance().getString(p, "particles_gui_changeColorClipboard"))) {
                openColorGUI(MessageHandler.getInstance().getString(p, "particles_gui_changeColorClipboardTitle"), p);
            } else if (clickedName
                    .equalsIgnoreCase(MessageHandler.getInstance().getString(p, "particles_gui_changeColorSelection"))) {
                openColorGUI(MessageHandler.getInstance().getString(p, "particles_gui_changeColorSelectionTitle"), p);
            }
            event.setCancelled(true);
        } else if (event.getView().getTitle().equals(MessageHandler.getInstance().getString(p, "particles_gui_changeColorClipboardTitle"))) {
            event.setCancelled(true);
            p.closeInventory();
            p.performCommand("particles clipboard " + getColor(clicked));

        } else if (event.getView().getTitle().equals(MessageHandler.getInstance().getString(p, "particles_gui_changeColorSelectionTitle"))) {
            event.setCancelled(true);
            p.closeInventory();
            p.performCommand("particles selection " + getColor(clicked));
        }

    }


    private static String getColor(ItemStack clicked) {
        Material m = clicked.getType();
        if (m.name().contains("DYE")) {
            return m.name().replace("_DYE", "");
        } else {
            return "BLACK";
        }
    }
}
