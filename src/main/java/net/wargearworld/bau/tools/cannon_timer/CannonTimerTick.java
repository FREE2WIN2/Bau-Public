package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.bau.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.Random;

public class CannonTimerTick implements Serializable {

    private CannonTimerSettings settings = null;
    private int amount = 1;

    public void spawn(Location location, CannonTimerBlock cannonTimerBlock) {
        Random random = new Random();
        Location location1 = location.clone().add(0.5, 0, 0.5);
        CannonTimerSettings effectiveSettings = settings;
        if(settings == null){
            effectiveSettings = cannonTimerBlock.getSettings();
        }
        location1 = effectiveSettings.convertLocation(location1);
        World world = location.getWorld();
        for (int i = 0; i < amount; i++) {
            Location loc = location1.clone();
            TNTPrimed primedTNT = (TNTPrimed) world.spawnEntity(location1, EntityType.PRIMED_TNT);
            primedTNT.setFuseTicks(80);
            if (!effectiveSettings.isVelocity()){
                primedTNT.setVelocity(new Vector(0,0,0));
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
        if (clickType == ClickType.RIGHT && amount < 60)
            amount += 4;
        return amount;
    }

    public int remove(ClickType clickType) {
        --amount;
        if (clickType == ClickType.RIGHT && amount > 5)
            amount -= 4;
        return amount;
    }
}
