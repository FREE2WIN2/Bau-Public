package net.wargearworld.bau.world;

import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.*;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.world.bauworld.BauWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class WorldGUI implements Listener {

    public WorldGUI(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void openMain(Player p) {
        GUI mainGUI = new ChestGUI(27, MessageHandler.getInstance().getString(p, "worldGUI_title"));
        HeadItem item = new HeadItem(new CustomHead("world"), s -> {
            openTemplates(s.getPlayer());
        });
        mainGUI.addItem(item);
        mainGUI.open(p);
    }

    public static void openTemplates(Player p) {
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        BauWorld bauWorld = WorldManager.get(p.getWorld());
        Map<WorldTemplate, Boolean> playersTemplates = PlayerDAO.getPlayersTeamplates(bauPlayer.getUuid());
        int invSize = (playersTemplates.size() + 8) / 9;
        GUI gui = new ChestGUI(invSize * 9, MessageHandler.getInstance().getString(p, "worldTemplateGUI_title"));
        for (Map.Entry<WorldTemplate, Boolean> entry : playersTemplates.entrySet()) {
            Item worldTemplateItem = entry.getKey().getItem(bauPlayer.getUuid());
            String prefix = "§c";
            if (entry.getValue()) {
                prefix = "§3";
            }
            if (bauWorld.getTemplate().equals(entry.getKey())) { //Doenst work!
                worldTemplateItem.addEnchantment(Enchantment.VANISHING_CURSE, 1);
                worldTemplateItem.addItemFLags(ItemFlag.HIDE_ENCHANTS);
            }
            //ODO addLore
            worldTemplateItem.setName(prefix + entry.getKey().getName());
            worldTemplateItem.setExecutor(s -> {
                //TODO
            });

            gui.addItem(worldTemplateItem);
        }
        gui.open(p);
    }

}
