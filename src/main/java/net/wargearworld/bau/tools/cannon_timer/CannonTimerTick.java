package net.wargearworld.bau.tools.cannon_timer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;

import java.util.Random;

public class CannonTimerTick {

    private double xOffset = 0;
    private double zOffset = 0;
    private boolean isRandom = false;
    private double xRandom = 0;
    private double zRandom = 0;
    private int amount = 0;

    public void spawn(Location location) {
        Random random = new Random();
        World world = location.getWorld();
        Location location1 = location.clone();
        location1.add(xOffset, 0, zOffset);
        for (int i = 0; i < amount; i++) {
            Location loc = location1.clone();
            if (isRandom) {
                location1.add(random.nextDouble() * xOffset, 0, random.nextDouble() * zOffset);
            }
            TNTPrimed primedTNT = (TNTPrimed) world.spawnEntity(location1, EntityType.PRIMED_TNT);
            primedTNT.setFuseTicks(80);
        }
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

    public double getxRandom() {
        return xRandom;
    }

    public void setxRandom(double xRandom) {
        this.xRandom = xRandom;
    }

    public double getzRandom() {
        return zRandom;
    }

    public void setzRandom(double zRandom) {
        this.zRandom = zRandom;
    }

    public int add() {
        return ++amount;
    }

    public int remove() {
        return --amount;
    }
}
