package net.wargearworld.bau.dao;

import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Player;
import net.wargearworld.db.model.PlayerPlotTemplate;
import net.wargearworld.db.model.PlotTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class PlayerDAO {

    public static Map<WorldTemplate,Boolean> getPlayersTeamplates(UUID playerUUID){
        return EntityManagerExecuter.run(em->{
            TreeMap<WorldTemplate,Boolean> out = new TreeMap<>();
            Player dbPlayer = em.find(Player.class,playerUUID);
            for(PlayerPlotTemplate playerPlotTemplate:dbPlayer.getPlayerPlotTemplates()){
                WorldTemplate worldTemplate = WorldTemplate.getTemplate(playerPlotTemplate.getPlotTemplate().getName());
                out.put(worldTemplate,true);
            }

            /*Get All PlotTemplates*/

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PlotTemplate> cq = cb.createQuery(PlotTemplate.class);
            cq.from(PlotTemplate.class);
            em.createQuery(cq).getResultList().forEach(s->{
                WorldTemplate worldTemplate = WorldTemplate.getTemplate(s.getName());
                if(!out.containsKey(worldTemplate)){
                    out.put(worldTemplate,false);
                }
            });
            return out;
        });
    }
}
