package net.wargearworld.Bau.TabCompleter;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.Tools.Particles.Colors;
import net.wargearworld.Bau.utils.HelperMethods;
import net.wargearworld.StringGetter.Language;

public class particlesTC implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String string, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		Player p = (Player) sender;
		Language lang = Language.DE;
		if (MessageHandler.playersLanguage.containsKey(p.getUniqueId())) {
			lang = MessageHandler.playersLanguage.get(p.getUniqueId());
		}
		List<String> out = new LinkedList<>();
		if (args.length == 1) {
			if (lang == Language.EN) {
				out.add("on");
				out.add("off");
			} else {
				out.add("an");
				out.add("aus");
			}
			out.add("clipboard");
			out.add("selection");
			out.add("gui");
			return HelperMethods.checkFortiped(args[0], out);
		}
		if(args.length == 2) {
			for(Colors color: Colors.values()) {
				out.add(color.name());
			}
			out.add("<r>");
			return HelperMethods.checkFortiped(args[1], out);
		}
		if(args.length == 3) {
			out.add("<g>");
			return out;
		}
		if(args.length == 4) {
			out.add("<b>");
			return out;
		}
		return null;
	}

}
