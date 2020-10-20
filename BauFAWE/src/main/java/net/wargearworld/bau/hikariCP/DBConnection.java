package net.wargearworld.bau.hikariCP;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;

import net.wargearworld.StringGetter.Language;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.db.model.*;
import net.wargearworld.thedependencyplugin.DependencyProvider;

public class DBConnection {

    /**
     * @param uuid -> The UUID of the Player who wants to know his plots he can join
     */

    public static Player getPlayer(UUID uuid) {
        EntityManager em = DependencyProvider.getEntityManager();
        Player player = em.find(Player.class, uuid);
        em.close();
        return player;
    }

    public static Player getPlayer(String name) {
        EntityManager em = DependencyProvider.getEntityManager();
        if (name == null)
            return null;
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
        em.close();
        return player;
    }

    public static Plot getPlot(long id) {
        EntityManager em = DependencyProvider.getEntityManager();
        Plot plot = em.find(Plot.class, id);
        em.close();
        return plot;
    }

    public static Plot getPlot(UUID owner, String name) {
        EntityManager em = DependencyProvider.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Plot> criteriaQuery = criteriaBuilder.createQuery(Plot.class);
        Root<Plot> root = criteriaQuery.from(Plot.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(Plot_.name), name), criteriaBuilder.equal(root.get(Plot_.owner), BauPlayer.getBauPlayer(owner).getDbPlayer()));
        Query query = em.createQuery(criteriaQuery);

        Plot plot = null;
        try {
            plot = (Plot) query.getSingleResult();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        em.close();
        return plot;
    }

    public static void persist(Object obj) {
        EntityManager em = DependencyProvider.getEntityManager();
        if (obj == null)
            return;
        em.getTransaction().begin();
        if (em.contains(obj)) {
            em.merge(obj);
        } else {
            em.persist(obj);
        }
        em.getTransaction().commit();
    }

    public static void update(Object obj, EntityManager em) {
        if (obj == null)
            return;
        em.getTransaction().begin();
        em.merge(obj);
        em.getTransaction().commit();
    }

    public static Language getLanguage(UUID uuid) {
        BauPlayer bauPlayer = BauPlayer.getBauPlayer(uuid);
        return bauPlayer.getLanguage();
    }

    public static void remove(Object object) {
        EntityManager em = DependencyProvider.getEntityManager();
        if (object == null)
            return;
        em.getTransaction().begin();
        em.remove(object);
        em.getTransaction().commit();
        em.close();
    }

    public static Collection<String> getAllWorlds() {
        EntityManager em = DependencyProvider.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Plot.class);
        Root root = criteriaQuery.from(Plot.class);
        Join join = root.join(Plot_.OWNER);
        criteriaQuery.select(criteriaBuilder.construct(WorldEntry.class,join.get(Player_.UUID), root.get(Plot_.NAME)));
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
        em.close();
        return out;
    }

    public static PlotTemplate getTemplate(String name) {
        EntityManager em = DependencyProvider.getEntityManager();
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
        em.close();
        return template;
    }

    public static void sendMail(String sender, Player receiver, String message) {
        Mail mail = new Mail();
        mail.setMessage(message);
        mail.setReceiver(receiver);
        mail.setSender(sender);
        DBConnection.persist(mail);
    }

    public static Collection<String> getAllNotAddedPlayers(long plotId) {
        EntityManager em = DependencyProvider.getEntityManager();
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
        em.close();
        return out;
    }

    public static void persist(EntityManager em, Object... objects) {
        for (Object obj : objects) {
            if (em.contains(obj)) {
                em.merge(obj);
            } else {
                em.persist(obj);
            }
        }
        em.flush();
    }

    public static UUID getUUID(String playerName) {
        EntityManager em = DependencyProvider.getEntityManager();
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
        em.close();
        return player.getUuid();
    }


    public static long getTemplateId(String name) {
        EntityManager em = DependencyProvider.getEntityManager();
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
        long id = template.getId();
        em.close();
        return id;
    }
}
