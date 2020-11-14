package net.wargearworld.bau.listener;

import net.wargearworld.StringGetter.Language;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.scoreboard.ScoreBoardBau;
import net.wargearworld.bau.tools.testBlockSlave.testBlockEditor.TestBlockEditorCore;
import net.wargearworld.bau.utils.ItemStackCreator;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.bauworld.BauWorld;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotTemplate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerListener implements Listener {

    public PlayerListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        // sprache
        Player p = e.getPlayer();
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        BauPlayer player = BauPlayer.getBauPlayer(p);
        String lang = player.getDbPlayer().getCountryCode();
        MessageHandler.playersLanguage.put(p.getUniqueId(), Language.valueOf(lang.toUpperCase()));
        // has own gs?
        String path = Bukkit.getWorldContainer().getAbsolutePath();
        File gs = new File(path + "/" + p.getUniqueId() + "_" + p.getName());
        String uuidString = p.getUniqueId().toString();
        if (!player.hasPlots()) {
            EntityManagerExecuter.run(em -> {
                PlotTemplate dbTemplate = em.find(PlotTemplate.class, WorldManager.template.getId());
                Plot plot = new Plot();
                plot.setName(p.getName());
                plot.setTemplate(dbTemplate);
                plot.setOwner(em.find(net.wargearworld.db.model.Player.class, p.getUniqueId()));
                plot.setDefault(true);
                em.persist(plot);
            });
        }
        WorldTemplate worldTemplate = EntityManagerExecuter.run(em -> {
            PlotTemplate plotTemplate = PlayerDAO.getDefaultPlotTemplate(p.getUniqueId());
            if (plotTemplate == null) {
                return WorldManager.template;
            } else {
                return WorldTemplate.getTemplate(plotTemplate.getName());
            }
        });


        if (!gs.exists()) {
            // Bukkit.createWorld((WorldCreator) WorldCreator.name("test").createWorld());
            // wenn nicht-> erstellen und hinteleportieren
            WorldManager.createWorldDir(uuidString + "_" + p.getName(), worldTemplate);
            p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "plotGenerating"));
        }


        World world = WorldManager.loadWorld(p.getName(), uuidString);
        BauWorld bauWorld = WorldManager.get(world);
        bauWorld.spawn(p);
        // wenn ja-> teleportieren
        // item
        p.getInventory().setItem(0, ItemStackCreator.createNewItemStack(Material.NETHER_STAR, "§6GUI"));
        new ScoreBoardBau(p);
        BauPlayer.getBauPlayer(p).loadClipboard();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeaveevent(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        PlayerMovement.playersLastPlot.remove(p.getUniqueId());
        ScoreBoardBau.getS(p).cancel();
        TestBlockEditorCore.playersTestBlockEditor.remove(p.getUniqueId());

        UUID uuid = p.getUniqueId();
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(p);
        bauPlayer.saveClipboard();
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (BauPlayer.getBauPlayer(uuid).getBukkitPlayer() == null) {
                BauPlayer.remove(uuid);
            }
        }, 10 * 60 * 60);//1hour


    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawnevent(PlayerRespawnEvent e) {
        WorldManager.get(e.getPlayer().getWorld()).spawn(e.getPlayer());
    }
}
