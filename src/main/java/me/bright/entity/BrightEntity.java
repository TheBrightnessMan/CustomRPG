package me.bright.entity;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStatModifier;
import me.bright.brightrpg.BrightStat;
import me.bright.conditions.BrightCondition;
import me.bright.damage.BrightDamage;
import me.bright.damage.DamageType;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightSpell;
import me.bright.utils.BrightNumberUtils;
import me.bright.utils.BrightStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BrightEntity extends BrightStatModifier {

    private static final String KEY_ATTRIBUTE = "KEY", DEFAULT_KEY = "null";

    private final BrightRPG plugin;
    private final LivingEntity livingEntity;
    private final String name;
    private final Map<BrightStat, Double> statsCache = new HashMap<>();
    private Map<BrightCondition, Long> conditions = new HashMap<>();
    private boolean meleeDisabled = false,
            bowDisabled = false,
            magicDisabled = false,
            movementDisabled = false;
    private BrightArmorStand frozenArmorStand = null;

    public BrightEntity(@NotNull LivingEntity livingEntity) {
        super(true);
        this.livingEntity = livingEntity;
        this.plugin = BrightRPG.getPlugin();
        String nbtName = livingEntity.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "NAME"),
                PersistentDataType.STRING, "null");
        this.name = (nbtName.equals("null")) ?
                BrightStringUtils.toPrettyString(livingEntity.getType().toString()) :
                nbtName;
        updateStatsCache();
        var attribute = livingEntity.getAttribute(Attribute.GENERIC_MAX_ABSORPTION);
        if (attribute == null) return;
        attribute.setBaseValue(Integer.MAX_VALUE);
    }

    @Contract("null -> null")
    public static BrightEntity fromLivingEntity(LivingEntity entity) {
        if (entity == null) return null;
        PersistentDataContainer nbt = entity.getPersistentDataContainer();

        // If is in list, return it
        for (BrightEntity entityInList : BrightRPG.getEntities()) {
            if (entity.getUniqueId().equals(entityInList.getLivingEntity().getUniqueId())) {
                return entityInList;
            }
        }

        // Not in list, must be new
        String givenKey = nbt.getOrDefault(new NamespacedKey(BrightRPG.getPlugin(), KEY_ATTRIBUTE),
                PersistentDataType.STRING, DEFAULT_KEY);
        return (givenKey.equals(DEFAULT_KEY)) ?
                BrightEntityList.getVanillaEntity(entity) :
                BrightEntityList.getCustomEntity(givenKey);
    }

    public double getStatFromNbt(@NotNull BrightStat stat) {
        switch (stat) {
            case CURRENT_HP -> {
                double valueFromNbt = livingEntity.getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, stat.key),
                                PersistentDataType.DOUBLE, statsCache.get(BrightStat.MAX_HP));
                return Math.max(0, Math.min(valueFromNbt, statsCache.get(BrightStat.MAX_HP)));
            }
            case LEVEL -> {
                return livingEntity.getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, stat.key),
                                PersistentDataType.DOUBLE, 1.0);
            }
            default -> {
                double valueFromNbt = livingEntity.getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, stat.key),
                                PersistentDataType.DOUBLE, stat.entityBaseValue);
                return Math.max(0, valueFromNbt);
            }
        }
    }

    protected double setStatToNbt(@NotNull BrightStat stat, double val) {
        double clamped = Math.max(0, val);
        livingEntity.getPersistentDataContainer()
                .set(new NamespacedKey(plugin, stat.key),
                        PersistentDataType.DOUBLE, clamped);
        return clamped;
    }

    public double getStatFromCache(@NotNull BrightStat stat) {
        return Math.max(0, statsCache.getOrDefault(stat, stat.entityBaseValue));
    }

    private void setStatToCache(@NotNull BrightStat stat, double value) {
        statsCache.put(stat, Math.max(0, value));
    }

    public @Nullable BrightItem getItemInMainHand() {
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return null;
        return BrightItem.fromItemStack(equipment.getItemInMainHand());
    }

    public @NotNull BrightDamage[] meleeHit(@NotNull BrightEntity target) {
        // Calculate base damage
        double weaponDamageAmount = getStatFromCache(BrightStat.DAMAGE) * (getStatFromCache(BrightStat.STRENGTH) / 100);

        // Roll for crit
        boolean crit = BrightDamage.rollCrit(getStatFromCache(BrightStat.CRIT_CHANCE));
        if (crit) {
            weaponDamageAmount *= (getStatFromCache(BrightStat.CRIT_DMG) / 100);
        }
        if (getLivingEntity() instanceof Player player) {
            weaponDamageAmount *= Math.pow(player.getAttackCooldown(), 6);
        }

        // Register the base damage
        List<BrightDamage> damageRegistry = new ArrayList<>();
        damageRegistry.add(new BrightDamage(DamageType.PHYSICAL, weaponDamageAmount, this, crit));

        // Apply armor on hit effects
        EntityEquipment equipment = this.getLivingEntity().getEquipment();
        if (equipment != null) {
            for (ItemStack armorContent : equipment.getArmorContents()) {
                if (armorContent == null) continue;
                BrightItem armor = BrightItem.fromItemStack(armorContent);
                if (armor == null) continue;
                damageRegistry = armor.onMeleeHit(this, target, damageRegistry);
            }
        }

        // Apply weapon on hit effects
        BrightItem weapon = getItemInMainHand();
        damageRegistry = (weapon == null) ?
                damageRegistry : weapon.onMeleeHit(this, target, damageRegistry);

        // Finalise and apply the damage
        return target.applyDamages(damageRegistry);
    }

    public @NotNull BrightDamage[] spellHit(@NotNull BrightEntity target, @NotNull BrightSpell spell) {
        BrightItem weapon = getItemInMainHand();

        // Calculate base damage
        double entitySpellDamage = this.getStatFromCache(BrightStat.SPELL_DMG);
        double entityIntelligence = this.getStatFromCache(BrightStat.INTELLIGENCE);
        double damageAmount = spell.getBaseDamage()
                * (1 + entityIntelligence / 100 * spell.getPower() / 100)
                * (entitySpellDamage / 100);

        // Register the base damage
        List<BrightDamage> damageRegistry = new ArrayList<>();
        damageRegistry.add(new BrightDamage(DamageType.MAGIC, damageAmount, this, false));

        // Apply armor on hit effects
        EntityEquipment equipment = this.getLivingEntity().getEquipment();
        if (equipment != null) {
            for (ItemStack armorContent : equipment.getArmorContents()) {
                if (armorContent == null) continue;
                BrightItem armor = BrightItem.fromItemStack(armorContent);
                if (armor == null) continue;
                damageRegistry = armor.onSpellHit(this, target, damageRegistry);
            }
        }

        // Apply weapon on hit effects
        damageRegistry = (weapon == null) ?
                damageRegistry : weapon.onSpellHit(this, target, damageRegistry);

        // Finalise and apply the damage
        return target.applyDamages(damageRegistry);
    }

    public @NotNull BrightDamage[] applyDamages(@NotNull List<BrightDamage> damages) {
        if (damages.isEmpty()) return BrightDamage.emptyFinalised;
        BrightEntity attacker = damages.getFirst().dealer();
        if (attacker == null) return BrightDamage.emptyFinalised; // How did this happen?

        double currentHp = getCurrentHp(),
                maxHp = getStatFromCache(BrightStat.MAX_HP),
                tempHp = getTempHp(),
                damageTaken = getStatFromCache(BrightStat.DAMAGE_TAKEN);
        if (currentHp <= 0 || maxHp <= 0) {
            // How did this happen?
            this.getLivingEntity().setHealth(0);
            return BrightDamage.emptyFinalised;
        }

        // Calculate normal and health-based damage
        //   and then apply resistances and penetration
        List<BrightDamage> finalDamages = new ArrayList<>();
        for (BrightDamage damage : damages) {
            BrightDamage flatDamage = BrightDamage.calculateFlatDamage(damage, this);
            BrightDamage resistedDamage = BrightDamage.calculateResistedDamage(flatDamage, this);
            finalDamages.add(
                    new BrightDamage(resistedDamage.type(),
                            resistedDamage.amount() * damageTaken / 100,
                            resistedDamage.dealer(), resistedDamage.critical()));
        }

        // Apply on hurt effects
        List<BrightDamage> mergedDamages = new ArrayList<>(
                Arrays.asList(BrightDamage.mergeDamages(finalDamages))
        );
        EntityEquipment equipment = this.getLivingEntity().getEquipment();
        if (equipment != null) {
            for (ItemStack armorContent : equipment.getArmorContents()) {
                BrightItem brightItem = BrightItem.fromItemStack(armorContent);
                if (brightItem == null) continue;
                mergedDamages = brightItem.onHurt(attacker, this, mergedDamages);
            }
        }
        BrightItem mainHand = this.getItemInMainHand();
        mergedDamages = (mainHand == null) ?
                mergedDamages : mainHand.onHurt(attacker, this, mergedDamages);

        // Merge everything together
        BrightDamage[] finalMergedDamages = BrightDamage.mergeDamages(mergedDamages);
        double totalDamage = Math.max(
                finalMergedDamages[0].amount() + finalMergedDamages[1].amount() + finalMergedDamages[2].amount(), 0
        );

        // Actually apply the damage
        if (tempHp >= totalDamage) {
            setTempHp(tempHp - totalDamage);
        } else {
            setTempHp(0);
            setCurrentHp(currentHp + tempHp - totalDamage);
        }
        update();
        return finalMergedDamages;
    }

    public void applyCondition(@NotNull BrightCondition condition, long durationTicks, boolean override) {
        condition.onStart(this);
        if (override) {
            conditions.put(condition, durationTicks);
            return;
        }
        long existingDuration = conditions.getOrDefault(condition, 0L);
        if (condition.canStack()) {
            conditions.put(condition, existingDuration + durationTicks);
            return;
        }
        if (existingDuration == 0) conditions.put(condition, durationTicks);
    }

    public double getTempHp() {
        return this.getStatFromNbt(BrightStat.TEMP_HP);
    }

    public void setTempHp(double newTempHp) {
        double maxTempHp = 10 * this.getStatFromCache(BrightStat.MAX_HP),
                actual = Math.min(newTempHp, maxTempHp);
        setStatToNbt(BrightStat.TEMP_HP, actual);
    }

    public double getCurrentHp() {
        return getStatFromNbt(BrightStat.CURRENT_HP);
    }

    public void setCurrentHp(double newHp) {
        double maxHp = getStatFromCache(BrightStat.MAX_HP),
                actual = Math.min(newHp, maxHp);
        setStatToNbt(BrightStat.CURRENT_HP, actual);
    }

    public double getCurrentMana() {
        return getStatFromNbt(BrightStat.CURRENT_MANA);
    }

    public void setCurrentMana(double newMana) {
        double maxHp = this.getStatFromCache(BrightStat.INTELLIGENCE),
                actual = Math.min(newMana, maxHp);
        setStatToNbt(BrightStat.CURRENT_MANA, actual);
    }

    public boolean update() {
        updateStatsCache();

        double currentHp = getCurrentHp(),
                maxHp = getStatFromCache(BrightStat.MAX_HP),
                tempHp = getTempHp();
        if (currentHp <= 0 || livingEntity.isDead()) {
            livingEntity.setHealth(0);
            return false;
        }

        updateDisplayHealthBar(tempHp, currentHp, maxHp);
        updateNameTag();
        return true;
    }

    private void updateStatsCache() {
        for (BrightStat stat : BrightStat.values()) {
            double result = calculateStat(stat);
            setStatToCache(stat, result);
        }
    }

    private double calculateStat(BrightStat stat) {
        double flat = getStatFlatMod(stat),
                add = getStatAddMod(stat),
                mul = getStatMulMod(stat) / 100;

        EntityEquipment equipment = getLivingEntity().getEquipment();
        if (equipment != null) {
            for (ItemStack armor : equipment.getArmorContents()) {
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                flat += brightArmor.getStatFlatMod(stat);
                add += brightArmor.getStatAddMod(stat);
                mul *= (1 + brightArmor.getStatMulMod(stat) / 100);
            }
        }

        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) {
            flat += mainHand.getStatFlatMod(stat);
            add += mainHand.getStatAddMod(stat);
            mul *= (1 + mainHand.getStatMulMod(stat) / 100);
        }

        for (BrightCondition condition : getConditions().keySet()) {
            flat += condition.getStatFlatMod(stat);
            add += condition.getStatAddMod(stat);
            mul *= (1 + condition.getStatMulMod(stat) / 100);
        }

        return flat * add / 100 * mul;
    }

    private void updateNameTag() {
        long currentHp = (long) getCurrentHp(),
                maxHp = (long) getStatFromCache(BrightStat.MAX_HP),
                tempHp = (long) getTempHp(),
                level = (long) getStatFromCache(BrightStat.LEVEL);

        StringBuilder customName = new StringBuilder(ChatColor.GRAY + "[Level ").append(level).append("] ")
                .append(name).append(" ");

        if (tempHp > 0)
            customName.append(BrightStat.TEMP_HP.color).append(tempHp + currentHp);
        else
            customName.append(BrightStat.CURRENT_HP.color).append(currentHp);

        customName.append(ChatColor.GRAY).append("/")
                .append(BrightStat.MAX_HP.color).append(maxHp).
                append(ChatColor.RED).append("♥");

        livingEntity.setCustomNameVisible(true);
        livingEntity.setCustomName(customName.toString());
    }

    public void tickConditions() {
        meleeDisabled = false;
        bowDisabled = false;
        magicDisabled = false;
        movementDisabled = false;

        Map<BrightCondition, Long> conditionsCache = new HashMap<>();
        for (var entry : conditions.entrySet()) {
            BrightCondition condition = entry.getKey();
            condition.tick(this);

            meleeDisabled |= condition.disablesMelee();
            bowDisabled |= condition.disablesBow();
            magicDisabled |= condition.disablesMagic();
            movementDisabled |= condition.disablesMovement();

            long duration = entry.getValue();
            if (duration == -1) {
                conditionsCache.put(condition, duration);
                continue;
            }

            duration -= plugin.updateTickRate;
            if (duration <= 0) {
                condition.onEnd(this);
            } else {
                conditionsCache.put(condition, duration);
            }
        }
        conditions = conditionsCache;

        Location location = getLivingEntity().getLocation().clone();
        if (movementDisabled) {
            if (frozenArmorStand == null) {
                frozenArmorStand = new BrightArmorStand(location.subtract(0, 1, 0));
                frozenArmorStand.getBukkitArmorStand().addPassenger(getLivingEntity());
            }
        } else {
            if (frozenArmorStand != null) {
                frozenArmorStand.getBukkitEntity().remove();
                frozenArmorStand = null;
            }
        }
    }

    public void updateDisplayHealthBar(double tempHp, double currentHp, double maxHp) {
        // Ensure 1 <= maxHp <= 100
        double displayMaxHearts = BrightNumberUtils.clamp(1, maxHp, 40);

        // Ensure 0 <= currentHp <= maxHp
        double clampedCurrentHp = BrightNumberUtils.clamp(0, currentHp, maxHp);

        double hpPerHeart = Math.max(1, maxHp / displayMaxHearts);
        AttributeInstance genericMaxHpAttribute = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (genericMaxHpAttribute == null) return;
        if (genericMaxHpAttribute.getBaseValue() != displayMaxHearts)
            genericMaxHpAttribute.setBaseValue(displayMaxHearts);
        livingEntity.setHealth(clampedCurrentHp / hpPerHeart);
        livingEntity.setAbsorptionAmount(Math.max(0, tempHp / hpPerHeart));
    }

    public void healSelf(double amount) {
        double currentHp = getCurrentHp();
        if (currentHp <= 0) return; // Cant heal if youre dead

        double vitality = this.getStatFromCache(BrightStat.VITALITY);
        double actualAmount = amount * vitality / 100;
        this.setCurrentHp(currentHp + actualAmount);
    }

    public @NotNull LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Map<BrightCondition, Long> getConditions() {
        return conditions;
    }

    public boolean meleeDisabled() {
        return meleeDisabled;
    }

    public boolean bowDisabled() {
        return bowDisabled;
    }

    public boolean magicDisabled() {
        return magicDisabled;
    }

    public boolean movementDisabled() {
        return movementDisabled;
    }

    @Override
    public int hashCode() {
        return livingEntity.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrightEntity brightEntity)) return false;
        return livingEntity.getUniqueId().equals(brightEntity.getLivingEntity().getUniqueId());
    }
}
