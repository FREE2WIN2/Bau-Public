package net.wargearworld.bau.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.HelperMethods;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.LocalWorldTemplate;
import net.wargearworld.bau.world.gui.WorldGUI;
import net.wargearworld.command_manager.ArgumentList;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import net.wargearworld.db.EntityManagerExecuter;
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

import static net.wargearworld.command_manager.nodes.LiteralNode.literal;
import static net.wargearworld.command_manager.nodes.ArgumentNode.argument;
import static net.wargearworld.command_manager.arguments.DynamicListArgument.dynamicList;
import static net.wargearworld.command_manager.nodes.InvisibleNode.invisible;
import static net.wargearworld.command_manager.arguments.StringArgument.string;
import static net.wargearworld.bau.utils.CommandUtil.getPlayer;

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

        commandHandel.addSubNode(literal("world")
                .setCallback(s->{buyWorld(s,false);})
                .addSubNode(argument("WorldName", string())
                        .setCallback(s -> {buyWorld(s, false);})
                        .addSubNode(invisible(literal("confirm")
                                .setCallback(s -> { buyWorld(s, true);})))));
    }

    private void buyWorld(ArgumentList s, boolean confirmed) {
        Player p = getPlayer(s);
        int amountOfWorlds = EntityManagerExecuter.run(em->{return BauPlayer.getBauPlayer(p).getdbWorlds().size();});
        MessageHandler msgHandler = MessageHandler.getInstance();
        if(amountOfWorlds >= BauConfig.getInstance().getMaxworlds()){
            msgHandler.send(p,"max_worlds");
            return;
        }
        String worldName = s.getString("WorldName");
        EconomyFormatter economyFormatter = EconomyFormatter.getInstance();
        Double price = amountOfWorlds * BauConfig.getInstance().getWorldprice();
        Account senderAccount = Account.getByUUID(p.getUniqueId());
        if(worldName == null || worldName.equals("")){
            if(senderAccount.has(price)){
                WorldGUI.openBuyWorldName(p);
            }else{
                msgHandler.send(p, "not_enough_money_world", (price - senderAccount.getBalance()) + economyFormatter.getCurrencySymbol(), "");
            }
            return;
        }
        if(!HelperMethods.isAscii(worldName) || worldName.contains("/")){
            msgHandler.send(p,"world_name_notAllowedChars",worldName);
            return;        }
        if(WorldManager.getPlayerWorld(worldName,p.getUniqueId()) != null){
            msgHandler.send(p,"world_name_exists",worldName);
            return;
        }


        if (confirmed) {
            Transaction transaction = new Transaction(senderAccount, null, price, TransactionType.PAY);
            transaction.setSenderMessageKey("MONEY.BUY.WORLD");
            transaction.setSenderMessageArguments(List.of(price + ""));
            try {
                transaction.execute();
                PlayerDAO.addNewWorld(worldName,p.getUniqueId(),false);
                msgHandler.send(p, "world_bought", worldName, economyFormatter.format(price), worldName);
            } catch (NotEnoughMoneyException e) {
                msgHandler.send(p, "not_enough_money_world", (price - senderAccount.getBalance()) + economyFormatter.getCurrencySymbol(), worldName);
            } catch (NegativeAmountException e) {
            }
        } else {
            TextComponent tc = new TextComponent(Main.prefix + msgHandler.getString(p, "world_buy_confirm", worldName, economyFormatter.format(price)));
            TextComponent click = new TextComponent(msgHandler.getString(p, "world_buy_confirm_button"));
            click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msgHandler.getString(p, "world_buy_confirm_button_hover", worldName)).create()));
            click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/buy world " + worldName + " confirm"));
            p.spigot().sendMessage(tc, click);
        }
    }

    private void buyTemplate(ArgumentList s, boolean confirmed) {
        Player p = getPlayer(s);
        MessageHandler msgHandler = MessageHandler.getInstance();
        LocalWorldTemplate localWorldTemplate = LocalWorldTemplate.getTemplate(s.getString("TemplateName"));
        EconomyFormatter economyFormatter = EconomyFormatter.getInstance();
        if (confirmed) {
            Account senderAccount = Account.getByUUID(p.getUniqueId());
            Transaction transaction = new Transaction(senderAccount, null, localWorldTemplate.getPrice(), TransactionType.PAY);
            transaction.setSenderMessageKey("MONEY.BUY.TEMPLATE");
            transaction.setSenderMessageArguments(List.of(localWorldTemplate.getName(), localWorldTemplate.getPrice() + ""));
            try {
                transaction.execute();
                PlayerDAO.addWorldTemplate(localWorldTemplate, p.getUniqueId());
                msgHandler.send(p, "worldtemplate_bought", localWorldTemplate.getName(), economyFormatter.format(localWorldTemplate.getPrice()), localWorldTemplate.getName());
            } catch (NotEnoughMoneyException e) {
                msgHandler.send(p, "not_enough_money_template", (localWorldTemplate.getPrice() - senderAccount.getBalance()) + economyFormatter.getCurrencySymbol(), localWorldTemplate.getName());
            } catch (NegativeAmountException e) {
            }
        } else {
            TextComponent tc = new TextComponent(Main.prefix + msgHandler.getString(p, "worldtemplate_buy_confirm", localWorldTemplate.getName(), economyFormatter.format(localWorldTemplate.getPrice())));
            TextComponent click = new TextComponent(msgHandler.getString(p, "worldtemplate_buy_confirm_button"));
            click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msgHandler.getString(p, "worldtemplate_buy_confirm_button_hover", localWorldTemplate.getName())).create()));
            click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/buy template " + localWorldTemplate.getName() + " confirm"));
            p.spigot().sendMessage(tc, click);
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
