package net.wargearworld.bau.tools.cannon_timer;


import org.bukkit.Location;

import java.util.Map;
import java.util.TreeMap;

public class CannonTimerBlock {
    private Location loc;
    private boolean active = true;
    private TreeMap<Integer,CannonTimerTick> ticks;

    public CannonTimerBlock(Location loc) {
        ticks = new TreeMap<>();
        this.loc = loc;
    }

    public void spawnTnTs(int currentTick) {
        CannonTimerTick cannonTimerTick = ticks.get(currentTick);
        if(cannonTimerTick == null || !active)
            return;
        cannonTimerTick.spawn(loc);
    }

    public boolean isActive() {
        return active;
    }

    public void addTick(int tick){
        ticks.put(tick,new CannonTimerTick());
    }

    public CannonTimerTick getTick(int tick){
        return ticks.get(tick);
    }

    public Map<Integer, CannonTimerTick> getTicks() {
        return ticks;
    }
}
