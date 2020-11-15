package net.wargearworld.bau.dao;

import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

public class PlayerDAO {

    public static Map<WorldTemplate, Boolean> getPlayersTeamplates(UUID playerUUID) {
        return EntityManagerExecuter.run(em -> {
            TreeMap<WorldTemplate, Boolean> out = new TreeMap<>();
            Player dbPlayer = em.find(Player.class, playerUUID);
            for (PlayerPlotTemplate playerPlotTemplate : dbPlayer.getPlayerPlotTemplates()) {
                WorldTemplate worldTemplate = WorldTemplate.getTemplate(playerPlotTemplate.getPlotTemplate().getName());
                out.put(worldTemplate, true);
            }

            /*Get All PlotTemplates*/

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PlotTemplate> cq = cb.createQuery(PlotTemplate.class);
            cq.from(PlotTemplate.class);
            em.createQuery(cq).getResultList().forEach(s -> {
                WorldTemplate worldTemplate = WorldTemplate.getTemplate(s.getName());
                if (!out.containsKey(worldTemplate)) {
                    out.put(worldTemplate, false);
                }
            });
            return out;
        });
    }

    public static void addPlotTemplate(WorldTemplate worldTemplate, UUID playerUUID) {
        EntityManagerExecuter.run(em -> {
            Player dbPlayer = em.find(Player.class, playerUUID);
            PlotTemplate plotTemplate = em.find(PlotTemplate.class, worldTemplate.getId());
            PlayerPlotTemplate playerPlotTemplate = new PlayerPlotTemplate();
            dbPlayer.addPlayerPlotTemplate(playerPlotTemplate);
            plotTemplate.addPlayerPlotTemplate(playerPlotTemplate);

            em.persist(playerPlotTemplate);
        });
    }

    public static PlotTemplate getDefaultPlotTemplate(UUID uuid) {
        return EntityManagerExecuter.run(em -> {
            Player dbPlayer = em.find(Player.class, uuid);
            for (Plot plot : dbPlayer.getPlots()) {
                if (plot.getDefault()) {
                    return plot.getTemplate();
                }
            }
            return null;
        });
    }

    public static void addNewWorld(String worldName, UUID uuid, boolean isDefault) {
        EntityManagerExecuter.run(em -> {
            Player dbPlayer = em.find(Player.class, uuid);
            PlotTemplate plotTemplate = em.find(PlotTemplate.class,BauConfig.getInstance().getDefaultTemplate().getId());
            Plot plot = new Plot(worldName,dbPlayer, plotTemplate,isDefault);
            em.persist(plot);
        });
    }

    public static Collection<String> getPlayersAddedPlotsPlayerNames(UUID playerUUID) {
        return EntityManagerExecuter.run(em->{
           Set<String> out = new TreeSet<>();
           Player dbPlayer = em.find(Player.class,playerUUID);
           for(PlotMember plotMember: dbPlayer.getMemberedPlots()){
               out.add(plotMember.getPlot().getOwner().getName());
           }
           return out;
        });
    }
    public static Collection<String> getPlayersPlotNames(String playerName) {
        return EntityManagerExecuter.run(em->{
            Set<String> out = new TreeSet<>();
            Player dbPlayer = em.find(Player.class,DatabaseDAO.getUUID(playerName));
            for(Plot plot: dbPlayer.getPlots()){
                out.add(plot.getName());
            }
            return out;
        });
    }

    public static String getDefaultWorldName(UUID uuid) {
        return EntityManagerExecuter.run(em->{
            Player dbPlayer = em.find(Player.class,uuid);
            for(Plot plot: dbPlayer.getPlots()){
                if(plot.getDefault()){
                    return plot.getName();
                }
            }
            return dbPlayer.getPlots().iterator().next().getName();
        });
    }

    public static Collection<String> getAllPlayersWithPlot() {
        return EntityManagerExecuter.run(em->{
            Set<String> out = new TreeSet<>();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Plot> root = cq.from(Plot.class);
            cq.distinct(true);
            cq.select(root.get(Plot_.owner.getName()));
            List<Player> players =  em.createQuery(cq).getResultList();
            players.forEach(player -> {
                out.add(player.getName());
            });
            return out;
        });
    }
}
