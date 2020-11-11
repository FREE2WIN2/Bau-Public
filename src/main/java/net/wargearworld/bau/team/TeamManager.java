package net.wargearworld.bau.team;

import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.Player;
import net.wargearworld.db.model.WargearTeamMember;
import net.wargearworld.db.model.WargearTeamMember_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {

    private static Map<Long, Team> teams = new HashMap<>();

    public static Team getTeam(Long teamID) {
        if (teamID == null)
            return null;
        Team team = teams.get(teamID);
        if (team == null) {
            team = new Team(teamID);
            teams.put(teamID, team);
        }
        return team;
    }

    public static Team getTeam(UUID playerUUID) {
        Long teamID = EntityManagerExecuter.run(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<WargearTeamMember> criteriaQuery = cb.createQuery(WargearTeamMember.class);
            Root<WargearTeamMember> root = criteriaQuery.from(WargearTeamMember.class);

            criteriaQuery.where(cb.equal(root.get(WargearTeamMember_.member), em.find(Player.class, playerUUID)));
            WargearTeamMember wargearTeamMember = em.createQuery(criteriaQuery).getSingleResult();
            return wargearTeamMember.getTeam().getId();
        });
        return getTeam(teamID);
    }

    public static void updateTeams() {
        teams.clear(); //So every single Team will be updated
    }
}
