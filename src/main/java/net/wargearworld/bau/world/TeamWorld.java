package net.wargearworld.bau.world;

import net.wargearworld.bau.utils.MethodResult;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class TeamWorld extends BauWorld{

	private int teamID;
	private Set<UUID> leaders;
	private Set<UUID> members;
	public TeamWorld(int id, World world, int teamID) {
		super(world);
		this.teamID = teamID;
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
	public void checkForTimeoutMembership() {

	}

	@Override
	public void removeMember(UUID member) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTemplate(String templateName) {

	}

	@Override
	public void removeAllMembersFromRegions() {

	}

	@Override
	public void addAllMembersToRegions() {

	}

	@Override
	public long getId() {
		return teamID;
	}

	@Override
	public void leave(Player p) {

	}

	@Override
	protected String getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMember(UUID member) {
		return false;
	}

	@Override
	public Set<String> getMemberNames() {
		return null;
	}

}
