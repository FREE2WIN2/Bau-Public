package net.wargearworld.bau.communication;

import java.util.UUID;

import net.wargearworld.bau.team.Team;
import net.wargearworld.bau.team.TeamManager;
import net.wargearworld.bau.utils.HelperMethods;
import net.wargearworld.db.model.PluginCommunication;

public class PluginMessage {
	Long ID;
	String from;
	String subChannel;
	String command;

	public PluginMessage(PluginCommunication communication) {
		this.ID = communication.getId();
		this.from = communication.getSender();
		this.subChannel = communication.getSubChannel();
		this.command = communication.getCommand();
	}

	public void operate() {
		if (from.equals("WGTeam")) {
			String[] args = command.split(" ");
			switch (subChannel) {
			case "Update":
				if(HelperMethods.isInt(args[0])) {
					Long teamID = Long.parseLong(args[0]);
					if (TeamManager.containsTeam(teamID)) {
						Team team = TeamManager.getTeam(teamID);
						team.update();
					}
				}
				break;
			}
			
		}
		DatabaseCommunication.deleteMessage(ID);
	}
}
