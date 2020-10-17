package net.wargearworld.bau.world;

import java.time.LocalDateTime;
import java.util.*;

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
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Map<UUID, Date> loadMembers() {
		return null;
//		return DBConnection.getMembers(getId());
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
	public boolean isAuthorized(UUID uuid) {
		return owner.equals(uuid) || getMembers().keySet().contains(uuid);
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
		/*Player ownerPlayer = Bukkit.getPlayer(owner);
		if (!member.toString().equals(this.owner)) {
			if (DBConnection.removeMember(dbPlot.getId(), member)) {
				removeMemberFromAllRegions(member);
				String name = DBConnection.getName(member.toString());
				super.getMembers().remove(member);
				log(WorldAction.REMOVE, member.toString(), name);
				Player memberPlayer = Bukkit.getPlayer(member);
				if (memberPlayer != null) {
					Main.send(memberPlayer, "plotMemberRemove_memberMsg", getName());
					if (WorldManager.get(memberPlayer.getWorld()) == this) {
						memberPlayer.performCommand("gs");
					}
				}
				if (ownerPlayer != null) {
					Main.send(ownerPlayer, "plotMemberRemoved", name);
				} else {
					DBConnection.addMail("plugin: BAU", owner.toString(),
							MessageHandler.getInstance().getString(owner, "plotMemberRemoved").replace("%r", name));
				}
			} else {
				if (ownerPlayer != null) {
					Main.send(ownerPlayer, "error");
				}
			}
		} else {
			if (ownerPlayer != null)
				Main.send(ownerPlayer, "YouCantRemoveYourself");
		}*/
	}

	@Override
	public void setTemplate(String templateName) {
		WorldTemplate template = WorldTemplate.getTemplate(templateName);
		super.setTemplate(template);
	}

	@Override
	public MethodResult add(String playerName, Date to) {

		/*BauPlayer p = BauPlayer.getBauPlayer(owner);
		UUID uuidMember = UUID.fromString(DBConnection.getUUID(playerName));
		if (!isAuthorized(uuidMember)) {
			getMembers().put(uuidMember, to);
			if (DBConnection.addMember(dbPlot.getId(), uuidMember, to)) {
				addPlayerToAllRegions(uuidMember);
				if (to == null) {
					log(WorldAction.ADD, uuidMember.toString(), playerName);
				}
				p.sendMessage(Main.prefix
						+ MessageHandler.getInstance().getString(p, "plotMemberAdded").replace("%r", playerName));

				return MethodResult.SUCCESS;
			} else {
				p.sendMessage(Main.prefix + MessageHandler.getInstance().getString(p, "error"));
				return MethodResult.ERROR;
			}
		} else {
			p.sendMessage(
					Main.prefix + MessageHandler.getInstance().getString(p, "alreadyMember").replace("%r", playerName));
		}*/
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


	private void removeMember(PlotMember member){
		dbPlot.getMembers().remove(member);
		DBConnection.update(dbPlot);
		memberRemoved(member.getMember().getUuid().toString(),member.getMember().getName());
	}

	private void memberRemoved(String uuid, String name){
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
			String message = MessageHandler.getInstance().getString(owner, "plotMemberRemoved",name);
			net.wargearworld.db.model.Player receiver = BauPlayer.getBauPlayer(owner).getDbPlayer();
			String sender = "plugin: BAU";
			DBConnection.sendMail(sender,receiver,message);
		}
		log(WorldAction.REMOVE, uuid, name);
	}

	@Override
	protected String getOwner() {
		return owner.toString();
	}

}
