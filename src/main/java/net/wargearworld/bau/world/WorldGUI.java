package net.wargearworld.bau.world;

import net.wargearworld.GUI_API.GUI.ChestGUI;
import net.wargearworld.GUI_API.GUI.GUI;
import net.wargearworld.GUI_API.Items.*;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.db.model.enums.TransactionType;
import net.wargearworld.economy.core.account.Account;
import net.wargearworld.economy.core.account.Transaction;
import net.wargearworld.economy.core.exception.NegativeAmountException;
import net.wargearworld.economy.core.exception.NotEnoughMoneyException;
import net.wargearworld.economy.core.utils.EconomyFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
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
        MessageHandler msgHandler = MessageHandler.getInstance();
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        BauWorld bauWorld = WorldManager.get(p.getWorld());
        Map<WorldTemplate, Boolean> playersTemplates = PlayerDAO.getPlayersTeamplates(bauPlayer.getUuid());
        int invSize = (playersTemplates.size() + 8) / 9;
        GUI gui = new ChestGUI(invSize * 9, MessageHandler.getInstance().getString(p, "worldTemplateGUI_title"));
        EconomyFormatter economyFormatter = EconomyFormatter.getInstance();
        for (Map.Entry<WorldTemplate, Boolean> entry : playersTemplates.entrySet()) {
            WorldTemplate worldTemplate = entry.getKey();
            Item worldTemplateItem = entry.getKey().getItem(bauPlayer.getUuid());
            String prefix = "§c";

            worldTemplateItem.addLore("§8§m                    ");
            worldTemplateItem.addLore(" ");
            if (entry.getValue() || worldTemplate.getPrice() == 0) {
                if (!entry.getValue()) {
                    PlayerDAO.addPlotTemplate(worldTemplate, p.getUniqueId());
                }
                prefix = "§3";
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_available_lore"));
                worldTemplateItem.setExecutor(s -> {
                    if (bauWorld.isOwner(s.getPlayer())) {
                        s.getPlayer().closeInventory();
                        s.getPlayer().performCommand("gs setTemplate " + worldTemplate.getName());
                    }
                });
            } else {
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_not_available_lore"));
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_price_lore", economyFormatter.format(worldTemplate.getPrice())));
                worldTemplateItem.setExecutor(s -> {
                    p.performCommand("buy template " + worldTemplate.getName());
                    p.getOpenInventory().close();
                });
            }
            if (bauWorld.getTemplate().equals(entry.getKey())) {
                worldTemplateItem.addLore(msgHandler.getString(p, "worldTemplateGUI_template_active_lore"));
                worldTemplateItem.setExecutor(s -> {
                    if (bauWorld.isOwner(s.getPlayer())) {
                        s.getPlayer().performCommand("gs new");
                        s.getPlayer().closeInventory();
                    }
                });
            }
            //ODO addLore
            worldTemplateItem.setName(prefix + entry.getKey().getName());


            gui.addItem(worldTemplateItem);
        }
        gui.open(p);
    }

}
