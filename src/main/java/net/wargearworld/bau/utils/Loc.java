package net.wargearworld.bau.utils;

import com.sk89q.worldedit.math.BlockVector3;
import net.wargearworld.bau.tools.testBlockSlave.ChooseTestBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Objects;

public class Loc implements Serializable {

    private static final long serialVersionUID = 2490897685114624317L;
    private double x;
    private double y;
    private double z;

    public Loc(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Loc(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * DOn use! ists just for Serializsation
     */
    public Loc() {
    }

    public static Loc getByLocation(Location loc) {
        return new Loc(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Loc getByBlockVector(BlockVector3 blockVector3) {
        return new Loc(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ());
    }


    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public static Loc getByString(String string) {
        String[] locs = string.split(" ");
        double x = Double.parseDouble(locs[0]);
        double y = Double.parseDouble(locs[1]);
        double z = Double.parseDouble(locs[2]);
        return new Loc(x, y, z);
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
        throw new InvalidClassException("No data found");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loc loc = (Loc) o;
        return Double.compare(loc.x, x) == 0 &&
                Double.compare(loc.y, y) == 0 &&
                Double.compare(loc.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Loc{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public Block getBlock(World world) {
        return new Location(world, x, y, z).getBlock();
    }

    /**
     * Moves this Loc by the values of by
     *
     * @param by the values to move
     * @return this
     */
    public Loc move(Loc by) {
        x += by.x;
        y += by.y;
        z += by.z;
        return this;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
