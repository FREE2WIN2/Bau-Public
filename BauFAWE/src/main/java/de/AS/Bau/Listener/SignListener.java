package de.AS.Bau.Listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.AS.Bau.Main;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class SignListener implements Listener {
	public SignListener(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onblockPlace(BlockPlaceEvent e) {
		if (e.getItemInHand().hasItemMeta()) {
			if (e.getBlockPlaced().getType().getKey().getKey().contains("sign")) {
				e.setCancelled(true);
				ItemStack is = e.getItemInHand();
				Block b = e.getBlockPlaced();
				Sign sign = (Sign) b.getState();
				net.minecraft.server.v1_15_R1.ItemStack isNMS = CraftItemStack.asNMSCopy(is);
				NBTTagCompound comp;
				comp = isNMS.getTag();
				String templine1 = comp.getCompound("BlockEntityTag").getString("Text1")
						.replace("{\"extra\":[{\"text\":\"", "").replace("\"}],\"text\":\"\"}", "");
				String templine2 = comp.getCompound("BlockEntityTag").getString("Text2")
						.replace("{\"extra\":[{\"text\":\"", "").replace("\"}],\"text\":\"\"}", "");
				String templine3 = comp.getCompound("BlockEntityTag").getString("Text3")
						.replace("{\"extra\":[{\"text\":\"", "").replace("\"}],\"text\":\"\"}", "");
				String templine4 = comp.getCompound("BlockEntityTag").getString("Text4")
						.replace("{\"extra\":[{\"text\":\"", "").replace("\"}],\"text\":\"\"}", "");

				String line1;
				String line2;
				String line3;
				String line4;
				if (templine1.contains("{")) {
					line1 = "";
				} else {
					line1 = templine1;
				}

				if (templine2.contains("{")) {
					line2 = "";
				} else {
					line2 = templine2;
				}

				if (templine3.contains("{")) {
					line3 = "";
				} else {
					line3 = templine3;
				}

				if (templine4.contains("{")) {
					line4 = "";
				} else {
					line4 = templine4;
				}
				BlockData signBD = sign.getBlockData();
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						b.setBlockData(signBD);
						sign.setLine(0, line1);
						sign.setLine(1, line2);
						sign.setLine(2, line3);
						sign.setLine(3, line4);
						sign.update();

					}
				}, 1);

			}
		}
	}

}