package net.wargearworld.bau.advancement;

import net.wargearworld.Achievments.Advancement.Advancement;
import net.wargearworld.Achievments.AdvancementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class AdvTemplate implements Listener {
    Advancement advancement;

    public AdvTemplate(String name) {
        advancement = AdvancementManager.getAdvancement("bau", name);
    }

    public Advancement getAdvancement() {
        return advancement;
    }

    protected void grantSingleCriterion(Player p) {
        grantCriterion(advancement.getCriteriaNames()[0], p);
    }

    void incSingleCriterion(long by, Player p) {
        incCriterion(advancement.getCriteriaNames()[0], by, p);
    }

    void grantCriterion(String criterionName, Player p) {

        advancement.awardCriterion(p, criterionName);
    }

    void incCriterion(String criterionName, long by, Player p) {
        advancement.incCriterionProgress(p, criterionName,by);
    }


}