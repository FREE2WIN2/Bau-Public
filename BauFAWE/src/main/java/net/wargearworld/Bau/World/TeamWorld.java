package net.wargearworld.Bau.World;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;

import net.wargearworld.Bau.utils.MethodResult;

public class TeamWorld extends BauWorld{

	private int teamID;
	private Set<UUID> leaders;
	private Set<UUID> members;
	public TeamWorld(int id, World world, int teamID) {
		super(world);
		this.teamID = teamID;
	}

	@Override
	protected Map<UUID, Date> loadMembers() {
		Map<UUID,Date> out = new HashMap<>();
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAuthorized(UUID uuid) {
		return leaders.contains(uuid) || members.contains(uuid);
	}

	@Override
	public void showInfo(Player p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOwner(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addTemp(String playerName, int time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MethodResult add(String playerName, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeMember(UUID member) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTemplate(String templateName) {

	}

	@Override
	protected String getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

}
