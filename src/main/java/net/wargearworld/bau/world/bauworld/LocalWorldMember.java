package net.wargearworld.bau.world.bauworld;

import java.util.Objects;
import java.util.UUID;

public class LocalWorldMember {
    private String name;
    private UUID uuid;
    private boolean hasRights;

    public LocalWorldMember(String name, UUID uuid, boolean hasRights) {
        this.name = name;
        this.uuid = uuid;
        this.hasRights = hasRights;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean hasRights() {
        return hasRights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalWorldMember that = (LocalWorldMember) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid);
    }
}
