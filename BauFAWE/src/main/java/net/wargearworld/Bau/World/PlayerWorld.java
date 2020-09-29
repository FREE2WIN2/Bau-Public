package net.wargearworld.Bau.World;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.Main;
import net.wargearworld.Bau.MessageHandler;
import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.Bau.Player.BauPlayer;
import net.wargearworld.Bau.utils.ClickAction;
import net.wargearworld.Bau.utils.JsonCreater;
import net.wargearworld.Bau.utils.MethodResult;

public class PlayerWorld extends BauWorld {

	UUID owner;

	public PlayerWorld(int id, String owner, World world) {
		super(id, world);
		this.owner = UUID.fromString(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Map<UUID, Date> loadMembers() {
		return DBConnection.getMembers(getId());
	}

	@Override
	public void showInfo(Player p) {
		boolean isOwner = isOwner(p);
		Set<String> memberlist = DBConnection.getMember(p.getUniqueId().toString());
		Main.send(p, "memberListHeader", getName(p.getWorld()));
		for (String memberUUID : memberlist) {
			String memberName = DBConnection.getName(memberUUID);
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
		Player ownerPlayer = Bukkit.getPlayer(owner);
		if (!member.toString().equals(this.owner)) {
			if (DBConnection.removeMember(super.getId(), member)) {
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
		}
	}

	@Override
	public MethodResult add(String playerName, Date to) {

		BauPlayer p = BauPlayer.getBauPlayer(owner);
		UUID uuidMember = UUID.fromString(DBConnection.getUUID(playerName));
		if (!isAuthorized(uuidMember)) {
			getMembers().put(uuidMember, to);
			if (DBConnection.addMember(getId(), uuidMember, to)) {
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
			return MethodResult.FAILURE;
		}
	}

	@Override
	protected String getOwner() {
		return owner.toString();
	}

}
