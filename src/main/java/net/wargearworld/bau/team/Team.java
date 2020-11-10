package net.wargearworld.bau.team;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.WargearTeam;
import net.wargearworld.db.model.WargearTeamMember;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Team {
    public static Team of(BauPlayer bauPlayer) {
        return null;//TODO

    }

    private Long id;
    private String name;
    private String abbreviation;
    private Set<UUID> leaders;
    private Set<UUID> members;

    public Team(Long id) {

        this.id = Objects.requireNonNull(id);
        leaders = new HashSet<>();
        members = new HashSet<>();

        EntityManagerExecuter.run(em->{
            WargearTeam wargearTeam = em.find(WargearTeam.class,this.id);
            this.abbreviation = wargearTeam.getAbbreviation();
            this.name = wargearTeam.getName();
            for(WargearTeamMember wargearTeamMember:wargearTeam.getMembers()){
                if(wargearTeamMember.getTask().equalsIgnoreCase("leader")){
                    leaders.add(wargearTeamMember.getMember().getUuid());
                }else{
                    members.add(wargearTeamMember.getMember().getUuid());
                }
            }
        });
    }

    public boolean isMember(UUID playerUUID){
        if(leaders.contains(playerUUID)||members.contains(playerUUID))
            return true;
        return false;
    }
}
