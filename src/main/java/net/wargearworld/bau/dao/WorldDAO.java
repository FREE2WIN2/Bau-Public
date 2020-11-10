package net.wargearworld.bau.dao;

import net.wargearworld.bau.dao.constructors.WorldEntry;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.*;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

public class WorldDAO {
    public static Collection<String> getAllWorlds() {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Plot.class);
            Root root = criteriaQuery.from(Plot.class);
            Join join = root.join(Plot_.OWNER);
            criteriaQuery.select(criteriaBuilder.construct(WorldEntry.class, join.get(Player_.UUID), root.get(Plot_.NAME)));
            Query query = em.createQuery(criteriaQuery);

            Set<String> out = new TreeSet<>();
            List<WorldEntry> result = query.getResultList();
            try {
                for (WorldEntry entry : result) {

                    out.add(entry.getName());
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return out;
        });
    }

    public static PlotTemplate getTemplate(String name) {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<PlotTemplate> criteriaQuery = criteriaBuilder.createQuery(PlotTemplate.class);
            Root<PlotTemplate> root = criteriaQuery.from(PlotTemplate.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(PlotTemplate_.name), name));
            Query query = em.createQuery(criteriaQuery);

            PlotTemplate template = null;
            try {
                template = (PlotTemplate) query.getSingleResult();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return template;
        });

    }

    public static Collection<String> getAllNotAddedPlayers(long plotId) {
        return EntityManagerExecuter.run(em -> {
            Plot plot = em.find(Plot.class, plotId);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> pr = cq.from(Player.class);

            Join join = pr.join(Player_.MEMBERED_PLOTS, JoinType.LEFT);
            join.on(cb.equal(join.get(PlotMember_.PLOT), plotId));
            cq.select(pr.get(Player_.NAME));
            cq.where(cb.isNull(join.get(PlotMember_.MEMBER)));
            Query query = em.createQuery(cq);

            Collection<String> out = query.getResultList();
            out.remove(plot.getOwner().getName());
            return out;
        });
    }

    public static Collection<String> getAllWorldsOfPlayer(UUID uuid) {
        //First TeamWorld
        EntityManagerExecuter.run(em->{
            Player dbPlayer = em.find(Player.class,uuid);

        });
    return null;
    }
}
