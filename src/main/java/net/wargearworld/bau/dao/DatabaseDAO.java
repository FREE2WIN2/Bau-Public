package net.wargearworld.bau.dao;

import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.*;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

public class DatabaseDAO {

    /**
     * @param uuid The UUID of the Player who wants to know his plots he can join
     * @return Player
     */

    public static Player getPlayer(UUID uuid) {
        return EntityManagerExecuter.run(em -> {
            return em.find(Player.class, uuid);
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


    public static void sendMail(String sender, Player receiver, String message) {
        EntityManagerExecuter.run(em -> {
            Mail mail = new Mail();
            mail.setMessage(message);
            mail.setReceiver(receiver);
            mail.setSender(sender);
            em.persist(mail);
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

    public static Collection<? extends String> getNames(Set<UUID> uuids) {
        List<String> out = new ArrayList();
        EntityManagerExecuter.run(em -> {
            for (UUID uuid : uuids) {
                out.add(em.find(Player.class, uuid).getName());
            }
        });
        return out;
    }
}
