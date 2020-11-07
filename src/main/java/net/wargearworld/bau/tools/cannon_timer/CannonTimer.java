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
    private boolean blocked = false;

    public CannonTimer() {
        blocks = new HashMap<>();
    }

    public void start(Player p) {
        if (blocked) {
            MessageHandler.getInstance().send(p, "cannonTimer_blocked");
            return;
        }
        blocked = true;
        for (CannonTimerBlock block : blocks.values()) {
            block.startspawn();
        }


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
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    blocked = false;
                    for (CannonTimerBlock block : blocks.values()) {
                        block.endSpawn();
                    }
                }, 20 * 4);
            }
            scheduler.setX(++currentTick);
        }, 0, 1));
    }

    public void reset() {
        blocks.clear();
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
