package net.wargearworld.bau.advancement.listener;

import net.wargearworld.bau.advancement.AdvTemplate;
import net.wargearworld.bau.advancement.event.PlayerEditTestBlockEvent;
import net.wargearworld.bau.advancement.event.PlayerUseCannonReloaderEvent;
import org.bukkit.event.EventHandler;

public class AdvCannonReload extends AdvTemplate {
    public AdvCannonReload() {
        super("reload");
    }
    @EventHandler
    public void onEditTB(PlayerUseCannonReloaderEvent event) {
        grantSingleCriterion(event.getPlayer());
    }

}