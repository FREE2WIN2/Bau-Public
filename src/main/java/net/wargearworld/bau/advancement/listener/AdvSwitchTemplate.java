package net.wargearworld.bau.advancement.listener;

import net.wargearworld.bau.advancement.AdvTemplate;
import net.wargearworld.bau.advancement.event.PlayerSwitchTemplateEvent;
import org.bukkit.event.EventHandler;

public class AdvSwitchTemplate extends AdvTemplate {
    public AdvSwitchTemplate() {
        super("bautemplate");
    }

    @EventHandler
    public void onSwitchTemplate(PlayerSwitchTemplateEvent event){
        grantSingleCriterion(event.getPlayer());
    }
}
