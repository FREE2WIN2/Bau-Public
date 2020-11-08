package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.utils.Loc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Random;

public class CannonTimerTick implements Serializable {

    private static final long serialVersionUID = -2350542064188343999L;
    private CannonTimerSettings settings;
    private int amount;

    public CannonTimerTick() {
        settings = null;
        amount = 1;
    }

    public void spawn(Loc loc, World world, CannonTimerBlock cannonTimerBlock) {
        Random random = new Random();
        Location location = loc.toLocation(world).add(0.5, 0, 0.5);
        CannonTimerSettings effectiveSettings = settings;
        if (settings == null) {
            effectiveSettings = cannonTimerBlock.getSettings();
        }
        location = effectiveSettings.convertLocation(location);
        for (int i = 0; i < amount; i++) {
            Location location1 = location.clone();
            TNTPrimed primedTNT = (TNTPrimed) world.spawnEntity(location1, EntityType.PRIMED_TNT);
            primedTNT.setFuseTicks(80);
            if (!effectiveSettings.isVelocity()) {
                primedTNT.setVelocity(new Vector(0, 0, 0));
            }
        }
    }

    public CannonTimerSettings getSettings() {
        return settings;
    }

    public void setSettings(CannonTimerSettings settings) {
        this.settings = settings;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int add(ClickType clickType) {
        ++amount;
        if (clickType == ClickType.RIGHT) {
            if (amount >= 60) {
                amount = 64;
            } else {
                amount += 4;
            }
        }
        return amount;
    }

    public int remove(ClickType clickType) {
        --amount;
        if (clickType == ClickType.RIGHT) {
            if (amount < 5) {
                amount = 1;
            } else {
                amount -= 4;
            }
        }
        return amount;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(settings);
        out.writeInt(amount);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj != null)
            settings = (CannonTimerSettings) obj;
        amount = in.readInt();
        System.out.println(toString());
    }

    private void readObjectNoData()
            throws ObjectStreamException {
        amount = 1;
        settings = new CannonTimerSettings();

    }

    @Override
    public String toString() {
        return "CannonTimerTick{" +
                "settings=" + settings +
                ", amount=" + amount +
                '}';
    }


}
