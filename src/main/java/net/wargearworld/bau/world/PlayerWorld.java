package net.wargearworld.bau.world;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.utils.MethodResult;
import net.wargearworld.bau.world.plot.PlotPattern;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.PlotMember;
import net.wargearworld.thedependencyplugin.DependencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class PlayerWorld extends BauWorld {

    UUID owner;
    private long plotID;

    public PlayerWorld(long id, UUID owner, World world) {
        super(world);
        this.plotID = id;
        EntityManager em = DependencyProvider.getEntityManager();
        Plot dbPlot = em.find(Plot.class, plotID);
        this.owner = owner;
        setTemplate(dbPlot.getTemplate().getName());
        plots = new HashMap<>();
        for (PlotPattern plotPattern : getTemplate().getPlots()) {
            plots.put(plotPattern.getID(), plotPattern.toPlot(this));
        }
        addPlayerToAllRegions(owner);
        em.close();
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
    public boolean isMember(UUID memberUUID) {
        EntityManager em = DependencyProvider.getEntityManager();
        Plot dbPlot = em.find(Plot.class, plotID);
        for (PlotMember member : dbPlot.getMembers()) {
            if (member.getMember().getUuid().equals(memberUUID)) {
                em.close();
                return true;
            }
        }
        em.close();
        return false;
    }

    @Override
    public Set<String> getMemberNames() {
        EntityManager em = DependencyProvider.getEntityManager();
        Set<String> out = new HashSet<>();
        Plot plot = em.find(Plot.class, plotID);
        for (PlotMember plotMember : plot.getMembers()) {
            out.add(plotMember.getMember().getName());
        }
        em.close();
        return out;
    }

    @Override
    public boolean isAuthorized(UUID uuid) {
        return owner.equals(uuid) || isMember(uuid);
    }

    public boolean hasRights(UUID uuid){
        if(owner.equals(uuid)){
            return true;
        }
        boolean rights = false;
        EntityManager em = DependencyProvider.getEntityManager();
        net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class,uuid);
        for(PlotMember plotMember : dbPlayer.getMemberedPlots()){
            if(plotMember.getPlot().getId() == plotID){
               rights = plotMember.hasRights();
               break;
            }
        }
        em.close();
        return rights;
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
    public void setTemplate(String templateName) {
        WorldTemplate template = WorldTemplate.getTemplate(templateName);
        super.setTemplate(template);
    }

    @Override
    public void removeAllMembersFromRegions() {
        EntityManager em = DependencyProvider.getEntityManager();
        Plot dbPlot = em.find(Plot.class, plotID);
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
        em.close();
    }

    @Override
    public void addAllMembersToRegions() {
        EntityManager em = DependencyProvider.getEntityManager();
        Plot dbPlot = em.find(Plot.class, plotID);
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
        em.close();
    }

    @Override
    public MethodResult add(String playerName, Date to) {

        BauPlayer p = BauPlayer.getBauPlayer(owner);
        UUID uuidMember = DBConnection.getUUID(playerName);
        if (!isAuthorized(uuidMember)) {
            EntityManager em = DependencyProvider.getEntityManager();
            Plot dbPlot = em.find(Plot.class, plotID);
            net.wargearworld.db.model.Player member = em.find(net.wargearworld.db.model.Player.class,uuidMember);
            PlotMember plotMember = new PlotMember();
            plotMember.setRights(true);
            plotMember.setPlot(dbPlot);
            plotMember.setMember(member);
            if (to != null) {
                plotMember.setAddedTo(new Timestamp(to.getTime()).toLocalDateTime());
            }
            dbPlot.addMember(plotMember);
            em.getTransaction().begin();
            em.merge(dbPlot);
            em.getTransaction().commit();
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
        Set<UUID> uuidsToRemove = new HashSet<>();
        EntityManager em = DependencyProvider.getEntityManager();
        Plot dbPlot = em.find(Plot.class, plotID);
        for (PlotMember member : dbPlot.getMembers()) {
            if (member.getAddedTo() != null && member.getAddedTo().isBefore(now)) {
                uuidsToRemove.add(member.getMember().getUuid());
            }
        }
        em.close();
        for(UUID uid:uuidsToRemove){
            removeMember(uid);
        }
    }

    @Override
    public void removeMember(UUID member) {
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if (member.toString().equals(this.owner)) {
            if (ownerPlayer != null)
                Main.send(ownerPlayer, "YouCantRemoveYourself");
        } else {
            BauPlayer memberBauPlayer = BauPlayer.getBauPlayer(member);
            EntityManager em = DependencyProvider.getEntityManager();
            Plot dbPlot = em.find(Plot.class, plotID);
            PlotMember plotMember = dbPlot.getMember(memberBauPlayer.getDbPlayer());
            dbPlot.removeMember(plotMember);
            em.getTransaction().begin();
            em.merge(dbPlot);
            em.getTransaction().commit();
            em.close();

            removeMemberFromAllRegions(plotMember.getMember().getUuid());
            memberRemoved(plotMember.getMember().getUuid());
        }
    }

    private void memberRemoved(UUID uuid) {
        Player memberPlayer = Bukkit.getPlayer(uuid);

        Player ownerPlayer = Bukkit.getPlayer(owner);
        String memberName;
        if (memberPlayer != null) {
            memberName = memberPlayer.getName();
            Main.send(memberPlayer, "plotMemberRemove_memberMsg", getName());
            if (WorldManager.get(memberPlayer.getWorld()) == this) {
                memberPlayer.performCommand("gs");
            }
        } else {
            memberName = DBConnection.getPlayer(uuid).getName();
        }
        if (ownerPlayer != null) {
            Main.send(ownerPlayer, "plotMemberRemoved", memberName);
        } else {
            /* Send new Mail */
            String message = MessageHandler.getInstance().getString(owner, "plotMemberRemoved", memberName);
            net.wargearworld.db.model.Player receiver = BauPlayer.getBauPlayer(owner).getDbPlayer();
            String sender = "plugin: BAU";
            DBConnection.sendMail(sender, receiver, message);
        }
        log(WorldAction.REMOVE, uuid.toString(), memberName);
    }

    @Override
    protected String getOwner() {
        return owner.toString();
    }


    @Override
    public long getId() {
        return plotID;
    }

    @Override
    public void leave(Player p) {
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if(ownerPlayer == null)
            return;
        MessageHandler.getInstance().send(ownerPlayer,"plot_leaved",p.getName());
    }

    @Override
    public void spawn(Player p){
        super.spawn(p);
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if(ownerPlayer == null)
            return;
        MessageHandler.getInstance().send(ownerPlayer,"plot_entered",p.getName());
    }

}
