package net.wargearworld.Bau.Communication;

import java.util.List;
import java.util.UUID;

import net.wargearworld.Bau.World.TeamWorld;
import net.wargearworld.Bau.World.WorldManager;

public class PluginMessage {
	int ID;
	String from;
	String subChannel;
	String command;

	public PluginMessage(int id, String from, String subChannel, String command) {
		this.ID = id;
		this.from = from;
		this.subChannel = subChannel;
		this.command = command;
	}

	public void operate() {
		// from: Team
		// subChannel: Members
		// command: add <SpielerName> <TeamID>
		// subChannel: Members
		// command: Spielername
		if (from.equals("TeamSystem")) {
			String[] args = command.split(" ");
			int teamID = Integer.parseInt(args[2]);
			UUID memberUUID = UUID.fromString(args[1]);
			List<TeamWorld> worlds = WorldManager.getTeamWorlds(teamID);
			switch (subChannel) {
			case "Members":
				if (args[0].equals("addMember")) {
					for (TeamWorld world : worlds) {
						world.addMember(memberUUID);
					}
				} else if (args[0].equals("removeMember")) {
					for (TeamWorld world : worlds) {
						world.removeMember(memberUUID);
					}
				}else if (args[0].equals("setLeader")) {
					for (TeamWorld world : worlds) {
						world.setLeader(memberUUID);
					}
				}
				break;
			case "Leaders":
				if (args[0].equals("addLeader")) {
					for (TeamWorld world : worlds) {
						world.addLeader(memberUUID);
					}
				} else if (args[0].equals("removeLeader")) {
					for (TeamWorld world : worlds) {
						world.removeMember(memberUUID);
					}
				}else if (args[0].equals("setMember")) {
					for (TeamWorld world : worlds) {
						world.setMember(memberUUID);
					}
				}
				break;
			}
			
		}
		DatabaseCommunication.deleteMessage(ID);
	}
}
