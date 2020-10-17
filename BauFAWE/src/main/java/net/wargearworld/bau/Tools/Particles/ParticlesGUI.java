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

public class ParticlesGUI {

    public static void open(Player p) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        GUI gui = new ChestGUI(9, "§6Particles§7GUI");
        Item clipOn = new DefaultItem(Material.MAP, msgHandler.getString(p, "particles_gui_clipOn"), s -> {
            Particles.setActive(ParticleContent.CLIPBOARD,true,s.getPlayer());
            open(p);
        });
        Item clipOff = new DefaultItem(Material.FILLED_MAP, msgHandler.getString(p, "particles_gui_clipOff"), s -> {
            Particles.setActive(ParticleContent.CLIPBOARD,false,s.getPlayer());
            open(p);
        });
        Item selOn = new DefaultItem(Material.STICK, msgHandler.getString(p, "particles_gui_selOn"), s -> {
            Particles.setActive(ParticleContent.SELECTION,true,s.getPlayer());
            open(p);
        });
        Item selOff = new DefaultItem(Material.BLAZE_ROD, msgHandler.getString(p, "particles_gui_selOff"), s -> {
            Particles.setActive(ParticleContent.SELECTION,false,s.getPlayer());
            open(p);
        });
        Item colorClip = new DefaultItem(Material.GLOBE_BANNER_PATTERN, msgHandler.getString(p, "particles_gui_changeColorClipboard"), s -> {
            openColorGUI(ParticleContent.CLIPBOARD, MessageHandler.getInstance().getString(p, "particles_gui_changeColorClipboardTitle"), p);
        });
        Item colorSel = new DefaultItem(Material.WOODEN_AXE,
                msgHandler.getString(p, "particles_gui_changeColorSelection"), s -> {
            openColorGUI(ParticleContent.SELECTION, MessageHandler.getInstance().getString(p, "particles_gui_changeColorSelectionTitle"), p);
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
        }, selOff);
        gui.setItem(3, s -> {
            return !particlesShow.isSelectionActive();
        }, selOn);

        gui.setItem(5, colorClip);

        gui.setItem(7, colorSel);
        gui.open(p);
    }

    private static void openColorGUI(ParticleContent content, String name, Player p) {
        ChestGUI gui = new ChestGUI(18, name);
        Executor<ArgumentList> executor = s -> {
            s.getPlayer().performCommand("particles " + content.name().toLowerCase() + " " + getColor(s.getClicked()));
            s.getPlayer().closeInventory();
        };
        gui.setItem(0,build(Material.BLACK_DYE, executor));
        gui.setItem(1,build(Material.BLUE_DYE, executor));
        gui.setItem(2,build(Material.BROWN_DYE, executor));
        gui.setItem(3,build(Material.CYAN_DYE, executor));
        gui.setItem(4,build(Material.GRAY_DYE, executor));
        gui.setItem(5,build(Material.GREEN_DYE, executor));
        gui.setItem(6,build(Material.LIGHT_BLUE_DYE, executor));
        gui.setItem(7,build(Material.LIGHT_GRAY_DYE, executor));
        gui.setItem(8,build(Material.LIME_DYE, executor));
        gui.setItem(9,build(Material.MAGENTA_DYE, executor));
        gui.setItem(10,build(Material.ORANGE_DYE, executor));
        gui.setItem(11,build(Material.PINK_DYE, executor));
        gui.setItem(12,build(Material.PURPLE_DYE, executor));
        gui.setItem(13,build(Material.RED_DYE, executor));
        gui.setItem(14,build(Material.WHITE_DYE, executor));
        gui.setItem(15,build(Material.YELLOW_DYE, executor));
        gui.open(p);
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
