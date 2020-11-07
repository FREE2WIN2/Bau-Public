package net.wargearworld.bau.tools.cannon_timer;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.io.Serializable;
import java.util.*;

public class CannonTimerBlock implements Serializable{
    private Location loc;
    private boolean active = true;
    private Map<Integer, CannonTimerTick> ticks;
    private CannonTimerSettings settings = new CannonTimerSettings();

    public CannonTimerBlock(Location loc) {
        ticks = new TreeMap<>();
        this.loc = loc;
    }

    public void startspawn() {
        loc.getBlock().setType(Material.AIR);
    }

    public void spawnTnTs(int currentTick) {

        CannonTimerTick cannonTimerTick = ticks.get(currentTick);
        if (cannonTimerTick == null || !active)
            return;
        cannonTimerTick.spawn(loc);
    }

    public void endSpawn() {
        if (active) {
            loc.getBlock().setType(CannonTimerListener.activeMaterial);
        } else {
            loc.getBlock().setType(CannonTimerListener.inactiveMaterial);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void addTick() {
        for (int i = 1; i < 81; i++) {
            if (ticks.get(i) == null) {
                ticks.put(i, new CannonTimerTick());
                return;
            }
        }
    }

    public CannonTimerTick getTick(int tick) {
        return ticks.get(tick);
    }

    public Map<Integer, CannonTimerTick> getTicks() {
        return ticks;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (this.active) {
            loc.getBlock().setType(CannonTimerListener.activeMaterial);
        } else {
            loc.getBlock().setType(CannonTimerListener.inactiveMaterial);
        }
    }

    public Integer increaseTick(int tick, ClickType clickType) {
        CannonTimerTick cannonTimerTick = ticks.get(tick);
        int jumps = 1;
        if(clickType == ClickType.RIGHT)
            jumps = 5;
        for (int i = tick + jumps; i < 81; i++) {
            if (!ticks.containsKey(i)) {
                ticks.remove(tick);
                ticks.put(i, cannonTimerTick);

                return i;
            }
        }
        return null;
    }

    public Integer decreaseTick(int tick, ClickType clickType) {
        CannonTimerTick cannonTimerTick = ticks.get(tick);
        int jumps = 1;
        if(clickType == ClickType.RIGHT&& tick > 5)
            jumps = 5;
        for (int i = tick - jumps; i >= 1; i--) {
            if (!ticks.containsKey(i)) {
                ticks.remove(tick);
                ticks.put(i, cannonTimerTick);
                return i;
            }
        }
        return null;
    }

    public CannonTimerSettings getSettings() {
        return settings;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }
}
