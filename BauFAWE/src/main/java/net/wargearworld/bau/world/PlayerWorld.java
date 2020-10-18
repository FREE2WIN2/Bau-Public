package net.wargearworld.bau.world;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.wargearworld.bau.world.plots.PlotPattern;
import net.wargearworld.db.model.Mail;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.utils.MethodResult;

public class PlayerWorld extends BauWorld {

    UUID owner;
    private Plot dbPlot;

    public PlayerWorld(long id, String owner, World world) {
        super(world);
        dbPlot = DBConnection.getPlot(id);
        this.owner = UUID.fromString(owner);
        setTemplate(dbPlot.getTemplate().getName());
        plots = new HashMap<>();
        for (PlotPattern plotPattern : getTemplate().getPlots()) {
            plots.put(plotPattern.getID(), plotPattern.toPlot(this));
        }
    }

    @Override
    public void showInfo(Player p) {
        boolean isOwner = isOwner(p);
        Set<PlotMember> members = dbPlot.getMembers();
        Main.send(p, "memberListHeader", getName());
        for (PlotMember plotMember : members) {
            String memberName = plotMember.getMember().getName();
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
    public boolean isMember(UUID memberUUID) {
        for (PlotMember member : dbPlot.getMembers()) {
            System.out.println(member.getMember().getUuid());
            if (member.getMember().getUuid().equals(memberUUID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAuthorized(UUID uuid) {
        return owner.equals(uuid) || isMember(uuid);
    }

    @Override
    public boolean isOwner(Player player) {
        return owner.equals(player.getUniqueId());
    }

    @Override
    public void addTemp(String playerName, int time) {
        BauPlayer p = BauPlayer.getBauPlayer(owner);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.add(Calendar.HOUR_OF_DAY, time);
        Date to = calendar.getTime();
        if (add(playerName, to) == MethodResult.SUCCESS) {
            Main.send(p, "memberTempAdded", playerName, "" + time);
            super.log(WorldAction.ADD, playerName, playerName, time + "");
        }
    }

    @Override
    public void removeMember(UUID member) {
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if (member.toString().equals(this.owner)) {
            if (ownerPlayer != null)
                Main.send(ownerPlayer, "YouCantRemoveYourself");
        } else {
            List<PlotMember> members = new ArrayList<>(dbPlot.getMembers());
            for (PlotMember plotMember : members) {
                if (plotMember.getMember().getUuid().equals(member)) {
                    dbPlot.getMembers().remove(plotMember);
                    DBConnection.persist(dbPlot);
                    DBConnection.remove(plotMember);
                    return;
                }
            }
            removeMemberFromAllRegions(member);

            BauPlayer memberBauPlayer = BauPlayer.getBauPlayer(member);
            log(WorldAction.REMOVE, member.toString(), memberBauPlayer.getName());
            Player memberPlayer = Bukkit.getPlayer(member);
            if (memberPlayer != null) {
                Main.send(memberPlayer, "plotMemberRemove_memberMsg", getName());
                if (WorldManager.get(memberPlayer.getWorld()) == this) {
                    memberPlayer.performCommand("gs");
                }
            }
            if (ownerPlayer != null) {
                Main.send(ownerPlayer, "plotMemberRemoved", memberBauPlayer.getName());
            } else {
                DBConnection.addMail("plugin: BAU", BauPlayer.getBauPlayer(owner).getDbPlayer(),
                        MessageHandler.getInstance().getString(owner, "plotMemberRemoved").replace("%r", memberBauPlayer.getName()));
            }
        }
    }

    @Override
    public void setTemplate(String templateName) {
        WorldTemplate template = WorldTemplate.getTemplate(templateName);
        super.setTemplate(template);
    }

    @Override
    public void removeAllMembersFromRegions() {
        for (PlotMember member : dbPlot.getMembers()) {
            for (ProtectedRegion region : super.regionManager.getRegions().values()) {
                DefaultDomain members = region.getMembers();
                members.removePlayer(member.getMember().getUuid());
                region.setMembers(members);
            }
        }
        try {
            regionManager.saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addAllMembersToRegions() {
        for (PlotMember member : dbPlot.getMembers()) {
            for (ProtectedRegion region : regionManager.getRegions().values()) {
                DefaultDomain members = region.getMembers();
                members.addPlayer(member.getMember().getUuid());
                region.setMembers(members);
            }
        }
        try {
            regionManager.saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MethodResult add(String playerName, Date to) {

        BauPlayer p = BauPlayer.getBauPlayer(owner);
        net.wargearworld.db.model.Player member = DBConnection.getPlayer(playerName);
        UUID uuidMember = member.getUuid();
        if (!isAuthorized(uuidMember)) {
            if (to != null) {
            dbPlot.addMember(member,new Timestamp(to.getTime()).toLocalDateTime(),true);
            }else{
                dbPlot.addMember(member,null,true);
            }
            DBConnection.update(dbPlot);
            addPlayerToAllRegions(uuidMember);
            if (to == null) {
                log(WorldAction.ADD, uuidMember.toString(), playerName);
            }
            p.sendMessage(Main.prefix
                    + MessageHandler.getInstance().getString(p, "plotMemberAdded").replace("%r", playerName));

            return MethodResult.SUCCESS;
        } else {
            p.sendMessage(
                    Main.prefix + MessageHandler.getInstance().getString(p, "alreadyMember").replace("%r", playerName));
        }
        return MethodResult.FAILURE;
    }

    @Override
    public void checkForTimeoutMembership() {
        LocalDateTime now = LocalDateTime.now();
        for (PlotMember member : dbPlot.getMembers()) {
            if (member.getAddedTo() != null && member.getAddedTo().isBefore(now)) {
                removeMember(member);
            }
        }
    }


    private void removeMember(PlotMember member) {
        dbPlot.removeMember(member.getMember());
        memberRemoved(member.getMember().getUuid().toString(), member.getMember().getName());
        DBConnection.update(dbPlot);
    }

    private void memberRemoved(String uuid, String name) {
        Player ownerPlayer = Bukkit.getPlayer(owner);
        Player memberPlayer = Bukkit.getPlayer(UUID.fromString(uuid));
        if (memberPlayer != null) {
            Main.send(memberPlayer, "plotMemberRemove_memberMsg", getName());
            if (WorldManager.get(memberPlayer.getWorld()) == this) {
                memberPlayer.performCommand("gs");
            }
        }
        if (ownerPlayer != null) {
            Main.send(ownerPlayer, "plotMemberRemoved", name);
        } else {
            /* Send new Mail */
            String message = MessageHandler.getInstance().getString(owner, "plotMemberRemoved", name);
            net.wargearworld.db.model.Player receiver = BauPlayer.getBauPlayer(owner).getDbPlayer();
            String sender = "plugin: BAU";
            DBConnection.sendMail(sender, receiver, message);
        }
        log(WorldAction.REMOVE, uuid, name);
    }

    @Override
    protected String getOwner() {
        return owner.toString();
    }


    @Override
    public long getId() {
        return dbPlot.getId();
    }
}
