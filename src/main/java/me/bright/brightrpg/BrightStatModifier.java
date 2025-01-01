package me.bright.brightrpg;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrightStatModifier {

    private final Map<BrightStat, Double>
            flatMod = new ConcurrentHashMap<>(),
            addMod = new ConcurrentHashMap<>(),
            mulMod = new ConcurrentHashMap<>();

    public BrightStatModifier(boolean isEntity) {
        if (isEntity) initMapEntity();
        else initMapOther();
    }

    private void initMapEntity() {
        for (BrightStat stat : BrightStat.values()) {
            flatMod.put(stat, stat.entityBaseValue);
            addMod.put(stat, 100D);
            mulMod.put(stat, 100D);
        }
    }

    private void initMapOther() {
        for (BrightStat stat : BrightStat.values()) {
            flatMod.put(stat, 0D);
            addMod.put(stat, 0D);
            mulMod.put(stat, 0D);
        }
    }

    public Map<BrightStat, Double> getAllFlatMod() {
        return flatMod;
    }

    public Map<BrightStat, Double> getAllAddMod() {
        return addMod;
    }

    public Map<BrightStat, Double> getAllMulMod() {
        return mulMod;
    }

    public double getStatFlatMod(BrightStat stat) {
        return flatMod.get(stat);
    }

    public double getStatAddMod(BrightStat stat) {
        return addMod.get(stat);
    }

    public double getStatMulMod(BrightStat stat) {
        return mulMod.get(stat);
    }

    public void setStatFlatMod(BrightStat stat, double val) {
        flatMod.put(stat, val);
    }

    public void setStatAddMod(BrightStat stat, double val) {
        addMod.put(stat, Math.max(-100D, val));
    }

    public void setStatMulMod(BrightStat stat, double val) {
        mulMod.put(stat, Math.max(-100D, val));
    }

}
