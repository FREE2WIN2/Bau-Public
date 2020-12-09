package net.wargearworld.bau.advancement.listener;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.advancement.AdvTemplate;
import net.wargearworld.bau.advancement.event.PlayerSaveTestBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class AdvSaveTestBlock extends AdvTemplate {
    public AdvSaveTestBlock() {
        super("savetb");
    }

    @EventHandler
    public void onSaveTB(PlayerSaveTestBlockEvent event) {
        grantSingleCriterion(event.getPlayer());
    }
}