package net.wargearworld.Bau.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.HikariCP.DataSource;
import net.wargearworld.Bau.utils.MethodResult;

public class TeamWorld extends BauWorld {

	private int teamID;
	private Set<UUID> leaders;
	private String teamName;

	public TeamWorld(int id, World world, int teamID) {
		super(id, world);
		this.teamID = teamID;
	}

	@Override
	protected Map<UUID, Date> loadMembers() {
		leaders = new HashSet<>();
		Map<UUID, Date> out = new HashMap<>();
		try (Connection conn = DataSource.getConnection()){
			String sql = "SELECT * FROM `Team_has_Player`,Team WHERE Team.ID = TeamID AND TeamID = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, teamID);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				teamName = rs.getString("name");
				UUID playerUUID = UUID.fromString(rs.getString("PlayerUUID"));
				if(rs.getString("task").equals("Leader")) {
					leaders.add(playerUUID);
				}else {
					out.put(playerUUID,null);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}

	@Override
	public boolean isAuthorized(UUID uuid) {
		return leaders.contains(uuid) || super.getMembers().containsKey(uuid);
	}

	@Override
	public void showInfo(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOwner(Player player) {
		return leaders.contains(player.getUniqueId());
	}

	@Override
	public void addTemp(String playerName, int time) {
	}

	@Override
	public MethodResult add(String playerName, Date to) {
		return null;
	}

	@Override
	public void removeMember(UUID member) {
		super.getMembers().remove(member);
		leaders.remove(member);
		super.addPlayerToAllRegions(member);
	}

	@Override
	protected String getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addMember(UUID memberUUID) {
		super.getMembers().put(memberUUID, null);
		super.addPlayerToAllRegions(memberUUID);
	}

	public void setLeader(UUID memberUUID) {
		leaders.add(memberUUID);
		super.getMembers().remove(memberUUID);
	}

	public void addLeader(UUID memberUUID) {
		leaders.add(memberUUID);
		super.addPlayerToAllRegions(memberUUID);
	}

	public void setMember(UUID memberUUID) {
		leaders.remove(memberUUID);
		super.getMembers().put(memberUUID, null);
	}

	public String getTeamName() {
		return teamName;
	}
	
	public int getTeamID() {
		return teamID;
	}
}
