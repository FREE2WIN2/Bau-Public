package net.wargearworld.bau.hikariCP;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.db.model.*;
import net.wargearworld.thedependencyplugin.DependencyProvider;

public class DBConnection {
    private static EntityManager em = DependencyProvider.getEntityManager();

    /**
     * @param uuid -> The UUID of the Player who wants to know his plots he can join
     */

    public static Player getPlayer(UUID uuid) {
        return em.find(Player.class, uuid);
    }

    public static Player getPlayer(String name) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Player.class);
        Root root = criteriaQuery.from(Player.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(Player_.name), name));
        Query query = em.createQuery(criteriaQuery);
        return (Player) query.getSingleResult();
    }

    public static Plot getPlot(long id) {
        EntityManager em = DependencyProvider.getEntityManager();
        return em.find(Plot.class, id);
    }

    public static Plot getPlot(UUID owner, String name) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Plot> criteriaQuery = criteriaBuilder.createQuery(Plot.class);
        Root<Plot> root = criteriaQuery.from(Plot.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(Plot_.name), name),criteriaBuilder.equal(root.get(Plot_.owner),BauPlayer.getBauPlayer(owner).getDbPlayer()));
        Query query = em.createQuery(criteriaQuery);
        return (Plot) query.getSingleResult();
    }

    public static void persist(Object obj){
        em.getTransaction().begin();
        em.persist(obj);
        em.merge(obj);
        em.getTransaction().commit();
    }

    public static void update(Object obj){
        em.getTransaction().begin();
        em.merge(obj);
        em.getTransaction().commit();
    }

    public static String getLanguage(String uuid) {
        return getPlayer(uuid).getCountryCode();
    }

    public static void remove(Object object) {
        em.getTransaction().begin();
        em.remove(object);
        em.getTransaction().commit();
    }

    public static Collection<String> getAllWorlds() {
        return null;
    }

    public static PlotTemplate getTemplate(String name) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<PlotTemplate> criteriaQuery = criteriaBuilder.createQuery(PlotTemplate.class);
        Root<PlotTemplate> root = criteriaQuery.from(PlotTemplate.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(PlotTemplate_.name), name));
        Query query = em.createQuery(criteriaQuery);
        return (PlotTemplate) query.getSingleResult();
    }

    public static void sendMail(String sender, Player receiver, String message){
        Mail mail = new Mail();
        mail.setMessage(message);
        mail.setReceiver(receiver);
        mail.setSender(sender);
        DBConnection.persist(mail);
    }
}
