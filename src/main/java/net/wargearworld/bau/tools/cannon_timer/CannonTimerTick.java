package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.bau.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.Vector;

import java.util.Random;

public class CannonTimerTick {

    private double xOffset = 0;
    private double zOffset = 0;
    private boolean isRandom = false;
    private int amount = 1;

    public void spawn(Location location) {
        Random random = new Random();
        World world = location.getWorld();
        Location location1 = location.clone().add(0.5, 0, 0.5);
        location1.add(xOffset, 0, zOffset);
        for (int i = 0; i < amount; i++) {
            Location loc = location1.clone();
            if (isRandom) {
                location1.add(random.nextDouble() * xOffset, 0, random.nextDouble() * zOffset);
            }
            TNTPrimed primedTNT = (TNTPrimed) world.spawnEntity(location1, EntityType.PRIMED_TNT);
            primedTNT.setFuseTicks(80);
            if (!isRandom){
                primedTNT.setVelocity(new Vector(0,0,0));
//                teleport(primedTNT, location1);

            }
        }
    }

    private void teleport(TNTPrimed primedTNT, Location loc) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            primedTNT.teleport(loc);
        }, 1);
    }

    public double getxOffset() {
        return xOffset;
    }

    public void setxOffset(double xOffset) {
        this.xOffset = xOffset;
    }

    public double getzOffset() {
        return zOffset;
    }

    public void setzOffset(double zOffset) {
        this.zOffset = zOffset;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public void setRandom(boolean random) {
        this.isRandom = random;
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
