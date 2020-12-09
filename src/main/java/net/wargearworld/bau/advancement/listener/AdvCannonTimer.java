package net.wargearworld.bau.advancement.listener;

import net.wargearworld.bau.advancement.AdvTemplate;
import net.wargearworld.bau.advancement.event.PlayerUseCannonTimerEvent;
import org.bukkit.event.EventHandler;

public class AdvCannonTimer extends AdvTemplate {
    public AdvCannonTimer() {
        super("simulator");
    }

    @EventHandler
    public void onCannonTimer(PlayerUseCannonTimerEvent event){
        grantSingleCriterion(event.getPlayer());
    }
}
