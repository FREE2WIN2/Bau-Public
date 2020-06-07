package de.AS.Bau.Plots;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.AS.Bau.Main;
import de.AS.Bau.StringGetterBau;
import de.AS.Bau.HikariCP.DBConnection;
import de.AS.Bau.WorldEdit.WorldGuardHandler;

public class UsersPlot {

	UUID ownerUUID;
	String template;
	Set<String> member;

	public UsersPlot(UUID ownerUUID, String template, Set<String> member) {
		this.ownerUUID  = ownerUUID;
		this.template = template;
		this.member = member;
	}
	

	public boolean addMember(String uuidMember) {
		if(member.add(uuidMember)) {
			if( DBConnection.addMember(ownerUUID, UUID.fromString(uuidMember))) {
				WorldGuardHandler.addPlayerToAllRegions(ownerUUID.toString(), uuidMember);
				return true;
			}
		}
		return false;
		
	}
	
	public boolean removeMember(String uuidMember,String playerName) {
		if(member.remove(uuidMember)) {	
			if(DBConnection.removeMember(ownerUUID, UUID.fromString(uuidMember))) {
				WorldGuardHandler.removeMemberFromAllRegions(ownerUUID.toString(),UUID.fromString(uuidMember));
				if(ownerIsOn()) {
					Main.send(getOwner(), "plotMemberRemoved", playerName);
				}else {
					DBConnection.addMail("plugin: BAU", ownerUUID.toString(), StringGetterBau
							.getString(ownerUUID, "plotMemberRemoved").replace("%r", playerName));
				}
				return true;
			}
		}
		return false;
	}
	public boolean isMember(UUID uuid) {
		return member.contains(uuid.toString());
	}

	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public boolean ownerIsOn() {
		return Bukkit.getPlayer(ownerUUID) !=null;
	}
	
	private Player getOwner() {
		if(ownerIsOn()) {
			return Bukkit.getPlayer(ownerUUID);
		}
		return null;
	}
	
	public Set<String> getMember() {
		return member;
	}
}
