package net.wargearworld.bau.event;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldEditMoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private BlockVector3 offset;
    private Region region;
    private Player player;

    public WorldEditMoveEvent(BlockVector3 offset, Region region, Player player) {
        super(true);
        this.offset = offset;
        this.region = region;
        this.player = player;
    }

    public BlockVector3 getOffset() {
        return offset;
    }

    public Region getRegion() {
        return region;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
