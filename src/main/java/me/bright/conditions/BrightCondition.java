package me.bright.conditions;

import me.bright.brightrpg.BrightStats;
import me.bright.entity.BrightEntity;

import java.util.HashMap;
import java.util.Map;

public abstract class BrightCondition {

    private final Map<BrightStats, Double> statsMod = new HashMap<>();
    private final String key, displayName;
    private boolean canStack = false;

    public BrightCondition(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public void setStatModifier(BrightStats stat, double modifier) {
        this.statsMod.put(stat, modifier);
    }

    public double getStatModifier(BrightStats stat) {
        return this.statsMod.getOrDefault(stat, 0D);
    }

    public Map<BrightStats, Double> getStatsMod() {
        return statsMod;
    }

    public void onStart(BrightEntity affectedEntity) {

    }

    public void tick(BrightEntity affectedEntity) {
        // No op
    }

    public void onEnd(BrightEntity affectedEntity) {

    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKey() {
        return key;
    }

    protected void setCanStack(boolean canStack) {
        this.canStack = canStack;
    }

    public boolean canStack() {
        return this.canStack;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrightCondition other)) return false;
        return this.getKey().equals(other.getKey());
    }
}
