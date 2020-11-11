package net.wargearworld.bau.world.bauworld;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.utils.MethodResult;
import net.wargearworld.bau.world.plot.PlotPattern;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamWorld extends BauWorld {

    private Team team;

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
        boolean isOwner = isOwner(p);
        Main.send(p, "memberListHeader", getName());
        for (String memberName : getMemberNames()) {
            String hover = MessageHandler.getInstance().getString(p, "memberHoverRemove").replace("%r", memberName);
            JsonCreater remove = new JsonCreater("§7[§6" + memberName + "§7]");
            if (isOwner) {
                remove.addClickEvent("/gs remove " + memberName, ClickAction.SUGGEST_COMMAND).addHoverEvent(hover);
            }
            remove.send(p);
        }
        if (isOwner) {
            new JsonCreater("§a[+]§r  ").addClickEvent("/gs add ", ClickAction.SUGGEST_COMMAND)
                    .addHoverEvent(MessageHandler.getInstance().getString(p, "addMemberHover")).send(p);
        }
        Main.send(p, "timeShow", p.getWorld().getTime() + "");
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
    public void removeAllMembersFromRegions() {
        for (UUID uuid : team.getLeaders()) {
            removeMemberFromAllRegions(uuid);
        }
        for (UUID uuid : team.getMembers()) {
            removeMemberFromAllRegions(uuid);
        }
        for (UUID uuid : team.getNewcomers()) {
            removeMemberFromAllRegions(uuid);
        }
    }

    @Override
    public void addAllMembersToRegions() {
        for (UUID uuid : team.getLeaders()) {
            addPlayerToAllRegions(uuid);
        }
        for (UUID uuid : team.getMembers()) {
            addPlayerToAllRegions(uuid);
        }
        for (UUID uuid : team.getNewcomers()) {
            addPlayerToAllRegions(uuid);
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

    @Override
    public Collection<String> getMemberNames() {
        return team.getMemberNames();
    }

}
