package net.wargearworld.bau.dao;

import net.wargearworld.bau.config.BauConfig;
import net.wargearworld.bau.world.LocalWorldTemplate;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

public class PlayerDAO {

    public static Map<LocalWorldTemplate, Boolean> getPlayersTeamplates(UUID playerUUID) {
        return EntityManagerExecuter.run(em -> {
            TreeMap<LocalWorldTemplate, Boolean> out = new TreeMap<>();
            Player dbPlayer = em.find(Player.class, playerUUID);
            for (PlayerWorldTemplate playerWorldTemplate : dbPlayer.getPlayerWorldTemplates()) {
                LocalWorldTemplate localWorldTemplate = LocalWorldTemplate.getTemplate(playerWorldTemplate.getWorldTemplate().getName());
                out.put(localWorldTemplate, true);
            }

            /*Get All WorldTemplates*/

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<LocalWorldTemplate> cq = cb.createQuery(LocalWorldTemplate.class);
            cq.from(LocalWorldTemplate.class);
            em.createQuery(cq).getResultList().forEach(s -> {
                LocalWorldTemplate localWorldTemplate = LocalWorldTemplate.getTemplate(s.getName());
                if (!out.containsKey(localWorldTemplate)) {
                    out.put(localWorldTemplate, false);
                }
            });
            return out;
        });
    }

    public static void addWorldTemplate(LocalWorldTemplate localWorldTemplate, UUID playerUUID) {
        EntityManagerExecuter.run(em -> {
            Player dbPlayer = em.find(Player.class, playerUUID);
            WorldTemplate worldTemplate = em.find(WorldTemplate.class, localWorldTemplate.getId());
            PlayerWorldTemplate playerWorldTemplate = new PlayerWorldTemplate();
            dbPlayer.addPlayerWorldTemplate(playerWorldTemplate);
            worldTemplate.addPlayerWorldTemplate(playerWorldTemplate);
            em.persist(playerWorldTemplate);
        });
    }

    public static WorldTemplate getDefaultWorldTemplate(UUID uuid) {
        return EntityManagerExecuter.run(em -> {
            Player dbPlayer = em.find(Player.class, uuid);
            for (World world : dbPlayer.getWorlds()) {
                if (world.getDefault()) {
                    return world.getTemplate();
                }
            }
            return null;
        });
    }

    public static void addNewWorld(String worldName, UUID uuid, boolean isDefault) {
        EntityManagerExecuter.run(em -> {
            Player dbPlayer = em.find(Player.class, uuid);
            WorldTemplate worldTemplate = em.find(WorldTemplate.class,BauConfig.getInstance().getDefaultTemplate().getId());
            World world = new World(worldName,dbPlayer, worldTemplate,isDefault);
            Icon icon = em.find(Icon.class,2l);
            world.setIcon(icon);
            em.persist(world);
        });
    }

    public static Collection<String> getPlayersAddedWorldsPlayerNames(UUID playerUUID) {
        return EntityManagerExecuter.run(em->{
           Set<String> out = new TreeSet<>();
           Player dbPlayer = em.find(Player.class,playerUUID);
           for(WorldMember worldMember: dbPlayer.getMemberedWorlds()){
               out.add(worldMember.getWorld().getOwner().getName());
           }
           return out;
        });
    }
    public static Collection<String> getPlayersWorldNames(String playerName) {
        return EntityManagerExecuter.run(em->{
            Set<String> out = new TreeSet<>();
            Player dbPlayer = em.find(Player.class,DatabaseDAO.getUUID(playerName));
            for(World world: dbPlayer.getWorlds()){
                out.add(world.getName());
            }
            return out;
        });
    }

    public static String getDefaultWorldName(UUID uuid) {
        return EntityManagerExecuter.run(em->{
            Player dbPlayer = em.find(Player.class,uuid);
            for(World world: dbPlayer.getWorlds()){
                if(world.getDefault()){
                    return world.getName();
                }
            }
            return dbPlayer.getWorlds().iterator().next().getName();
        });
    }

    public static Collection<String> getAllPlayersWithWorld() {
        return EntityManagerExecuter.run(em->{
            Set<String> out = new TreeSet<>();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<World> root = cq.from(World.class);
            cq.distinct(true);
            cq.select(root.get(World_.owner.getName()));
            List<Player> players =  em.createQuery(cq).getResultList();
            players.forEach(player -> {
                out.add(player.getName());
            });
            return out;
        });
    }

    public static Collection<WorldMember> getMemberedWorlds(UUID uuid){
        return EntityManagerExecuter.run(em->{
            Player dbPlayer = em.find(Player.class,uuid);
            return dbPlayer.getMemberedWorlds();
        });
    }

    public static String getName(UUID owner) {
        return EntityManagerExecuter.run(em->{
            Player dbPlayer = em.find(Player.class,owner);
            return dbPlayer.getName();
        });
    }
}
