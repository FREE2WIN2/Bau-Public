package net.wargearworld.bau.tools.cannon_timer;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CannonTimer {

    private Map<Location, CannonTimerBlock> blocks;

    public CannonTimer() {
        blocks = new HashMap<>();
    }

    public void start(Player p) {
        MessageHandler.getInstance().send(p, "cannonTimer_start");
        Scheduler scheduler = new Scheduler();
        scheduler.setX(0); // X = variable -> ticktime
        scheduler.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            int currentTick = scheduler.getX();
            for (CannonTimerBlock cannonTimerBlock : blocks.values()) {
                cannonTimerBlock.spawnTnTs(currentTick);
            }
            if (currentTick == 80) {
                scheduler.cancel();
            }
            scheduler.setX(++currentTick);
        }, 0, 1));
    }

    public void reset() {
        blocks.clear();//TODO remove blocks
    }

    public void addBlock(Location loc, CannonTimerBlock cannonTimerBlock) {
        blocks.put(loc, cannonTimerBlock);
    }

    public void removeBlock(Location loc) {
        blocks.remove(loc);
    }

    public CannonTimerBlock getBlock(Location loc) {
        return blocks.get(loc);
    }
}
