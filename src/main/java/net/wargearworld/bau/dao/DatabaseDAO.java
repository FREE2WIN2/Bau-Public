package net.wargearworld.bau.dao;

import net.wargearworld.bau.dao.constructors.WorldEntry;
import net.wargearworld.db.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;

@ApplicationScoped
@Transactional
public class DatabaseDAO {
    @Inject
    private EntityManager em;

    /**
     * @param uuid The UUID of the Player who wants to know his plots he can join
     * @return Player
     */

    public Player getPlayer(UUID uuid) {
        Player player = em.find(Player.class, uuid);
        return player;
    }

    public Player getPlayer(String name) {
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
        return player;
    }

    public Plot getPlot(long id) {
        Plot plot = em.find(Plot.class, id);
        return plot;
    }

    public void persist(Object obj) {
        if (obj == null)
            return;
        if (em.contains(obj)) {
            em.merge(obj);
        } else {
            em.persist(obj);
        }
    }

    public void update(Object obj, EntityManager em) {
        if (obj == null)
            return;
        em.merge(obj);
    }



    public void remove(Object object) {
        if (object == null)
            return;
        em.remove(object);
    }

    public Collection<String> getAllWorlds() {
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
    }

    public PlotTemplate getTemplate(String name) {
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
    }

    public void sendMail(String sender, Player receiver, String message) {
        Mail mail = new Mail();
        mail.setMessage(message);
        mail.setReceiver(receiver);
        mail.setSender(sender);
        persist(mail);
    }

    public Collection<String> getAllNotAddedPlayers(long plotId) {
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
    }

    public void persist(EntityManager em, Object... objects) {
        for (Object obj : objects) {
            if (em.contains(obj)) {
                em.merge(obj);
            } else {
                em.persist(obj);
            }
        }
    }

    public UUID getUUID(String playerName) {
        EntityManager em = CDI.current().select(EntityManager.class).get();
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
    }

}
