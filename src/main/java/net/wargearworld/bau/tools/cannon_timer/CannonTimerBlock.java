package net.wargearworld.bau.tools.cannon_timer;


import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.utils.Loc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

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
        ticks = new LinkedHashMap<>();
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
        BauConfig bauConfig = BauConfig.getInstance();
        if (active) {
            loc.getBlock(world).setType(bauConfig.getCannonTimerActiveBlock());
        } else {
            loc.getBlock(world).setType(bauConfig.getCannonTimerInactiveBlock());
        }
    }

    public boolean isActive() {
        return active;
    }

    public void addTick() {
        BauConfig bauConfig = BauConfig.getInstance();
        for (int i = 1; i <= bauConfig.getCannonTimerMaxTicks(); i++) {
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
        endSpawn(world);
    }

    public Integer increaseTick(int tick, ClickType clickType) {
        CannonTimerTick cannonTimerTick = ticks.get(tick);
        Integer newTick = getNewTick(tick, clickType);
        if (newTick == null)
            return null;
        HashMap<Integer, CannonTimerTick> copy = new LinkedHashMap<>(ticks);
        ticks.clear();
        for (Map.Entry<Integer, CannonTimerTick> entry : copy.entrySet()) {
            if (entry.getKey() == tick) {
                ticks.put(newTick, entry.getValue());
            } else {
                ticks.put(entry.getKey(), entry.getValue());
            }
        }
        return newTick;
    }

    private Integer getNewTick(int tick, ClickType clickType) {
        BauConfig bauConfig = BauConfig.getInstance();
        int jumps = 1;
        if (clickType == ClickType.RIGHT)
            jumps = 5;
        for (int i = tick + jumps; i <= bauConfig.getCannonTimerMaxTicks(); i++) {
            if (!ticks.containsKey(i)) {
                return i;
            }
        }
        return null;
    }

    public Integer decreaseTick(int tick, ClickType clickType) {
        Integer newTick = getLowerTick(tick, clickType);
        if (newTick == null)
            return null;
        Map<Integer, CannonTimerTick> copy = new LinkedHashMap<>(ticks);
        ticks.clear();
        for (Map.Entry<Integer, CannonTimerTick> entry : copy.entrySet()) {
            if (entry.getKey() == tick) {
                ticks.put(newTick, entry.getValue());
            } else {
                ticks.put(entry.getKey(), entry.getValue());
            }
        }
        return newTick;
    }

    private Integer getLowerTick(int tick, ClickType clickType) {
        int jumps = 1;
        if (clickType == ClickType.RIGHT && tick > 5)
            jumps = 5;
        for (int i = tick - jumps; i >= 1; i--) {
            if (!ticks.containsKey(i)) {
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
        ticks.clear();
        this.ticks.putAll(newTicks);
    }

    /* Serialization */
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(loc);
        out.writeBoolean(active);
        out.writeObject(settings);
        out.writeObject(ticks);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        loc = (Loc) in.readObject();
        active = in.readBoolean();
        settings = (CannonTimerSettings) in.readObject();
        ticks = (Map<Integer, CannonTimerTick>) in.readObject();
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
