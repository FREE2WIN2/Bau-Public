package net.wargearworld.bau.world.bauworld;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.PlotDAO;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.utils.MethodResult;
import net.wargearworld.bau.world.WorldTemplate;
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
    private Collection<WorldMember> worldMembers;
    public TeamWorld(Team team, World w) {
        super(w);
        this.team = team;
        setTemplate(team.getTemplate().getName());
        plots = new HashMap<>();
        for (PlotPattern plotPattern : getTemplate().getPlots()) {
            plots.put(plotPattern.getID(), plotPattern.toPlot(this));
        }
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
    public Collection<WorldMember> getMembers() {
        if(worldMembers == null){
            worldMembers = team.getWorldMembers();
        }
        return worldMembers;
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
    public void setTemplate(WorldTemplate template) {
        super.template = template;
        PlotDAO.update(this);
    }

    @Override
    public void removeAllMembersFromRegions() {
        for (WorldMember worldMember : team.getLeaders()) {
            removeMemberFromAllRegions(worldMember.getUuid());
        }
        for (WorldMember worldMember : team.getMembers()) {
            removeMemberFromAllRegions(worldMember.getUuid());
        }
        for (WorldMember worldMember : team.getNewcomers()) {
            removeMemberFromAllRegions(worldMember.getUuid());
        }
    }

    @Override
    public void addAllMembersToRegions() {
        for (WorldMember worldMember : team.getLeaders()) {
            addPlayerToAllRegions(worldMember.getUuid());
        }
        for (WorldMember worldMember : team.getMembers()) {
            addPlayerToAllRegions(worldMember.getUuid());
        }
        for (WorldMember worldMember : team.getNewcomers()) {
            addPlayerToAllRegions(worldMember.getUuid());
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
        return null;
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
        MessageHandler.getInstance().send(p, "world_tp", getName(), "Team: " + getOwner());
    }
}
