package net.wargearworld.bau.team;

import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.WorldTemplate;
import net.wargearworld.bau.world.bauworld.TeamWorld;
import net.wargearworld.bau.world.bauworld.WorldMember;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.WargearTeam;
import net.wargearworld.db.model.WargearTeamMember;
import org.bukkit.World;

import java.util.*;

public class Team {
    private Long id;
    private String name;
    private String abbreviation;
    private Set<WorldMember> leaders;
    private Set<WorldMember> members;
    private Set<WorldMember> newcomers;

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
        for (WorldMember worldMember : leaders) {
            if (worldMember.getUuid().equals(uuid))
                return true;
        }
        for (WorldMember worldMember : members) {
            if (worldMember.getUuid().equals(uuid))
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

    public Set<WorldMember> getLeaders() {
        return leaders;
    }

    public Set<WorldMember> getMembers() {
        return members;
    }

    public Set<WorldMember> getNewcomers() {
        return newcomers;
    }

    public Collection<String> getMemberNames() {
        List<String> out = new ArrayList<>();
        getNameOfMembers(leaders, out);
        getNameOfMembers(members, out);
        getNameOfMembers(newcomers, out);
        return out;
    }

    private void getNameOfMembers(Collection<WorldMember> membersList, Collection<String> outList) {
        for (WorldMember worldMember : membersList) {
            outList.add(worldMember.getName());
        }
    }

    public WorldTemplate getTemplate() {
        return EntityManagerExecuter.run(em -> {
            WargearTeam wargearTeam = em.find(WargearTeam.class, this.id);
            return WorldTemplate.getTemplate(wargearTeam.getTemplate().getName());
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

    public void setNewcomers(Set<WorldMember> newcomers) {
        this.newcomers = newcomers;
    }

    public Collection<WorldMember> getWorldMembers() {
        List<WorldMember> out = new ArrayList<>(leaders);
        out.addAll(members);
        out.addAll(newcomers);
        return out;
    }

    public boolean isLeader(UUID uniqueId) {
        for (WorldMember worldMember : leaders) {
            if (worldMember.getUuid().equals(uniqueId)) {
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
                WorldMember worldMember = new WorldMember(name, uuid,!wargearTeamMember.isNewcomer());
                if (wargearTeamMember.isLeader()) {
                    leaders.add(worldMember);
                } else if (wargearTeamMember.isNewcomer()) {
                    newcomers.add(worldMember);
                } else {
                    members.add(worldMember);
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
