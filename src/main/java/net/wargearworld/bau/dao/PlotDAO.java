package net.wargearworld.bau.dao;

import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.bau.world.gui.WorldIcon;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.*;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

public class PlotDAO {
    public static void update(PlayerWorld playerWorld) {
        EntityManagerExecuter.run(em -> {
            Plot plot = em.find(Plot.class, playerWorld.getId());
            plot.setTemplate(em.find(PlotTemplate.class, playerWorld.getTemplate().getId()));
            plot.setName(playerWorld.getName());
            em.merge(plot);
        });
    }

    public static void update(TeamWorld teamWorld) {
        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, teamWorld.getTeam().getId());
//            wargearTeam.getTemplate().getWargearTeams().remove(wargearTeam);

            PlotTemplate plotTemplate = em.find(PlotTemplate.class, teamWorld.getTemplate().getId());
            plotTemplate.getWargearTeams().add(wargearTeam);
            System.out.println("plotTemlateID " + plotTemplate.getId());
            wargearTeam.setTemplate(plotTemplate);
            em.merge(wargearTeam);
        });

        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, teamWorld.getTeam().getId());
//            wargearTeam.getTemplate().getWargearTeams().remove(wargearTeam);

            PlotTemplate plotTemplate = em.find(PlotTemplate.class, teamWorld.getTemplate().getId());
            plotTemplate.getWargearTeams().add(wargearTeam);
            wargearTeam.setTemplate(plotTemplate);
            em.merge(wargearTeam);
        });
    }

    public static WorldIcon getWorldIcon(long plotID) {
        return EntityManagerExecuter.run(em -> {
            Plot dbPLot = em.find(Plot.class, plotID);
            Icon icon = dbPLot.getIcon();
            return new WorldIcon(icon);
        });
    }

    public static WorldTemplate getTemplate(UUID owner, String name) {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Plot> cq = cb.createQuery(Plot.class);
            Root<Plot> root = cq.from(Plot.class);
            cq.where(cb.equal(root.get(Plot_.owner), em.find(Player.class, owner)), cb.equal(root.get(Plot_.name), name));
            Plot plot = em.createQuery(cq).getSingleResult();
            if (plot != null)
                return WorldTemplate.getTemplate(plot.getTemplate().getName());
            return null;
        });
    }

    public static void changeToDefault(UUID owner, String name) {
        EntityManagerExecuter.run(em->{
           Player dbPlayer = em.find(Player.class,owner);
           for(Plot plot:dbPlayer.getPlots()){
               if(plot.getDefault()){
                   plot.setDefault(false);
                   em.merge(plot);
                   break;
               }
           }
           for(Plot plot: dbPlayer.getPlots()){
               if(plot.getName().equals(name)){
                   plot.setDefault(true);
                   em.merge(plot);
                   break;
               }
           }
        });
    }

    public static void setIcon(UUID owner, String name, Long id) {
        EntityManagerExecuter.run(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Plot> cq = cb.createQuery(Plot.class);
            Root<Plot> root = cq.from(Plot.class);
            cq.where(cb.equal(root.get(Plot_.owner), em.find(Player.class, owner)), cb.equal(root.get(Plot_.name), name));
            try{
            Plot plot = em.createQuery(cq).getSingleResult();
            Icon icon = em.find(Icon.class,id);
            plot.setIcon(icon);
            em.merge(plot);
            }catch(NoResultException ex){}
        });
    }
}
