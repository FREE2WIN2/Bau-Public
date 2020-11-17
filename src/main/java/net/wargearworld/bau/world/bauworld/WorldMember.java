package net.wargearworld.bau.world.bauworld;

import java.util.Objects;
import java.util.UUID;

public class WorldMember {
    private String name;
    private UUID uuid;

    public WorldMember(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldMember that = (WorldMember) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid);
    }
}
