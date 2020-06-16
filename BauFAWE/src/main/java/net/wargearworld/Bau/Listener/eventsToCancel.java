package net.wargearworld.Bau.Listener;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.wargearworld.Bau.WorldEdit.WorldGuardHandler;

public class eventsToCancel implements Listener {

	/* permanent canceled Events */
	
	@EventHandler
	public void BlockSpread(BlockSpreadEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void LeavedDecay(LeavesDecayEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void SpongeAbsorb(SpongeAbsorbEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void BlockGrow(BlockGrowEvent e) {
		e.setCancelled(true);
	}

	public void PlayerDamage(EntityDamageEvent event) {
		if(!event.getCause().equals(DamageCause.ENTITY_EXPLOSION)){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Pistonextend(BlockPistonExtendEvent event) {
		for(Block b:event.getBlocks()) {
			System.out.println(b.getLocation());
			if(!WorldGuardHandler.isInBuildRegion(b.getLocation())) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
