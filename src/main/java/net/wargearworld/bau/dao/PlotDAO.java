package net.wargearworld.bau.dao;

import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Event;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotTemplate;
import net.wargearworld.db.model.WargearTeam;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class PlotDAO {
    public static void update(PlayerWorld playerWorld) {
        EntityManagerExecuter.run(em->{
            Plot plot = em.find(Plot.class,playerWorld.getId());
            plot.setTemplate(em.find(PlotTemplate.class,playerWorld.getTemplate().getId()));
            em.merge(plot);
        });
    }

    public static void update(TeamWorld teamWorld) {
        EntityManagerExecuter.run(em->{
            WargearTeam wargearTeam = em.find(WargearTeam.class,teamWorld.getTeam().getId());
//            wargearTeam.getTemplate().getWargearTeams().remove(wargearTeam);

            PlotTemplate plotTemplate = em.find(PlotTemplate.class,teamWorld.getTemplate().getId());
            plotTemplate.getWargearTeams().add(wargearTeam);
            System.out.println("plotTemlateID "  + plotTemplate.getId());
            wargearTeam.setTemplate(plotTemplate);
            em.merge(wargearTeam);
        });

        EntityManagerExecuter.run(em->{
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Event> cq = cb.createQuery(Event.class);
            Root<Event> root = cq.from(Event.class);

            List<Event> events = em.createQuery(cq).getResultList();
            WargearTeam wargearTeam = em.find(WargearTeam.class,teamWorld.getTeam().getId());
//            wargearTeam.getTemplate().getWargearTeams().remove(wargearTeam);

            PlotTemplate plotTemplate = em.find(PlotTemplate.class,teamWorld.getTemplate().getId());
            plotTemplate.getWargearTeams().add(wargearTeam);
            wargearTeam.setTemplate(plotTemplate);
            em.merge(wargearTeam);
        });
    }
}
