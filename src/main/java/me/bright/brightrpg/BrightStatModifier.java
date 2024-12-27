package me.bright.brightrpg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BrightStatModifier {

    private final Map<BrightStats, Double>
            flatMod = new ConcurrentHashMap<>(),
            addMod = new ConcurrentHashMap<>(),
            mulMod = new ConcurrentHashMap<>();

    public BrightStatModifier(boolean isEntity) {
        if (isEntity) initMapEntity();
        else initMapOther();
    }

    private void initMapEntity() {
        for (BrightStats stat : BrightStats.values()) {
            flatMod.put(stat, stat.entityBaseValue);
            addMod.put(stat, 100D);
            mulMod.put(stat, 100D);
        }
    }

    private void initMapOther() {
        for (BrightStats stat : BrightStats.values()) {
            flatMod.put(stat, stat.otherBaseValue);
            addMod.put(stat, 0D);
            mulMod.put(stat, 0D);
        }
    }

    public Map<BrightStats, Double> getAllFlatMod() {
        return flatMod;
    }

    public Map<BrightStats, Double> getAllAddMod() {
        return addMod;
    }

    public Map<BrightStats, Double> getAllMulMod() {
        return mulMod;
    }

    public double getStatFlatMod(BrightStats stat) {
        return flatMod.get(stat);
    }

    public double getStatAddMod(BrightStats stat) {
        return addMod.get(stat);
    }

    public double getStatMulMod(BrightStats stat) {
        return mulMod.get(stat);
    }

    public void setStatFlatMod(BrightStats stat, double val) {
        flatMod.put(stat, val);
    }

    public void setStatAddMod(BrightStats stat, double val) {
        addMod.put(stat, val);
    }

    public void setStatMulMod(BrightStats stat, double val) {
        mulMod.put(stat, val);
    }
}
