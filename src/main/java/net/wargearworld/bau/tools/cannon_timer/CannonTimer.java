package net.wargearworld.bau.tools.cannon_timer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.Loc;
import net.wargearworld.bau.utils.Scheduler;
import net.wargearworld.bau.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CannonTimer implements Serializable, Cloneable {

    private static final long serialVersionUID = -5360744045770382988L;
    private Map<Loc, CannonTimerBlock> blocks;
    private transient boolean blocked;
    private transient Location startMove;
    private transient Loc lastOffset;

    public CannonTimer() {
        blocked = false;
        if (blocks == null)
            blocks = new HashMap<>();
    }

    public void start(Player p) {
        if (blocked) {
            MessageHandler.getInstance().send(p, "cannonTimer_blocked");
            return;
        }
        if (blocks.isEmpty()) {
            MessageHandler.getInstance().send(p, "cannonTimer_empty");
            return;
        }
        blocked = true;
        if (BauPlayer.getBauPlayer(p).getActivateTrailOnCannonTimer()) {
            p.performCommand("trail new");
        }

        for (CannonTimerBlock block : blocks.values()) {
            block.startspawn(p.getWorld());
        }


        MessageHandler.getInstance().send(p, "cannonTimer_start");
        Scheduler scheduler = new Scheduler();
        scheduler.setX(0); // X = variable -> ticktime
        scheduler.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            int currentTick = scheduler.getX();
            for (CannonTimerBlock cannonTimerBlock : blocks.values()) {
                cannonTimerBlock.spawnTnTs(currentTick, p.getWorld());
            }
            if (currentTick == 80) {
                scheduler.cancel();
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    blocked = false;
                    for (CannonTimerBlock block : blocks.values()) {
                        block.endSpawn(p.getWorld());
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
        blocks.put(Loc.getByLocation(loc), cannonTimerBlock);
    }

    public void setBlock(Location loc, CannonTimerBlock cannonTimerBlock) {
        blocks.put(Loc.getByLocation(loc), cannonTimerBlock);
    }

    public void setBlock(Loc loc, CannonTimerBlock cannonTimerBlock) {
        blocks.put(loc, cannonTimerBlock);
    }

    public void addBlock(Loc loc, CannonTimerBlock cannonTimerBlock) {
        blocks.put(loc, cannonTimerBlock);
    }

    public void removeBlock(Location loc) {
        blocks.remove(Loc.getByLocation(loc));
    }

    public CannonTimerBlock getBlock(Location loc) {
        return blocks.get(Loc.getByLocation(loc));
    }

    public CannonTimerBlock getBlock(Loc loc) {
        return blocks.get(loc);
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(blocks);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        blocks = (Map<Loc, CannonTimerBlock>) in.readObject();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
        blocks = new HashMap<>();
    }

    public void startMove(Player p) {
        if (startMove != null) {
            MessageHandler.getInstance().send(p, "cannonTimer_already_moving", WorldManager.get(p.getWorld()).getPlot(p.getLocation()).getId().replace("plot", ""));
            return;
        }
        startMove = p.getLocation().getBlock().getLocation();
        MessageHandler msghandler = MessageHandler.getInstance();
        TextComponent tc1 = new TextComponent(Main.prefix + msghandler.getString(p, "cannonTimer_start_moving"));
        tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ct move end"));
        tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msghandler.getString(p, "cannonTimer_start_moving_hover")).create()));
        TextComponent tc2 = new TextComponent(msghandler.getString(p, "cannonTimer_start_moving_2"));
        p.spigot().sendMessage(tc1, tc2);
    }

    public void place(Player p) {
        if (startMove == null) {
            MessageHandler.getInstance().send(p, "cannonTimer_not_moving", WorldManager.get(p.getWorld()).getPlot(p.getLocation()).getId().replace("plot", ""));
            return;
        }
        Loc offset = Loc.getByLocation(p.getLocation().getBlock().getLocation().subtract(startMove));
        move(p.getWorld(), offset);
        startMove = null;
        lastOffset = offset;
        MessageHandler.getInstance().send(p, "cannonTimer_moved");
    }

    private void move(World world, Loc offset) {
        HashMap<Loc, CannonTimerBlock> map = new HashMap(blocks);
        blocks.clear();
        for (Map.Entry<Loc, CannonTimerBlock> entry : map.entrySet()) {
            Loc loc = entry.getKey();
            CannonTimerBlock cannonTimerBlock = entry.getValue();
            cannonTimerBlock.setPreviousLoc(loc);
            loc.getBlock(world).setType(Material.AIR);
            loc = loc.move(offset);
            cannonTimerBlock.setLoc(loc);
            cannonTimerBlock.setActive(cannonTimerBlock.isActive(), world);
            blocks.put(loc, cannonTimerBlock);
        }
    }

    public void undoMoving(Player p) {
        if (lastOffset == null) {
            MessageHandler.getInstance().send(p, "cannonTimer_move_noUndo");
            return;
        }
        move(p.getWorld(), new Loc(lastOffset.getX(), lastOffset.getY(), lastOffset.getZ()));
        MessageHandler.getInstance().send(p, "cannonTimer_move_undo");
    }

    public void moveBlock(CannonTimerBlock cannonTimerBlock, Loc offset) {
        Loc loc = cannonTimerBlock.getLoc();
        cannonTimerBlock.setPreviousLoc(loc);
        blocks.remove(loc);
        Loc newLoc = loc.move(offset);
        cannonTimerBlock.setLoc(newLoc);
        blocks.put(newLoc, cannonTimerBlock);
    }

    public void clear() {
        blocks.clear();
    }

    public Map<Loc, CannonTimerBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(Map<Loc, CannonTimerBlock> blocks) {
        this.blocks = blocks;
    }

    @Override
    public CannonTimer clone() {
        try {
            return (CannonTimer) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    public void undo(Loc from) {
        CannonTimerBlock cannonTimerBlock = blocks.get(from);
        if (cannonTimerBlock != null) {
            Loc loc = cannonTimerBlock.getLoc();
            Loc newLoc = cannonTimerBlock.getPreviousLoc();
            if(newLoc == null)
                return;
            cannonTimerBlock.setPreviousLoc(loc);
            blocks.remove(loc);
            blocks.put(newLoc, cannonTimerBlock);
            cannonTimerBlock.setLoc(newLoc);
        }
    }
}
