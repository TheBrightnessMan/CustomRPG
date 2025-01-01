package me.bright.conditions;

import me.bright.brightrpg.BrightStatModifier;
import me.bright.entity.BrightEntity;

public abstract class BrightCondition extends BrightStatModifier {

    private final String key, displayName;
    private boolean canStack = false,
            disableMelee = false,
            disableBow = false,
            disableMagic = false,
            disableMovement = false;

    public BrightCondition(String key, String displayName) {
        super(false);
        this.key = key;
        this.displayName = displayName;
    }

    public void onStart(BrightEntity affectedEntity) {
        // No op
    }

    public void tick(BrightEntity affectedEntity) {
        // No op
    }

    public void onEnd(BrightEntity affectedEntity) {
        // No op
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

    public boolean disablesMelee() {
        return disableMelee;
    }

    protected void setDisablesMelee(boolean disableMelee) {
        this.disableMelee = disableMelee;
    }

    public boolean disablesBow() {
        return disableBow;
    }

    protected void setDisablesBow(boolean disableBow) {
        this.disableBow = disableBow;
    }

    public boolean disablesMagic() {
        return disableMagic;
    }

    protected void setDisablesMagic(boolean disableMagic) {
        this.disableMagic = disableMagic;
    }

    public boolean disablesMovement() {
        return disableMovement;
    }

    protected void setDisablesMovement(boolean disableMovement) {
        this.disableMovement = disableMovement;
    }
}
