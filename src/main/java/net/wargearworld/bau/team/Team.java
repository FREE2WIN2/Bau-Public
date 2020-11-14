package net.wargearworld.bau.team;

import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.WargearTeam;
import net.wargearworld.db.model.WargearTeamMember;

import java.util.*;

public class Team {
    private Long id;
    private String name;
    private String abbreviation;
    private Set<UUID> leaders;
    private Set<UUID> members;
    private Set<UUID> newcomers;

    public Team(Long id) {

        this.id = Objects.requireNonNull(id);
        System.out.println(this.id);
        leaders = new HashSet<>();
        members = new HashSet<>();
        newcomers = new HashSet<>();
        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            this.abbreviation = wargearTeam.getAbbreviation();
            this.name = wargearTeam.getName();
            for (WargearTeamMember wargearTeamMember : wargearTeam.getMembers()) {
                UUID uuid = wargearTeamMember.getMember().getUuid();
                if (wargearTeamMember.isLeader()) {
                    leaders.add(uuid);
                } else if (wargearTeamMember.isNewcomer()) {
                    newcomers.add(uuid);
                } else {
                    members.add(uuid);
                }
            }
        });
    }

    public boolean isMember(UUID playerUUID) {
        if (leaders.contains(playerUUID) || members.contains(playerUUID))
            return true;
        return false;
    }

    public boolean isAuthorized(UUID uuid) {
        return leaders.contains(uuid) || members.contains(uuid);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public Set<UUID> getLeaders() {
        return leaders;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getNewcomers() {
        return newcomers;
    }

    public Collection<String> getMemberNames() {
        List<String> out = new ArrayList<>();
        out.addAll(DatabaseDAO.getNames(leaders));
        out.addAll(DatabaseDAO.getNames(members));
        out.addAll(DatabaseDAO.getNames(newcomers));
        return out;
    }

    public WorldTemplate getTemplate() {
        return  EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            return WorldTemplate.getTemplate(wargearTeam.getTemplate().getName());
        });
    }

    public void setName(String name) {
        this.name = name;
        EntityManagerExecuter.run(em->{
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            wargearTeam.setName(name);
            em.merge(wargearTeam);
        });
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
        EntityManagerExecuter.run(em->{
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            wargearTeam.setAbbreviation(abbreviation);
            em.merge(wargearTeam);
        });
    }

    public void setNewcomers(Set<UUID> newcomers) {
        this.newcomers = newcomers;
    }

}
