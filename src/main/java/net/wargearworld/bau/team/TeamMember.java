package net.wargearworld.bau.team;

import java.util.UUID;

public class TeamMember {
    private Long id;
    private UUID uuid;
    private String task;
    private boolean leader;
    private boolean newcomer;

    public TeamMember(Long id, UUID uuid, String task, boolean leader, boolean newcomer) {
        this.id = id;
        this.uuid = uuid;
        this.task = task;
        this.leader = leader;
        this.newcomer = newcomer;
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTask() {
        return task;
    }

    public boolean isLeader() {
        return leader;
    }

    public boolean isNewcomer() {
        return newcomer;
    }

    public void update(){

    }
}
