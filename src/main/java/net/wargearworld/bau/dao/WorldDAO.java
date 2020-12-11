package net.wargearworld.bau.dao;

import net.wargearworld.bau.dao.constructors.WorldEntry;
import net.wargearworld.bau.world.LocalWorldTemplate;
import net.wargearworld.bau.world.bauworld.PlayerWorld;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.bau.world.gui.WorldIcon;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.*;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

public class WorldDAO {
    public static Collection<String> getAllWorlds() {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(World.class);
            Root root = criteriaQuery.from(World.class);
            Join join = root.join(World_.OWNER);
            criteriaQuery.select(criteriaBuilder.construct(WorldEntry.class, join.get(Player_.UUID), root.get(World_.NAME)));
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

    public static WorldTemplate getTemplate(String name) {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<WorldTemplate> criteriaQuery = criteriaBuilder.createQuery(WorldTemplate.class);
            Root<WorldTemplate> root = criteriaQuery.from(WorldTemplate.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(WorldTemplate_.name), name));
            Query query = em.createQuery(criteriaQuery);

            WorldTemplate template = null;
            try {
                template = (WorldTemplate) query.getSingleResult();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return template;
        });

    }

    public static Collection<String> getAllNotAddedPlayers(long worldId) {
        return EntityManagerExecuter.run(em -> {
            World world = em.find(World.class, worldId);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> pr = cq.from(Player.class);

            Join join = pr.join(Player_.MEMBERED_WORLDS, JoinType.LEFT);
            join.on(cb.equal(join.get(WorldMember_.WORLD), worldId));
            cq.select(pr.get(Player_.NAME));
            cq.where(cb.isNull(join.get(WorldMember_.MEMBER)));
            Query query = em.createQuery(cq);

            Collection<String> out = query.getResultList();
            out.remove(world.getOwner().getName());
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
    public static void update(PlayerWorld playerWorld) {
        EntityManagerExecuter.run(em -> {
            World world = em.find(World.class, playerWorld.getId());
            world.setTemplate(em.find(WorldTemplate.class, playerWorld.getTemplate().getId()));
            world.setName(playerWorld.getName());
            try {
                em.merge(world);
            }catch(Exception exception){
                exception.printStackTrace();
            }
        });
    }

    public static void update(TeamWorld teamWorld) {
        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, teamWorld.getTeam().getId());
//            wargearTeam.getTemplate().getWargearTeams().remove(wargearTeam);

            WorldTemplate worldTemplate = em.find(WorldTemplate.class, teamWorld.getTemplate().getId());
            worldTemplate.getWargearTeams().add(wargearTeam);
            wargearTeam.setTemplate(worldTemplate);
            try {
                em.merge(wargearTeam);
            }catch(Exception exception){
                exception.printStackTrace();
            }
        });

        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, teamWorld.getTeam().getId());
//            wargearTeam.getTemplate().getWargearTeams().remove(wargearTeam);

            WorldTemplate worldTemplate = em.find(WorldTemplate.class, teamWorld.getTemplate().getId());
            worldTemplate.getWargearTeams().add(wargearTeam);
            wargearTeam.setTemplate(worldTemplate);
            try {
                em.merge(wargearTeam);
            }catch(Exception exception){
                exception.printStackTrace();
            }
        });
    }

    public static WorldIcon getWorldIcon(long worldID) {
        return EntityManagerExecuter.run(em -> {
            World dbPLot = em.find(World.class, worldID);
            Icon icon = dbPLot.getIcon();
            return new WorldIcon(icon);
        });
    }

    public static LocalWorldTemplate getTemplate(UUID owner, String name) {
        return EntityManagerExecuter.run(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<World> cq = cb.createQuery(World.class);
            Root<World> root = cq.from(World.class);
            cq.where(cb.equal(root.get(World_.owner), em.find(Player.class, owner)), cb.equal(root.get(World_.name), name));
            World world = em.createQuery(cq).getSingleResult();
            if (world != null)
                return LocalWorldTemplate.getTemplate(world.getTemplate().getName());
            return null;
        });
    }

    public static void changeToDefault(UUID owner, String name) {
        EntityManagerExecuter.run(em->{
            Player dbPlayer = em.find(Player.class,owner);
            for(World world:dbPlayer.getWorlds()){
                if(world.getDefault()){
                    world.setDefault(false);
                    try {
                        em.merge(world);
                    }catch(Exception exception){
                        exception.printStackTrace();
                    }
                    break;
                }
            }
            for(World world: dbPlayer.getWorlds()){
                if(world.getName().equals(name)){
                    world.setDefault(true);
                    try {
                        em.merge(world);
                    }catch(Exception exception){
                        exception.printStackTrace();
                    }
                    break;
                }
            }
        });
    }

    public static void setIcon(UUID owner, String name, Long id) {
        EntityManagerExecuter.run(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<World> cq = cb.createQuery(World.class);
            Root<World> root = cq.from(World.class);
            cq.where(cb.equal(root.get(World_.owner), em.find(Player.class, owner)), cb.equal(root.get(World_.name), name));
            try{
                World world = em.createQuery(cq).getSingleResult();
                Icon icon = em.find(Icon.class,id);
                world.setIcon(icon);
                try {
                    em.merge(world);
                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }catch(NoResultException ex){}
        });
    }

}
