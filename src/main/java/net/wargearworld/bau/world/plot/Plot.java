package net.wargearworld.bau.world.plot;

import com.sk89q.worldedit.math.BlockVector3;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.tools.cannon_timer.CannonTimer;
import net.wargearworld.bau.tools.explosion_cache.ExplosionCache;
import net.wargearworld.bau.tools.waterremover.WaterRemover;
import net.wargearworld.bau.tools.waterremover.WaterRemoverListener;
import net.wargearworld.bau.tools.worldfuscator.WorldFuscatorIntegration;
import net.wargearworld.bau.world.bauworld.BauWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.worldedit.Schematic;
import net.wargearworld.bau.worldedit.WorldEditHandler;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Plot {


    private ProtectedRegion region;
    private String id;

    private Location middleNorth;
    private Schematic ground;
    private WaterRemover waterRemover;
    private CannonTimer cannonTimer;

    private Clipboard undo; //Undo from Reset
    private CannonTimer undoCannonTimer;

    private int deactivatedExplosionCache = 0;
    private BukkitTask bukkitTask;

    ExplosionCache explosionCache;

    protected Plot(ProtectedRegion region, String id, Location middleNorth, Schematic ground, BauWorld bauWorld) {
        this.region = region;
        this.id = id;
        this.middleNorth = middleNorth;
        this.ground = ground;
        if (region != null)
            setWaterRemover(region.getFlag(WaterRemoverListener.waterRemoverFlag) == State.DENY);
        cannonTimer = deserializeCannonTimer(bauWorld);
        if (cannonTimer == null)
            cannonTimer = new CannonTimer();

        explosionCache = new ExplosionCache();
    }


    public ProtectedRegion getRegion() {
        return region;
    }

    public String getId() {
        return id;
    }

    /* SL */

    public boolean toggleSL() {
        return toggleFlag(Main.stoplag);
    }

    public void setSL(boolean on) {
        if (on) {
            region.setFlag(Main.stoplag, State.ALLOW);
        } else {
            region.setFlag(Main.stoplag, State.DENY);
        }
    }

    public boolean getSL() {
        return region.getFlag(Main.stoplag) == State.ALLOW;
    }


    /* TNT */

    public boolean toggleTNT() {
        return toggleFlag(Main.TntExplosion);
    }

    public void setTNT(boolean on) {
        if (on) {
            region.setFlag(Main.TntExplosion, State.ALLOW);
        } else {
            region.setFlag(Main.TntExplosion, State.DENY);
        }
    }

    public boolean getTNT() {
        return region.getFlag(Main.TntExplosion) == State.ALLOW;
    }

    /* General Flags */
    private boolean toggleFlag(StateFlag flag) {
        if (region.getFlag(flag) == State.ALLOW) {
            region.setFlag(flag, State.DENY);
            return false;
        } else {
            region.setFlag(flag, State.ALLOW);
            return true;
        }
    }


    public boolean reset(World world) {
        /* save Undo */
        Region rg = new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
        rg.setWorld(BukkitAdapter.adapt(world));
        BlockArrayClipboard board = new BlockArrayClipboard(rg);
        board.setOrigin(BukkitAdapter.adapt(middleNorth).toVector().toBlockPoint());
        ForwardExtentCopy copy = new ForwardExtentCopy(rg.getWorld(), rg, board.getOrigin(), board,
                board.getOrigin());
        try {
            Operations.completeLegacy(copy);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        undo = board;
        delundo();

        WorldEditHandler.pasteground(ground, middleNorth);
        undoCannonTimer = cannonTimer.clone();
        cannonTimer = null;
        return true;

    }

    private void delundo() {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            undo = null;
            undoCannonTimer = null;
        }, 20 * 60 * 1);
    }

    public boolean undo(World world) {
        /* save Undo */
        if (undo != null) {
            WorldEditHandler.pasteAsync(new ClipboardHolder(undo), undo.getOrigin(), world, true);
            undo = null;
        }
        if (undoCannonTimer != null) {
            cannonTimer = undoCannonTimer.clone();
            undoCannonTimer = null;
        }
        return true;

    }

    public Location getTeleportPoint() {
        Location out = middleNorth.clone();//clone
        return out.add(0.5, 0, 0);
    }

    /**
     * @return The Paste Position for Things in Direction North (Lays in South)
     */
    public Location getPasteN() {
        Location out = middleNorth.clone();//clone
        return out.add(0, 0, 23);
    }

    /**
     * @return The Paste Position for Things in Direction South (Lays in North)
     */
    public Location getPasteS() {
        Location out = middleNorth.clone();//clone
        return out.add(0, 0, -24);
    }

    public Schematic getGround() {
        return ground;
    }

    public abstract PlotType getType();

    public void setWaterRemover(boolean active) {
        if (active) {
            region.setFlag(WaterRemoverListener.waterRemoverFlag, State.ALLOW);
            startWaterRemover();
        } else {
            region.setFlag(WaterRemoverListener.waterRemoverFlag, State.DENY);
            stopWaterRemover();
        }
    }

    public void startWaterRemover() {
        waterRemover = new WaterRemover(middleNorth.getBlockZ());
    }

    public void stopWaterRemover() {
        if (waterRemover != null)
            waterRemover.stop();
        waterRemover = null;
    }

    public WaterRemover getWaterRemover() {
        return waterRemover;
    }

    public boolean calcWorldFuscator(BlockVector3 min) {
        return !(region.contains(min) && isWorldFuscated());
    }

    public boolean isWorldFuscated() {
        return region.getFlag(WorldFuscatorIntegration.worldfuscatorFlag) == State.ALLOW;
    }

    public void setWorldFuscated(boolean active) {
        if (active) {
            region.setFlag(WorldFuscatorIntegration.worldfuscatorFlag, State.ALLOW);
        } else {
            region.setFlag(WorldFuscatorIntegration.worldfuscatorFlag, State.DENY);
        }
    }

    public CannonTimer getCannonTimer() {
        return cannonTimer;
    }

    public void unload(BauWorld bauWorld) {
        try {
            File serializationFile = new File(bauWorld.getWorldSettingsDir(), getId() + ".ser");
            if (!serializationFile.exists()) {
                serializationFile.getParentFile().mkdirs();
            }
            serializationFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(serializationFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(cannonTimer);
            objectOutputStream.close();
            objectOutputStream.flush();
            outputStream.close();
        } catch (IOException e) {
        }
    }

    private CannonTimer deserializeCannonTimer(BauWorld bauWorld) {
        File serializationFile = new File(bauWorld.getWorldSettingsDir(), getId() + ".ser");
        if (!serializationFile.exists()) {
            return new CannonTimer();
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(serializationFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            CannonTimer out = (CannonTimer) ois.readObject();
            ois.close();
            fis.close();
            return out;
        } catch (IOException | ClassNotFoundException e) {
        }
        return new CannonTimer();
    }


    public boolean isWaterRemoverActive() {
        return waterRemover != null;
    }

    public int getDeactivatedExplosionCache() {
        return deactivatedExplosionCache;
    }

    public boolean isDeactivatedExplosionCache() {
        return deactivatedExplosionCache != 0;
    }

    public void deactivateExplosionCache(int seconds, BauWorld bauWorld) {
        deactivatedExplosionCache = seconds;
        for (Player player : getPlayers(bauWorld)) {
            ScoreBoardBau.cmdUpdate(player);
            if (seconds == 0) {
                MessageHandler.getInstance().send(player, "tnt_allowExplosion_deactivated");
            }
        }
        if (seconds == 0)
            return;
        bukkitTask = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            deactivatedExplosionCache--;
            Collection<Player> players = getPlayers(bauWorld);
            for (Player player : players) {
                ScoreBoardBau.cmdUpdate(player);
            }
            if (deactivatedExplosionCache == 0) {
                bukkitTask.cancel();
                bukkitTask = null;
                for (Player player : players) {
                    MessageHandler.getInstance().send(player, "tnt_allowExplosion_deactivated");
                }
            }
        }, 20, 20);
    }

    public ExplosionCache getExplosionCache() {
        return explosionCache;
    }


    public Collection<Player> getPlayers(BauWorld bauWorld) {
        List<Player> out = new ArrayList<>();
        for (Player p : bauWorld.getWorld().getPlayers()) {
            if (bauWorld.getPlot(p.getLocation()).equals(this)) {
                out.add(p);
            }
        }
        return out;
    }
}
