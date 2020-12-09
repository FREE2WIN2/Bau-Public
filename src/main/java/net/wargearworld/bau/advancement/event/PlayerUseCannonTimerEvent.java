package net.wargearworld.bau.advancement.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerUseCannonTimerEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerUseCannonTimerEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
