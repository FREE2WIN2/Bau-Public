package net.wargearworld.Bau.HikariCP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import net.wargearworld.db.model.*;
import net.wargearworld.thedependencyplugin.DependencyProvider;

import net.wargearworld.Bau.World.WorldManager;
import net.wargearworld.Bau.World.WorldTemplate;

public class DBConnection {
    /**
     * @param uuid -> The UUID of the Player who wants to know his plots he can join
     */

    public static Player getPlayer(UUID uuid) {
        EntityManager em = DependencyProvider.getEntityManager();
        return em.find(Player.class, uuid);
    }

    public static Player getPlayer(String name) {
        EntityManager em = DependencyProvider.getEntityManager();
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
        EntityManager em = DependencyProvider.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Plot> criteriaQuery = criteriaBuilder.createQuery(Plot.class);
        Root<Plot> root = criteriaQuery.from(Plot.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(Plot_.name), name),criteriaBuilder.equal(root.get(Plot_.owner),getPlayer(owner)));
        Query query = em.createQuery(criteriaQuery);
        return (Plot) query.getSingleResult();
    }

    public static void persist(Object obj){
        EntityManager em = DependencyProvider.getEntityManager();
        em.persist(obj);
        flushAndClear(em);
    }

    private static void flushAndClear(EntityManager em){
        em.flush();
        em.clear();
    }

    public static String getLanguage(String uuid) {
        return getPlayer(uuid).getCountryCode();
    }

    public static void remove(Object object) {
        EntityManager em = DependencyProvider.getEntityManager();
        em.remove(object);
        flushAndClear(em);
    }

    public static Collection<String> getAllWorlds() {
        return null;
    }

    public static PlotTemplate getTemplate(String name) {
        System.out.println(name);
        EntityManager em = DependencyProvider.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<PlotTemplate> criteriaQuery = criteriaBuilder.createQuery(PlotTemplate.class);
        Root<PlotTemplate> root = criteriaQuery.from(PlotTemplate.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(PlotTemplate_.name), name));
        Query query = em.createQuery(criteriaQuery);
        return (PlotTemplate) query.getSingleResult();
    }
}
