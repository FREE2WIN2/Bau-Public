package net.wargearworld.bau.tools.cannon_reloader;

import net.wargearworld.bau.utils.Loc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class AutoCannonReloaderBlock {
    private Loc loc;
    private Loc previousLoc;

    public AutoCannonReloaderBlock(Loc loc) {
        this.loc = loc;
    }

    public void spawn(World world){
        Location location = loc.toLocation(world);
        if (location.getBlock().getType().isAir() || location.getBlock().isLiquid()) {
            location.getBlock().setType(Material.TNT);
        }
    }

    public void move(Loc offset){
        previousLoc = loc;
        loc = loc.move(offset);
    }

    public Loc getLoc() {
        return loc;
    }

    public void setLoc(Loc loc) {
        this.loc = loc;
    }

    public void setPreviousLoc(Loc previousLoc) {
        this.previousLoc = previousLoc;
    }

    public Loc getPreviousLoc() {
        return previousLoc;
    }
}
