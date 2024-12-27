package me.bright.entity;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStatModifier;
import me.bright.brightrpg.BrightStats;
import me.bright.conditions.BrightCondition;
import me.bright.damage.BrightDamage;
import me.bright.damage.DamageType;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightSpell;
import me.bright.utils.BrightStringUtils;
import org.bukkit.ChatColor;
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
    private final Map<BrightStats, Double> statsCache = new HashMap<>();
    private final Map<BrightCondition, Double> conditions = new HashMap<>();

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

    public double getStatFromNbt(@NotNull BrightStats stat) {
        switch (stat) {
            case CURRENT_HP -> {
                double valueFromNbt = livingEntity.getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, stat.key),
                                PersistentDataType.DOUBLE, statsCache.get(BrightStats.MAX_HP));
                return Math.max(0, Math.min(valueFromNbt, statsCache.get(BrightStats.MAX_HP)));
            }
//            case CURRENT_MANA -> {
//                double valueFromNbt = livingEntity.getPersistentDataContainer()
//                        .getOrDefault(new NamespacedKey(plugin, stat.key),
//                                PersistentDataType.DOUBLE, stats.get(BrightStats.INTELLIGENCE));
//                return Math.max(0, Math.min(valueFromNbt, stats.get(BrightStats.INTELLIGENCE)));
//            }
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

    protected double setStatToNbt(@NotNull BrightStats stat, double val) {
        double clamped = Math.max(0, val);
        livingEntity.getPersistentDataContainer()
                .set(new NamespacedKey(plugin, stat.key),
                        PersistentDataType.DOUBLE, clamped);
        return clamped;
    }

    public double getStatFromCache(@NotNull BrightStats stat) {
        return Math.max(0, statsCache.getOrDefault(stat, stat.entityBaseValue));
    }

    private void setStatToCache(@NotNull BrightStats stat, double value) {
        statsCache.put(stat, Math.max(0, value));
    }

    public @Nullable BrightItem getItemInMainHand() {
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return null;
        return BrightItem.fromItemStack(equipment.getItemInMainHand());
    }

    public @NotNull BrightDamage[] meleeHit(@NotNull BrightEntity target) {
        BrightItem weapon = getItemInMainHand();

        // Calculate base damage
        double weaponBaseDamage = (weapon == null) ? 1 : weapon.getBaseDamage();
        double weaponDamageAmount = (weaponBaseDamage + 5) * (this.getStatFromCache(BrightStats.STRENGTH) / 100);

        // Roll for crit
        boolean crit = BrightDamage.rollCrit(this.getStatFromCache(BrightStats.CRIT_CHANCE));
        if (crit) weaponDamageAmount *= (1 + this.getStatFromCache(BrightStats.CRIT_DMG) / 100);

        if (this.getLivingEntity() instanceof Player player)
            weaponDamageAmount *= Math.pow(player.getAttackCooldown(), 6);

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
        damageRegistry = (weapon == null) ? damageRegistry : weapon.onMeleeHit(this, target, damageRegistry);

        // Finalise and apply the damage
        return target.applyDamages(damageRegistry);
    }

    public @NotNull BrightDamage[] spellHit(@NotNull BrightEntity target, @NotNull BrightSpell spell) {
        BrightItem weapon = getItemInMainHand();

        // Calculate base damage
        double entitySpellDamage = this.getStatFromCache(BrightStats.SPELL_DMG);
        double entityIntelligence = this.getStatFromCache(BrightStats.INTELLIGENCE);
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

    public BrightDamage[] applyDamages(List<BrightDamage> damages) {
        if (damages.isEmpty()) return BrightDamage.emptyFinalised;
        BrightEntity attacker = damages.getFirst().dealer();
        if (attacker == null) return BrightDamage.emptyFinalised; // How did this happen?

        double currentHp = getCurrentHp(),
                maxHp = getStatFromCache(BrightStats.MAX_HP),
                tempHp = getStatFromCache(BrightStats.TEMP_HP),
                damageTaken = getStatFromCache(BrightStats.DAMAGE_TAKEN);
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
            this.setTempHp(tempHp - totalDamage);
        } else {
            this.setTempHp(0);
            this.setCurrentHp(currentHp + tempHp - totalDamage);
        }
        this.update();
        return finalMergedDamages;
    }

    public void applyCondition(BrightCondition condition, double durationSeconds, boolean override) {
        condition.onStart(this);
        if (override) {
            conditions.put(condition, durationSeconds);
            return;
        }
        double existingDuration = conditions.getOrDefault(condition, 0D);
        if (condition.canStack()) {
            conditions.put(condition, existingDuration + durationSeconds);
            return;
        }
        if (existingDuration == 0) conditions.put(condition, durationSeconds);
    }

    public double getTempHp() {
        return this.getStatFromNbt(BrightStats.TEMP_HP);
    }

    public void setTempHp(double newTempHp) {
        double maxTempHp = 10 * this.getStatFromCache(BrightStats.MAX_HP),
                actual = Math.min(newTempHp, maxTempHp);
        setStatToNbt(BrightStats.TEMP_HP, actual);
    }

    public double getCurrentHp() {
        return getStatFromNbt(BrightStats.CURRENT_HP);
    }

    public void setCurrentHp(double newHp) {
        double maxHp = getStatFromCache(BrightStats.MAX_HP),
                actual = Math.min(newHp, maxHp);
        setStatToNbt(BrightStats.CURRENT_HP, actual);
    }

    public double getCurrentMana() {
        return getStatFromNbt(BrightStats.CURRENT_MANA);
    }

    public void setCurrentMana(double newMana) {
        double maxHp = this.getStatFromCache(BrightStats.INTELLIGENCE),
                actual = Math.min(newMana, maxHp);
        setStatToNbt(BrightStats.CURRENT_MANA, actual);
    }

    public boolean update() {
        tickConditions();
        updateStatsCache();

        double currentHp = getCurrentHp(),
                maxHp = getStatFromCache(BrightStats.MAX_HP),
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
        for (BrightStats stat : BrightStats.values()) {
            double result = calculateStat(stat);
            statsCache.put(stat, result);
        }
    }

    private double calculateStat(BrightStats stat) {
        EntityEquipment equipment = getLivingEntity().getEquipment();
        double flat = getStatFlatMod(stat),
                add = getStatAddMod(stat),
                mul = getStatMulMod(stat) / 100;

        if (equipment != null) {
            for (ItemStack armor : equipment.getArmorContents()) {
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                flat += brightArmor.getStatFlatMod(stat);
                add += brightArmor.getStatAddMod(stat);
                mul *= (1 + brightArmor.getStatMulMod(stat) / 100);
            }
        }

        BrightItem mainHand = this.getItemInMainHand();
        if (mainHand != null) {
            flat += mainHand.getStatFlatMod(stat);
            add += mainHand.getStatAddMod(stat);
            mul *= (1 + mainHand.getStatMulMod(stat) / 100);
        }

        return flat * add / 100 * mul;
    }

    private void updateNameTag() {
        long currentHp = (long) getCurrentHp(),
                maxHp = (long) getStatFromCache(BrightStats.MAX_HP),
                tempHp = (long) getTempHp(),
                level = (long) getStatFromCache(BrightStats.LEVEL);

        StringBuilder customName = new StringBuilder(ChatColor.GRAY + "[Level ").append(level).append("] ")
                .append(name).append(" ");

        if (tempHp > 0)
            customName.append(BrightStats.TEMP_HP.color).append(tempHp + currentHp);
        else
            customName.append(BrightStats.CURRENT_HP.color).append(currentHp);

        customName.append(ChatColor.GRAY).append("/")
                .append(BrightStats.MAX_HP.color).append(maxHp).
                append(ChatColor.RED).append("♥");

        livingEntity.setCustomNameVisible(true);
        livingEntity.setCustomName(customName.toString());
    }

    public void tickConditions() {
        var iterator = conditions.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BrightCondition condition = entry.getKey();
            double duration = entry.getValue();

            condition.tick(this);
            duration -= 0.1;

            if (duration <= 0) {
                condition.onEnd(this);
                iterator.remove();
            } else {
                conditions.put(condition, duration); // Update duration
            }
        }
    }

    public void updateDisplayHealthBar(double tempHp, double currentHp, double maxHp) {
        // Ensure 1 <= maxHp <= 100
        double displayMaxHearts = Math.max(1, Math.min(maxHp, 40));

        // Ensure 0 <= currentHp <= maxHp
        double clampedCurrentHp = Math.max(0, Math.min(currentHp, maxHp));

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

        double vitality = this.getStatFromCache(BrightStats.VITALITY);
        double actualAmount = amount * vitality / 100;
        this.setCurrentHp(currentHp + actualAmount);
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public String getName() {
        return name;
    }

//    public List<Damage> physicalHit(@NotNull BrightEntity target) {
//        final List<Damage> damages = new ArrayList<>();
//
//        BrightItem weapon = getItemInMainHand();
//        Damage baseDamage = Damage.noDamage;
//        List<ConditionalDamage> conditionalDamages = new ArrayList<>();
//        if (weapon != null) {
//            baseDamage = weapon.getBaseDamage();
//            conditionalDamages = weapon.getConditionalDamage();
//        }
//
//        long strength = getStrength();
//        long critChance = getCritChance();
//        long critDamage = getCritDamage();
//
//        double amount = (baseDamage.amount() + 5) * (1 + (double) strength / 100);
//        if (this instanceof BrightPlayer player)
//            amount *= Math.pow(player.getPlayer().getAttackCooldown(), 3);
//
//        if (rollCrit(critChance))
//            damages.add(new Damage(DamageType.PHYSICAL,
//                    (long) (amount * (1 + (double) critDamage / 100)),
//                    this, true));
//        else
//            damages.add(new Damage(DamageType.PHYSICAL, (long) amount, this, false));
//
//        for (ConditionalDamage conditionalDamage : conditionalDamages) {
//            var condition = conditionalDamage.condition();
//            Damage damage = conditionalDamage.damage();
//            if (condition.test(this, target)) damages.add(damage);
//        }
//
//        List<Damage> real = target.takeDamage(damages);
//        showDamages(target.getLivingEntity(), real);
//        return real;
//    }

//    public List<Damage> spellHit(@NotNull BrightEntity target, @NotNull BrightSpell brightSpell) {
//        long intelligence = getInt();
//        long spellDamage = getEntityAttribute(BrightEntityAttribute.SPELL_DMG);
//        var mainHand = this.getItemInMainHand();
//        var conditionalDamages = (mainHand == null) ?
//                new ArrayList<ConditionalDamage>() :
//                mainHand.getConditionalDamage();
//
//        List<Damage> damages = new ArrayList<>();
//        Damage baseDamage = brightSpell.getBaseDamage();
//
//        Damage actual = new Damage(baseDamage.type(), baseDamage.amount(), this, baseDamage.critical());
//        switch (actual.type()) {
//            case PHYSICAL, MAGIC, TRUE -> {
//                double amount = actual.amount() + (1 + brightSpell.getPower() / 10 * intelligence / 100) * spellDamage / 100;
//                actual = new Damage(actual.type(), (long) amount, this, actual.critical());
//            }
//        }
//        damages.add(actual);
//
//        for (var conditionalDamage : conditionalDamages) {
//            var condition = conditionalDamage.condition();
//            if (condition.test(this, target)) damages.add(conditionalDamage.damage());
//        }
//
//        target.getLivingEntity().damage(0.01);
//        List<Damage> real = target.takeDamage(damages);
//        showDamages(target.getLivingEntity(), real);
//        return real;
//    }

//    public List<Damage> takeDamage(@NotNull List<Damage> damages) {
//        if (damages.isEmpty()) return new ArrayList<>();
//        BrightEntity dealer = damages.getFirst().dealer();
//        if (dealer == null) return new ArrayList<>();
//        long currentHp = getEntityAttribute(BrightEntityAttribute.CURRENT_HP),
//                maxHp = getEntityAttribute(BrightEntityAttribute.MAX_HP);
//        double currentHpPercent = (double) currentHp / 100,
//                missingHpPercent = (double) (maxHp - currentHp) / 100,
//                maxHpPercent = (double) maxHp / 100;
//
//        if (currentHp <= 0 || maxHp <= 0) {
//            livingEntity.remove();
//            return new ArrayList<>();
//        }
//
//        BrightItem weapon = damages.getFirst().dealer().getItemInMainHand();
//        long flatArmorPen = 0L, percentArmorPen = 0L, flatMagicPen = 0L, percentMagicPen = 0L;
//        if (weapon != null) {
//            flatArmorPen = weapon.getAttribute(BrightItemAttribute.FLAT_ARMOR_PEN);
//            percentArmorPen = weapon.getAttribute(BrightItemAttribute.PERCENT_ARMOR_PEN);
//            flatMagicPen = weapon.getAttribute(BrightItemAttribute.FLAT_MAGIC_PEN);
//            percentMagicPen = weapon.getAttribute(BrightItemAttribute.PERCENT_MAGIC_PEN);
//        }
//
//        double effectiveArmor = (getArmor() - flatArmorPen) * (1 - (double) percentArmorPen / 100),
//                effectiveMR = (getArmor() - flatMagicPen) * (1 - (double) percentMagicPen / 100),
//                physicalReduction = effectiveArmor / (effectiveArmor + Damage.getBalanceFactor()),
//                magicalReduction = effectiveMR / (effectiveMR + Damage.getBalanceFactor());
//        long universalReduction = getDamageReduction();
//
//        double rawPhysical = 0L,
//                rawMagic = 0L,
//                rawTrue = 0L;
//        boolean physicalCrit = false,
//                magicCrit = false,
//                trueCrit = false;
//        for (Damage damage : damages) {
//            switch (damage.type()) {
//                case PHYSICAL -> {
//                    rawPhysical += damage.amount();
//                    physicalCrit = physicalCrit || damage.critical();
//                }
//                case MAGIC -> {
//                    rawMagic += damage.amount();
//                    magicCrit = magicCrit || damage.critical();
//                }
//                case TRUE -> {
//                    rawTrue += damage.amount();
//                    trueCrit = trueCrit || damage.critical();
//                }
//
//                case CURRENT_HP_PHYSICAL -> rawPhysical += damage.amount() * currentHpPercent;
//                case CURRENT_HP_MAGIC -> rawMagic += damage.amount() * currentHpPercent;
//                case CURRENT_HP_TRUE -> rawTrue += damage.amount() * currentHpPercent;
//
//                case MISSING_HP_PHYSICAL -> rawPhysical += damage.amount() * missingHpPercent;
//                case MISSING_HP_MAGIC -> rawMagic += damage.amount() * missingHpPercent;
//                case MISSING_HP_TRUE -> rawTrue += damage.amount() * missingHpPercent;
//
//                case MAX_HP_PHYSICAL -> rawPhysical += damage.amount() * maxHpPercent;
//                case MAX_HP_MAGIC -> rawMagic += damage.amount() * maxHpPercent;
//                case MAX_HP_TRUE -> rawTrue += damage.amount() * maxHpPercent;
//            }
//        }
//
//        rawPhysical *= (1 - physicalReduction) * (1 - (double) universalReduction / 100);
//        rawMagic *= (1 - magicalReduction) * (1 - (double) universalReduction / 100);
//
//        long finalPhysical = (long) Math.max(0, rawPhysical),
//                finalMagic = (long) Math.max(0, rawMagic),
//                finalTrue = (long) Math.max(0, rawTrue),
//                total = finalPhysical + finalMagic + finalTrue;
//
//        if (total >= currentHp)
//            livingEntity.setHealth(0);
//
//        setEntityAttribute(BrightEntityAttribute.CURRENT_HP, currentHp - total);
//
//        return Arrays.asList(
//                new Damage(DamageType.PHYSICAL, finalPhysical, dealer, physicalCrit),
//                new Damage(DamageType.MAGIC, finalMagic, dealer, magicCrit),
//                new Damage(DamageType.TRUE, finalTrue, dealer, trueCrit)
//        );
//    }
//
//
//
//    public void regenResources() {
//        long currentHp = getCurrentHp();
//        long maxHp = getMaxHp();
//        long currentMana = getCurrentMana();
//        long maxMana = getMaxMana();
//
//        if (currentHp != maxHp)
//            setEntityAttribute(BrightEntityAttribute.CURRENT_HP,
//                    Math.min(currentHp + hpRegen, maxHp));
//
//        if (currentMana != maxMana)
//            setEntityAttribute(BrightEntityAttribute.CURRENT_MANA,
//                    Math.min(currentMana + manaRegen, maxMana));
//    }
//
//
//    public long getStrength() {
//        long total = getEntityAttribute(BrightEntityAttribute.STRENGTH);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.STRENGTH);
//            }
//        }
//
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.STRENGTH);
//        return total;
//    }
//
//    public long getMaxHp() {
//        long total = getEntityAttribute(BrightEntityAttribute.MAX_HP);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.MAX_HP);
//            }
//        }
//
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.MAX_HP);
//        return total;
//    }
//
//    public long getInt() {
//        long total = getEntityAttribute(BrightEntityAttribute.INT);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.INT);
//            }
//        }
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.INT);
//        return total;
//    }
//
//    public long getCritChance() {
//        long total = getEntityAttribute(BrightEntityAttribute.CRIT_CHANCE);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.CRIT_CHANCE);
//            }
//        }
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.CRIT_CHANCE);
//        return total;
//    }
//
//    public long getCritDamage() {
//        long total = getEntityAttribute(BrightEntityAttribute.CRIT_DMG);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.CRIT_DMG);
//            }
//        }
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.CRIT_DMG);
//        return total;
//    }
//
//    public long getArmor() {
//        long total = getEntityAttribute(BrightEntityAttribute.ARMOR);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.ARMOR);
//            }
//        }
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.ARMOR);
//        return total;
//    }
//
//    public long getMagicResist() {
//        long total = getEntityAttribute(BrightEntityAttribute.MAGIC_RESIST);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.MAGIC_RESIST);
//            }
//        }
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.MAGIC_RESIST);
//        return total;
//    }
//
//    public long getDamageReduction() {
//        long total = getEntityAttribute(BrightEntityAttribute.DMG_REDUCTION);
//
//        EntityEquipment equipment = livingEntity.getEquipment();
//        if (equipment != null) {
//            ItemStack[] armors = equipment.getArmorContents();
//            for (ItemStack armor : armors) {
//                if (armor == null) continue;
//                BrightItem brightArmor = BrightItem.fromItemStack(armor);
//                if (brightArmor == null) continue;
//                total += brightArmor.getAttribute(BrightItemAttribute.DMG_REDUCTION);
//            }
//        }
//
//        BrightItem mainHand = getItemInMainHand();
//        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.DMG_REDUCTION);
//        return total;
//    }
//
//    public long getCurrentHp() {
//        return getEntityAttribute(BrightEntityAttribute.CURRENT_HP);
//    }
//
//    public long getCurrentMana() {
//        return getEntityAttribute(BrightEntityAttribute.CURRENT_MANA);
//    }
//
//    public long getMaxMana() {
//        return getInt();
//    }
//
//    public long getSpeed() {
//        return getEntityAttribute(BrightEntityAttribute.SPEED);
//    }
//
//    public double setGenericAttribute(Attribute attribute, AttributeModifier modifier) {
//        AttributeInstance attributeInstance = livingEntity.getAttribute(attribute);
//        if (attributeInstance == null) return 0;
//        for (AttributeModifier existingModifier : attributeInstance.getModifiers()) {
//            attributeInstance.removeModifier(existingModifier);
//        }
//        attributeInstance.addModifier(modifier);
//        return attributeInstance.getValue();
//    }
//
//
//

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
