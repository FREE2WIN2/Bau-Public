package net.wargearworld.bau.advancement.listener;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.advancement.AdvTemplate;
import net.wargearworld.bau.advancement.event.PlayerEditTestBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class AdvTestBlockEditor extends AdvTemplate {
    public AdvTestBlockEditor() {
        super("edittb");
    }
    @EventHandler
    public void onEditTB(PlayerEditTestBlockEvent event) {
        grantSingleCriterion(event.getPlayer());
    }

}
