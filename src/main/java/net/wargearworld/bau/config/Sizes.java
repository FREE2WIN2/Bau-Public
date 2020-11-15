package net.wargearworld.bau.config;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.module.Configuration;

public class Sizes {
    private int tier;
    private int x;
    private int y;
    private int z;
    private int shields;

    public Sizes(int tier, ConfigurationSection configurationSection) {//section sizes.tier
        this.tier = tier;
        this.x = configurationSection.getInt("x");
        this.y = configurationSection.getInt("y");
        this.z = configurationSection.getInt("z");
        this.shields = configurationSection.getInt("shields");
    }

    public int getTier() {
        return tier;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getShields() {
        return shields;
    }

    public BlockVector3 toBlockVector(){
        return BlockVector3.at(x,y,z);
    }
}
