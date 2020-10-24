package net.wargearworld.bau.utils;

import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketMapChunk {
        private Chunk chunk;

        public PacketMapChunk(final org.bukkit.Chunk chunk) {
            this.chunk = ((CraftChunk)chunk).getHandle();
        }

        public final void send(final Player player) {
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(chunk, 20));
        }

}
