package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.bau.MessageHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CannonTimerSettings implements Serializable {
    private double xOffset = 0;
    private double zOffset = 0;
    private boolean velocity = false;

    public CannonTimerSettings() {
    }

    public CannonTimerSettings(double xOffset, double zOffset, boolean velocity) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.velocity = velocity;
    }

    public List<String> generateLore(Player p) {
        MessageHandler msgHandler = MessageHandler.getInstance();
        List<String> out = new ArrayList<>();
        out.add(msgHandler.getString(p, "cannonTimer_gui_settings_lore_xOffset", xOffset + ""));
        out.add(msgHandler.getString(p, "cannonTimer_gui_settings_lore_zOffset", zOffset + ""));
        out.add(msgHandler.getString(p, "cannonTimer_gui_settings_lore_random_" + velocity));
        return out;
    }

    public Location convertLocation(Location origin){
        return origin.clone().add(xOffset,0,zOffset);
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

    public boolean isVelocity() {
        return velocity;
    }

    public void setVelocity(boolean velocity) {
        this.velocity = velocity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CannonTimerSettings that = (CannonTimerSettings) o;
        return Double.compare(that.xOffset, xOffset) == 0 &&
                Double.compare(that.zOffset, zOffset) == 0 &&
                velocity == that.velocity;
    }
}
