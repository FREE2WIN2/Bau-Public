package net.wargearworld.bau.world;

import net.wargearworld.StringGetter.Language;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.command_manager.CommandHandel;
import net.wargearworld.commandframework.player.BukkitCommandPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static net.wargearworld.bau.utils.CommandUtil.getPlayer;
public class WorldCommand implements TabExecutor {

    private CommandHandel handle;

    public WorldCommand(JavaPlugin plugin) {
        plugin.getCommand("worlds").setExecutor(this);
        plugin.getCommand("worlds").setTabCompleter(this);
        handle = new CommandHandel("worlds", Main.prefix, MessageHandler.getInstance());

        handle.setCallback(s -> {
            WorldGUI.openMain(getPlayer(s));
        });

    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command arg1, String arg2, String[] args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            BukkitCommandPlayer commandPlayer = new BukkitCommandPlayer(p);
            handle.execute(commandPlayer, Language.DE, args);
        }
        return true;
    }
}
