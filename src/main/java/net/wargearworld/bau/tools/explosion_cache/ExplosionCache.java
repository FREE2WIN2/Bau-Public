package net.wargearworld.bau.tools.explosion_cache;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.Stoplag;
import net.wargearworld.bau.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.*;

public class ExplosionCache {

    private Stack<List<ExplodedBlock>> explodedBlocks;
    private Map<UUID, Boolean> tnts;

    public ExplosionCache() {
        tnts = new HashMap<>();
        explodedBlocks = new Stack<>();
        explodedBlocks.push(new ArrayList<>());
    }

    public void handleExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        int z = WorldManager.get(loc.getWorld()).getPlot(loc).getTeleportPoint().getBlockZ();
        boolean primeZSmallerThanMiddleZ = tnts.get(event.getEntity().getUniqueId());
        boolean explosionZSmallerThanMiddleZ = event.getEntity().getLocation().getBlockZ() < z;

        boolean sameTeam = !explosionZSmallerThanMiddleZ ^ primeZSmallerThanMiddleZ;
        if (sameTeam) {
            List<ExplodedBlock> blocks = explodedBlocks.pop();
            for (Block block : event.blockList()) {
                blocks.add(new ExplodedBlock(block));
            }
            explodedBlocks.push(blocks);
            tnts.remove(event.getEntity().getUniqueId());
        }
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (getTnts(event.getLocation().getWorld()) == 0 && !explodedBlocks.peek().isEmpty()) {
                explodedBlocks.push(new ArrayList<>());
                for (Player player : loc.getWorld().getPlayers()) {
                    MessageHandler msgHandler = MessageHandler.getInstance();
                    TextComponent tc = new TextComponent(Main.prefix + msgHandler.getString(player, "explosion_cached"));
                    TextComponent click = new TextComponent(msgHandler.getString(player, "deletePlotHere"));
                    click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/explosion undo"));
                    click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msgHandler.getString(player, "explosion_hover")).create()));
                    player.spigot().sendMessage(tc, click);
                }
                removeUndo(explodedBlocks.size());
            }

        }, 2);
    }

    private void removeUndo(int size) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (explodedBlocks.size() >= size) {
                explodedBlocks.remove(size - 2);
            }
        }, 20 * 60 * 5);
    }

    public void undo(Player player) {
        if (explodedBlocks.size() == 1) {
            MessageHandler.getInstance().send(player, "explosion_noUndo");
            return;
        }
        boolean sl = Stoplag.getStatus(player.getLocation());
        Stoplag.getInstance().setStatusTemp(player.getLocation(), true,2);
        try {

            explodedBlocks.pop();
            List<ExplodedBlock> blocks = explodedBlocks.pop();
            for (ExplodedBlock block : blocks) {
                block.place();
            }
        } catch (Exception ex) {
        }
        explodedBlocks.push(new ArrayList<>());
        MessageHandler.getInstance().send(player, "explosion_undoed");

    }

    public void onEntityPrime(EntitySpawnEvent event) {
        Location loc = event.getLocation();
        int z = WorldManager.get(loc.getWorld()).getPlot(loc).getTeleportPoint().getBlockZ();
        tnts.put(event.getEntity().getUniqueId(), loc.getZ() < z);
    }

    private int getTnts(World w) {
        int count = 0;
        for (Entity entity : w.getEntities()) {
            if (entity.getType() == EntityType.PRIMED_TNT) {
                count++;
            }
        }
        return count;
    }
}
