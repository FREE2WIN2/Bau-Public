package net.wargearworld.Bau.Listener;


import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMetaBlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;

import net.wargearworld.Bau.Main;

public class SignListener implements Listener {
    public SignListener(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onblockPlace(BlockPlaceEvent e) throws ParseException {
        if (e.getItemInHand().hasItemMeta()) {
            if (e.getBlockPlaced().getType().getKey().getKey().contains("sign")) {
                e.setCancelled(true);
                ItemStack is = e.getItemInHand();
                CraftMetaBlockState state = (CraftMetaBlockState) is.getItemMeta();
                Block b = e.getBlockPlaced();
                Sign sign = (Sign) b.getState();
                Sign metasign = ((Sign) state.getBlockState());
                BlockData signBD = sign.getBlockData();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

                    @Override
                    public void run() {
                        b.setBlockData(signBD);
                        for(int i = 0;i<4;i++){
                            sign.setLine(i,metasign.getLine(i));
                        }
                        sign.update();
                    }
                }, 1);

            }
        }
    }

}