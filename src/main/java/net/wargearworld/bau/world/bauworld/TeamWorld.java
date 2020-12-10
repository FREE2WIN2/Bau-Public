package net.wargearworld.bau.world.bauworld;

import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.WorldDAO;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.utils.MethodResult;
import net.wargearworld.bau.world.LocalWorldTemplate;
import net.wargearworld.bau.world.gui.GUITeamWorld;
import net.wargearworld.bau.world.gui.IGUIWorld;
import net.wargearworld.bau.world.gui.WorldGUI;
import net.wargearworld.bau.world.plot.PlotPattern;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamWorld extends BauWorld {

    private Team team;
    private IGUIWorld iGuiWorld;
    private Collection<LocalWorldMember> localWorldMembers;
    public TeamWorld(Team team, World w) {
        super(w);
        this.team = team;
        setTemplate(team.getTemplate().getName());
        plots = new HashMap<>();
        for (PlotPattern plotPattern : getTemplate().getPlots()) {
            plots.put(plotPattern.getID(), plotPattern.toPlot(this));
        }
        addAllMembersToRegions();
    }

    @Override
    public boolean isAuthorized(UUID uuid) {
        return team.isAuthorized(uuid);
    }

    @Override
    public void showInfo(Player p) {
        WorldGUI.openWorldInfo(p,this,false);
    }

    @Override
    public Collection<LocalWorldMember> getMembers() {
        if(localWorldMembers == null){
            localWorldMembers = team.getWorldMembers();
        }
        return localWorldMembers;
    }

    @Override
    public boolean isOwner(Player player) {
        return team.getLeaders().contains(player.getUniqueId());
    }

    @Override
    public void addTemp(String playerName, int time) {
        return;//Not implementing
    }

    @Override
    public MethodResult add(String playerName, Date to) {
        return MethodResult.ERROR;
    }

    @Override
    public void checkForTimeoutMembership() {
        return;//Not implementing
    }

    @Override
    public void removeMember(UUID member) {
        return;//Not implementing
    }

    @Override
    public void setTemplate(LocalWorldTemplate template) {
        super.template = template;
        WorldDAO.update(this);
    }

    @Override
    public void removeAllMembersFromRegions() {
        for (LocalWorldMember localWorldMember : team.getLeaders()) {
            removeMemberFromAllRegions(localWorldMember.getUuid());
        }
        for (LocalWorldMember localWorldMember : team.getMembers()) {
            removeMemberFromAllRegions(localWorldMember.getUuid());
        }
        for (LocalWorldMember localWorldMember : team.getNewcomers()) {
            removeMemberFromAllRegions(localWorldMember.getUuid());
        }
    }

    @Override
    public void addAllMembersToRegions() {
        for (LocalWorldMember localWorldMember : team.getLeaders()) {
            addPlayerToAllRegions(localWorldMember.getUuid());
        }
        for (LocalWorldMember localWorldMember : team.getMembers()) {
            addPlayerToAllRegions(localWorldMember.getUuid());
        }
        for (LocalWorldMember localWorldMember : team.getNewcomers()) {
            addPlayerToAllRegions(localWorldMember.getUuid());
        }
    }

    @Override
    public long getId() {
        return team.getId();
    }

    @Override
    public void leave(Player p) {
//no implementation
    }

    @Override
    public String rename(String newName) {
        return super.worldName;
    }

    @Override
    public IGUIWorld getGUIWorld() {
        if(iGuiWorld == null)
            iGuiWorld = new GUITeamWorld(team);
        return iGuiWorld;
    }

    @Override
    public String getOwner() {
        return team.getName();
    }

    @Override
    public boolean isMember(UUID member) {
        return team.isMember(member);
    }

    public Team getTeam() {
       return team;
    }

    @Override
    public void spawn(Player p){
        super.spawn(p);
        MessageHandler.getInstance().send(p, "world_tp", "", "Team: " + getOwner());
    }

    public void update() {
        super.updateScoreboards();
        for(Player player: getWorld().getPlayers()){
            if(!isAuthorized(player.getUniqueId())){
                player.performCommand("gs");
                MessageHandler.getInstance().send(player,"team_world_noLongerAuthorised");
            }
        }
    }
}
