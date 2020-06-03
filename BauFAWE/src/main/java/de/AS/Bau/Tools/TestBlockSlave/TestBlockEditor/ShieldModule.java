package de.AS.Bau.Tools.TestBlockSlave.TestBlockEditor;

import org.bukkit.World;

public class ShieldModule {

	ShieldType type;
	int tier;
	ShieldPosition position;

	public ShieldModule(ShieldType type, int tier, ShieldPosition position) {
		this.type = type;
		this.tier = tier;
		this.position = position;
	}

	public ShieldPosition getPosition() {
		return position;
	}

	public ShieldType getType() {
		return type;
	}
	
	public void visualize(String plotID, World world) {
		
	}
	
}
