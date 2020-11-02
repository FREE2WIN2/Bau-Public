package net.wargearworld.bau.dao.constructors;

import java.util.UUID;

public class WorldEntry {
    private UUID uuid;
    private String name;

    public WorldEntry(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getName() {
        return uuid.toString() + "_"
                + name;
    }
}
