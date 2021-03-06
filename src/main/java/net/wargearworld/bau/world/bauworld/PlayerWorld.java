package net.wargearworld.bau.world.bauworld;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.dao.DatabaseDAO;
import net.wargearworld.bau.dao.PlayerDAO;
import net.wargearworld.bau.dao.WorldDAO;
import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.MethodResult;
import net.wargearworld.bau.world.WorldManager;
import net.wargearworld.bau.world.LocalWorldTemplate;
import net.wargearworld.bau.world.gui.GUIPlayerWorld;
import net.wargearworld.bau.world.gui.IGUIWorld;
import net.wargearworld.bau.world.gui.WorldGUI;
import net.wargearworld.bau.world.plot.PlotPattern;
import net.wargearworld.db.EntityManagerExecuter;
import net.wargearworld.db.model.WorldMember;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class PlayerWorld extends BauWorld {

    UUID owner;
    private long plotID;
    private IGUIWorld iguiWorld;
    private Collection<LocalWorldMember> localWorldMembers;

    public PlayerWorld(long id, UUID owner, World world) {
        super(world);
        this.plotID = id;
        EntityManagerExecuter.run(em -> {
            net.wargearworld.db.model.World dbPlot = em.find(net.wargearworld.db.model.World.class, plotID);
            this.owner = owner;
            setTemplate(dbPlot.getTemplate().getName());
            plots = new HashMap<>();
            for (PlotPattern plotPattern : getTemplate().getPlots()) {
                plots.put(plotPattern.getID(), plotPattern.toPlot(this));
            }
        });
        addPlayerToAllRegions(owner);

    }

    @Override
    public void showInfo(Player p) {
        WorldGUI.openWorldInfo(p, this,false);
    }

    @Override
    public Collection<LocalWorldMember> getMembers() {
        if (localWorldMembers == null) {
            EntityManagerExecuter.run(em -> {
                localWorldMembers = new ArrayList<>();
                net.wargearworld.db.model.World dbWorld = em.find(net.wargearworld.db.model.World.class, plotID);
                for (WorldMember member : dbWorld.getMembers()) {
                    localWorldMembers.add(new LocalWorldMember(member.getMember().getName(), member.getMember().getUuid(),member.hasRights()));
                }
            });
        }
        return localWorldMembers;
    }

    @Override
    public boolean isMember(UUID memberUUID) {
        for(LocalWorldMember localWorldMember :getMembers()){
            if(localWorldMember.getUuid().equals(memberUUID))
                return true;
        }
        return false;
    }

    public Collection<String> getMemberNames() {
        return EntityManagerExecuter.run(em -> {
            Set<String> out = new HashSet<>();
            net.wargearworld.db.model.World dbWorld = em.find(net.wargearworld.db.model.World.class, plotID);
            for (WorldMember worldMember : dbWorld.getMembers()) {
                out.add(worldMember.getMember().getName());
            }
            return out;
        });
    }

    @Override
    public boolean isAuthorized(UUID uuid) {
        return owner.equals(uuid) || isMember(uuid);
    }

    @Override
    public boolean hasRights(UUID uuid) {
        if (owner.equals(uuid)) {
            return true;
        }
        Player p = Bukkit.getPlayer(uuid);
        if(p != null && p.hasPermission("bau.rights.bypass")) return true;
        return EntityManagerExecuter.run(em -> {
            net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class, uuid);
            for (WorldMember worldMember : dbPlayer.getMemberedWorlds()) {
                if (worldMember.getWorld().getId() == plotID) {
                    return worldMember.hasRights();
                }
            }

            return false;
        });

    }

    @Override
    public boolean isOwner(Player player) {
        return owner.equals(player.getUniqueId());
    }


    @Override
    public void removeAllMembersFromRegions() {
        EntityManagerExecuter.run(em -> {
            net.wargearworld.db.model.World dbWorld = em.find(net.wargearworld.db.model.World.class, plotID);
            for (WorldMember member : dbWorld.getMembers()) {
                super.removeMemberFromAllRegions(member.getMember().getUuid());
            }
        });

    }

    @Override
    public void addAllMembersToRegions() {
        EntityManagerExecuter.run(em -> {
            net.wargearworld.db.model.World dbWorld = em.find(net.wargearworld.db.model.World.class, plotID);
            for (WorldMember member : dbWorld.getMembers()) {
                super.addPlayerToAllRegions(member.getMember().getUuid());
            }
        });
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
    public MethodResult add(String playerName, Date to) {

        BauPlayer p = BauPlayer.getBauPlayer(owner);
        UUID uuidMember = DatabaseDAO.getUUID(playerName);
        if (!isAuthorized(uuidMember)) {
            EntityManagerExecuter.run(em -> {
                net.wargearworld.db.model.World dbWorld = em.find(net.wargearworld.db.model.World.class, plotID);
                net.wargearworld.db.model.Player member = em.find(net.wargearworld.db.model.Player.class, uuidMember);
                WorldMember plotMember = new WorldMember();
                plotMember.setRights(true);
                plotMember.setWorld(dbWorld);
                plotMember.setMember(member);
                if (to != null) {
                    plotMember.setAddedTo(new Timestamp(to.getTime()).toLocalDateTime());
                }
                dbWorld.addMember(plotMember);
                em.persist(plotMember);
            localWorldMembers.add(new LocalWorldMember(playerName,member.getUuid(),true));
            });
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
        EntityManagerExecuter.run(em -> {
            net.wargearworld.db.model.World dbWorld = em.find(net.wargearworld.db.model.World.class, plotID);
            for (WorldMember member : dbWorld.getMembers()) {
                if (member.getAddedTo() != null && member.getAddedTo().isBefore(now)) {
                    uuidsToRemove.add(member.getMember().getUuid());
                }
            }
            for (UUID uid : uuidsToRemove) {
                removeMember(uid);
            }
        });
    }

    @Override
    public void removeMember(UUID member) {
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if (member.toString().equals(this.owner)) {
            if (ownerPlayer != null)
                Main.send(ownerPlayer, "YouCantRemoveYourself");
        } else {
            BauPlayer memberBauPlayer = BauPlayer.getBauPlayer(member);
            EntityManagerExecuter.run(em -> {

                net.wargearworld.db.model.World dbWorld = em.find(net.wargearworld.db.model.World.class, plotID);
                WorldMember plotMember = dbWorld.getMember(memberBauPlayer.getDbPlayer());

                dbWorld.removeMember(plotMember);
                plotMember.getMember().removePlotMember(plotMember);
                try{
                em.remove(plotMember);

                }catch(Exception exception){
                    Main.send(ownerPlayer, "error");
                    return;
                }
                removeMemberFromAllRegions(member);

                memberRemoved(member);
            });

            for(LocalWorldMember localWorldMember : localWorldMembers){
                if(localWorldMember.getUuid().equals(member)){
                    localWorldMembers.remove(localWorldMember);
                    break;
                }
            }


        }
    }

    @Override
    public void setTemplate(LocalWorldTemplate template) {
        super.template = template;
        WorldDAO.update(this);
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
            memberName = DatabaseDAO.getPlayer(uuid).getName();
        }
        if (ownerPlayer != null) {
            Main.send(ownerPlayer, "plotMemberRemoved", memberName);
        } else {
            /* Send new Mail */
            String message = MessageHandler.getInstance().getString(owner, "plotMemberRemoved", memberName);
            net.wargearworld.db.model.Player receiver = BauPlayer.getBauPlayer(owner).getDbPlayer();
            String sender = "plugin: BAU";
            DatabaseDAO.sendMail(sender, receiver, message);
        }
        log(WorldAction.REMOVE, uuid.toString(), memberName);
    }

    @Override
    public String getOwner() {
        return owner.toString();
    }


    @Override
    public long getId() {
        return plotID;
    }

    @Override
    public void leave(Player p) {
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if (ownerPlayer == null || ownerPlayer == p)
            return;
        MessageHandler.getInstance().send(ownerPlayer, "plot_leaved", p.getName(), getName());
    }

    @Override
    public String rename(String newName) {
        super.setWorldName(newName);
        super.setName(newName);
        super.worldName = owner + "_" + newName;
        WorldDAO.update(this);
        return getOwner() + "_" + newName;
    }

    @Override
    public IGUIWorld getGUIWorld() {
        if (iguiWorld == null)
            iguiWorld = new GUIPlayerWorld(getName(), owner, DatabaseDAO.getPlayer(owner).getName(), WorldDAO.getWorldIcon(plotID));
        return iguiWorld;
    }

    @Override
    public void spawn(Player p) {
        super.spawn(p);
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if (ownerPlayer == null || ownerPlayer == p)
            return;
        MessageHandler.getInstance().send(ownerPlayer, "plot_entered", p.getName(), getName());
        MessageHandler.getInstance().send(p, "world_tp", getName(), PlayerDAO.getName(owner));
    }

}
