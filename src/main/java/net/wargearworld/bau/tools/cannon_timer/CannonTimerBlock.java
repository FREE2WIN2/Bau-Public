package net.wargearworld.bau.tools.cannon_timer;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.wargearworld.bau.utils.Loc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;

import java.io.*;
import java.util.*;

public class CannonTimerBlock implements Serializable, Cloneable {
    private static final long serialVersionUID = -4112709504886629938L;
    private Loc loc;
    private boolean active;
    private Map<Integer, CannonTimerTick> ticks;
    private CannonTimerSettings settings;

    public CannonTimerBlock(Location loc) {
        this(Loc.getByLocation(loc));
    }

    public CannonTimerBlock() {
    }

    public CannonTimerBlock(Loc loc) {
        ticks = new HashMap<>();
        this.loc = loc;
        this.settings = new CannonTimerSettings();
        this.active = true;
    }

    public void startspawn(World world) {
        loc.getBlock(world).setType(Material.AIR);
    }

    public void spawnTnTs(int currentTick, World world) {

        CannonTimerTick cannonTimerTick = ticks.get(currentTick);
        if (cannonTimerTick == null || !active)
            return;
        cannonTimerTick.spawn(loc, world, this);
    }

    public void endSpawn(World world) {
        if (active) {
            loc.getBlock(world).setType(CannonTimerListener.activeMaterial);
        } else {
            loc.getBlock(world).setType(CannonTimerListener.inactiveMaterial);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void addTick() {
        for (int i = 1; i <= CannonTimerListener.MAX_TICKS; i++) {
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

    public void setActive(boolean active, World world) {
        this.active = active;
        if (this.active) {
            loc.getBlock(world).setType(CannonTimerListener.activeMaterial);
        } else {
            loc.getBlock(world).setType(CannonTimerListener.inactiveMaterial);
        }
    }

    public Integer increaseTick(int tick, ClickType clickType) {
        CannonTimerTick cannonTimerTick = ticks.get(tick);
        int jumps = 1;
        if (clickType == ClickType.RIGHT)
            jumps = 5;
        for (int i = tick + jumps; i <= CannonTimerListener.MAX_TICKS; i++) {
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
        if (clickType == ClickType.RIGHT && tick > 5)
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

    public Loc getLoc() {
        return loc;
    }

    public void setLoc(Loc loc) {
        this.loc = loc;
    }

    public void remove(Integer key) {
        ticks.remove(key);
    }

    public void sort() {
        System.out.println("sort");
        TreeMap<Integer, CannonTimerTick> newTicks = new TreeMap<>(ticks);
        this.ticks = newTicks;
    }

    /* Serialization */
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(loc);
        out.writeBoolean(active);
        out.writeObject(settings);
        out.writeObject(ticks);
        System.out.println(ticks.size());
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        loc = (Loc) in.readObject();
        active = in.readBoolean();
        settings = (CannonTimerSettings) in.readObject();
        ticks = (Map<Integer, CannonTimerTick>) in.readObject();

        System.out.println(toString());
    }

    private void readObjectNoData()
            throws ObjectStreamException {
        throw new InvalidObjectException("No data found");

    }

    @Override
    public String toString() {
        return "CannonTimerBlock{" +
                "loc=" + loc +
                ", active=" + active +
                ", ticks=" + ticks +
                ", settings=" + settings +
                '}';
    }

    @Override
    public CannonTimerBlock clone() throws CloneNotSupportedException {
        return (CannonTimerBlock) super.clone();
    }
}
