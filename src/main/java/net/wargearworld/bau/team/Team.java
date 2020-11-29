package net.wargearworld.bau.team;

import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.LocalWorldTemplate;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.bau.world.bauworld.LocalWorldMember;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.WargearTeam;
import net.wargearworld.db.model.WargearTeamMember;

import java.util.*;

public class Team {
    private Long id;
    private String name;
    private String abbreviation;
    private Set<LocalWorldMember> leaders;
    private Set<LocalWorldMember> members;
    private Set<LocalWorldMember> newcomers;

    public Team(Long id) {
        this.id = Objects.requireNonNull(id);
        leaders = new HashSet<>();
        members = new HashSet<>();
        newcomers = new HashSet<>();
        update();
    }

    public boolean isMember(UUID playerUUID) {
        return isAuthorized(playerUUID);
    }

    public boolean isAuthorized(UUID uuid) {
        for (LocalWorldMember localWorldMember : leaders) {
            if (localWorldMember.getUuid().equals(uuid))
                return true;
        }
        for (LocalWorldMember localWorldMember : members) {
            if (localWorldMember.getUuid().equals(uuid))
                return true;
        }
        return false;
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

    public Set<LocalWorldMember> getLeaders() {
        return leaders;
    }

    public Set<LocalWorldMember> getMembers() {
        return members;
    }

    public Set<LocalWorldMember> getNewcomers() {
        return newcomers;
    }

    public Collection<String> getMemberNames() {
        List<String> out = new ArrayList<>();
        getNameOfMembers(leaders, out);
        getNameOfMembers(members, out);
        getNameOfMembers(newcomers, out);
        return out;
    }

    private void getNameOfMembers(Collection<LocalWorldMember> membersList, Collection<String> outList) {
        for (LocalWorldMember localWorldMember : membersList) {
            outList.add(localWorldMember.getName());
        }
    }

    public LocalWorldTemplate getTemplate() {
        return EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            return LocalWorldTemplate.getTemplate(wargearTeam.getTemplate().getName());
        });
    }

    public void setName(String name) {
        this.name = name;
        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            wargearTeam.setName(name);
            em.merge(wargearTeam);
        });
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            wargearTeam.setAbbreviation(abbreviation);
            em.merge(wargearTeam);
        });
    }

    public void setNewcomers(Set<LocalWorldMember> newcomers) {
        this.newcomers = newcomers;
    }

    public Collection<LocalWorldMember> getWorldMembers() {
        List<LocalWorldMember> out = new ArrayList<>(leaders);
        out.addAll(members);
        out.addAll(newcomers);
        return out;
    }

    public boolean isLeader(UUID uniqueId) {
        for (LocalWorldMember localWorldMember : leaders) {
            if (localWorldMember.getUuid().equals(uniqueId)) {
                return true;
            }
        }
        return false;
    }

    public void update() {
        EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            this.abbreviation = wargearTeam.getAbbreviation();
            this.name = wargearTeam.getName();
            for (WargearTeamMember wargearTeamMember : wargearTeam.getMembers()) {
                UUID uuid = wargearTeamMember.getMember().getUuid();
                String name = wargearTeamMember.getMember().getName();
                LocalWorldMember localWorldMember = new LocalWorldMember(name, uuid,!wargearTeamMember.isNewcomer());
                if (wargearTeamMember.isLeader()) {
                    leaders.add(localWorldMember);
                } else if (wargearTeamMember.isNewcomer()) {
                    newcomers.add(localWorldMember);
                } else {
                    members.add(localWorldMember);
                }
            }
        });

        if(WorldManager.containsTeamWorld(this)){
            ((TeamWorld)WorldManager.getTeamWorld(this)).update();
        }
    }

    public boolean isNewcomer(UUID uniqueId) {
        return newcomers.contains(uniqueId);
    }
}
