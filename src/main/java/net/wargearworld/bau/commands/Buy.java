package net.wargearworld.bau.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import net.wargearworld.db.model.enums.TransactionType;
import net.wargearworld.economy.core.account.Account;
import net.wargearworld.economy.core.account.Transaction;
import net.wargearworld.economy.core.exception.NegativeAmountException;
import net.wargearworld.economy.core.exception.NotEnoughMoneyException;
import net.wargearworld.economy.core.utils.EconomyFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Text;

import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.command_manager.nodes.ArgumentNode.argument;
import static net.wargearworld.command_manager.arguments.DynamicListArgument.dynamicList;
import static net.wargearworld.command_manager.nodes.InvisibleNode.invisible;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Buy implements TabExecutor {

    private CommandHandel commandHandel;

    public Buy(JavaPlugin plugin) {
        plugin.getCommand("buy").setTabCompleter(this);
        plugin.getCommand("buy").setExecutor(this);

        commandHandel = new CommandHandel("buy", Main.prefix, MessageHandler.getInstance());
        commandHandel
                .addSubNode(literal("template")
                        .addSubNode(argument("TemplateName", dynamicList("TemplateName", s -> {
                            Set<String> out = new TreeSet<>();
                            PlayerDAO.getPlayersTeamplates(s.getPlayer().getUUID()).entrySet().forEach(entry -> {
                                if (!entry.getValue())
                                    out.add(entry.getKey().getName());
                            });
                            return out;
                        })).setCallback(s -> {
                            buyTemplate(s, false);
                        })
                                .addSubNode(invisible(literal("confirm").setCallback(s -> {
                                    buyTemplate(s, true);
                                })))));
    }

    private void buyTemplate(ArgumentList s, boolean b) {
        Player p = getPlayer(s);
        MessageHandler msgHandler = MessageHandler.getInstance();
            WorldTemplate worldTemplate = WorldTemplate.getTemplate(s.getString("TemplateName"));
            EconomyFormatter economyFormatter = EconomyFormatter.getInstance();
        if (b) {
            Account senderAccount = Account.getByUUID(p.getUniqueId());
            Transaction transaction = new Transaction(senderAccount, null, worldTemplate.getPrice(), TransactionType.PAY);
            transaction.setSenderMessageKey("MONEY.BUY.TEMPLATE");
            transaction.setSenderMessageArguments(List.of(worldTemplate.getName(), worldTemplate.getPrice() + ""));
            try {
                transaction.execute();
                PlayerDAO.addPlotTemplate(worldTemplate, p.getUniqueId());
                msgHandler.send(p, "worldtemplate_bought", worldTemplate.getName(), economyFormatter.format(worldTemplate.getPrice()), worldTemplate.getName());
            } catch (NotEnoughMoneyException e) {
                msgHandler.send(p, "not_enough_money_template", (worldTemplate.getPrice() - senderAccount.getBalance()) + economyFormatter.getCurrencySymbol(), worldTemplate.getName());
            } catch (NegativeAmountException e) {
            }
        } else {
            TextComponent tc = new TextComponent(Main.prefix + msgHandler.getString(p, "worldtemplate_buy_confirm",worldTemplate.getName(), economyFormatter.format(worldTemplate.getPrice())));
            TextComponent click = new TextComponent(msgHandler.getString(p,"worldtemplate_buy_confirm_button"));
            click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msgHandler.getString(p,"worldtemplate_buy_confirm_button_hover",worldTemplate.getName())).create()));
            click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/buy template " + worldTemplate.getName() + " confirm"));
            p.spigot().sendMessage(tc,click);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command arg1,
                                      String arg2, String[] args) {
        Player p = (Player) sender;
        List<String> ret = new ArrayList<>();
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        commandHandel.tabComplete(commandPlayer, MessageHandler.getInstance().getLanguage(p), args, ret);
        return ret;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player p = (Player) sender;
        BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
        return commandHandel.execute(commandPlayer, MessageHandler.getInstance().getLanguage(p), args);
    }
}
