package net.wargearworld.bau.dao;

import net.wargearworld.bau.dao.constructors.WorldEntry;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.*;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

public class DatabaseDAO {

    /**
     * @param uuid The UUID of the Player who wants to know his plots he can join
     * @return Player
     */

    public static Player getPlayer(UUID uuid) {
        return EntityManagerExecuter.run(em -> {
            em.find(Player.class, uuid);
        });
    }

    public static Player getPlayer(String name) {
        if (name == null)
            return null;
        return EntityManagerExecuter.run(em -> {


            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
            Root<Player> root = criteriaQuery.from(Player.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(Player_.name), name));

            Query query = em.createQuery(criteriaQuery);
            Player player = null;
            try {
                player = (Player) query.getSingleResult();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return player;
        });
    }

    public static Plot getPlot(long id) {
        return EntityManagerExecuter.run(em -> {
            return em.find(Plot.class, id);
        });
    }

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

    public static void sendMail(String sender, Player receiver, String message) {
        EntityManagerExecuter.run(em -> {
            Mail mail = new Mail();
            mail.setMessage(message);
            mail.setReceiver(receiver);
            mail.setSender(sender);
            em.persist(mail);
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

    public static UUID getUUID(String playerName) {
        return EntityManagerExecuter.run(em -> {
            if (playerName == null)
                return null;
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
            Root<Player> root = criteriaQuery.from(Player.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(Player_.name), playerName));

            Query query = em.createQuery(criteriaQuery);
            Player player = null;
            try {
                player = (Player) query.getSingleResult();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return player.getUuid();
        });

    }

}
