package me.bright.entity;

import me.bright.brightrpg.BrightRPG;
import me.bright.damage.ConditionalDamage;
import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.itemNSpell.main.BrightItem;
import me.bright.itemNSpell.main.BrightItemAttribute;
import me.bright.itemNSpell.main.BrightSpell;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BrightEntity {

    private final BrightRPG plugin;
    private final LivingEntity livingEntity;
    private final PersistentDataContainer nbt;
    private final String name;
    private final long hpRegen = 2L;
    private final long manaRegen = 5L;
    private static final String KEY_ATTRIBUTE = "KEY", DEFAULT_KEY = "null";

    protected BrightEntity(@NotNull LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        this.plugin = BrightRPG.getPlugin();
        this.nbt = livingEntity.getPersistentDataContainer();
        String nbtName = nbt.getOrDefault(new NamespacedKey(plugin, "NAME"),
                PersistentDataType.STRING, "null");
        this.name = (nbtName.equals("null")) ?
                toPrettyString(livingEntity.getType().toString()) :
                nbtName;
    }

    public static @Nullable BrightEntity fromLivingEntity(@Nullable LivingEntity entity) {
        if (entity == null) return null;
        PersistentDataContainer nbt = entity.getPersistentDataContainer();
        String givenKey = nbt.getOrDefault(new NamespacedKey(BrightRPG.getPlugin(), KEY_ATTRIBUTE),
                PersistentDataType.STRING, DEFAULT_KEY);
        return (givenKey.equals(DEFAULT_KEY)) ?
                BrightEntityList.getVanillaEntity(entity) :
                BrightEntityList.getCustomEntity(givenKey);
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public String getName() {
        return name;
    }

    public long getEntityAttribute(@NotNull BrightEntityAttribute attribute) {
        long result = 0;
        switch (attribute) {
            case CURRENT_HP -> result = nbt.getOrDefault(new NamespacedKey(plugin, attribute.key),
                    PersistentDataType.LONG, getMaxHp());
            case CURRENT_MANA -> result = nbt.getOrDefault(new NamespacedKey(plugin, attribute.key),
                    PersistentDataType.LONG, getMaxMana());
            default -> result = nbt.getOrDefault(new NamespacedKey(plugin, attribute.key),
                    PersistentDataType.LONG, attribute.defaultValue);
        }
        return result;
    }

    public long setEntityAttribute(@NotNull BrightEntityAttribute attribute, long val) {
        nbt.set(new NamespacedKey(plugin, attribute.key),
                PersistentDataType.LONG, val);
        return val;
    }

    public @Nullable BrightItem getItemInMainHand() {
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return null;
        return BrightItem.fromItemStack(equipment.getItemInMainHand());
    }

    public List<Damage> physicalHit(@NotNull BrightEntity target) {
        final List<Damage> damages = new ArrayList<>();

        BrightItem weapon = getItemInMainHand();
        Damage baseDamage = Damage.noDamage;
        List<ConditionalDamage> conditionalDamages = new ArrayList<>();
        if (weapon != null) {
            baseDamage = weapon.getBaseDamage();
            conditionalDamages = weapon.getConditionalDamage();
        }

        long strength = getStrength();
        long critChance = getCritChance();
        long critDamage = getCritDamage();

        double amount = (baseDamage.amount() + 5) * (1 + (double) strength / 100);
        if (this instanceof BrightPlayer player)
            amount *= Math.pow(player.getPlayer().getAttackCooldown(), 3);

        if (rollCrit(critChance))
            damages.add(new Damage(DamageType.PHYSICAL,
                    (long) (amount * (1 + (double) critDamage / 100)),
                    this, true));
        else
            damages.add(new Damage(DamageType.PHYSICAL, (long) amount, this, false));

        for (ConditionalDamage conditionalDamage : conditionalDamages) {
            var condition = conditionalDamage.condition();
            Damage damage = conditionalDamage.damage();
            if (condition.test(this, target)) damages.add(damage);
        }

        List<Damage> real = target.takeDamage(damages);
        showDamages(target.getLivingEntity(), real);
        return real;
    }

    public List<Damage> spellHit(@NotNull BrightEntity target, @NotNull BrightSpell brightSpell) {
        long intelligence = getInt();
        long spellDamage = getEntityAttribute(BrightEntityAttribute.SPELL_DMG);
        var mainHand = this.getItemInMainHand();
        var conditionalDamages = (mainHand == null) ?
                new ArrayList<ConditionalDamage>() :
                mainHand.getConditionalDamage();

        List<Damage> damages = new ArrayList<>();
        Damage baseDamage = brightSpell.getBaseDamage();

        Damage actual = new Damage(baseDamage.type(), baseDamage.amount(), this, baseDamage.critical());
        switch (actual.type()) {
            case PHYSICAL, MAGIC, TRUE -> {
                double amount = actual.amount() + (1 + brightSpell.getPower() / 10 * intelligence / 100) * spellDamage / 100;
                actual = new Damage(actual.type(), (long) amount, this, actual.critical());
            }
        }
        damages.add(actual);

        for (var conditionalDamage : conditionalDamages) {
            var condition = conditionalDamage.condition();
            if (condition.test(this, target)) damages.add(conditionalDamage.damage());
        }

        target.getLivingEntity().damage(0.01);
        List<Damage> real = target.takeDamage(damages);
        showDamages(target.getLivingEntity(), real);
        return real;
    }

    public List<Damage> takeDamage(@NotNull List<Damage> damages) {
        if (damages.isEmpty()) return damages;
        BrightEntity dealer = damages.getFirst().dealer();
        if (dealer == null) return new ArrayList<>();
        long currentHp = getEntityAttribute(BrightEntityAttribute.CURRENT_HP),
                maxHp = getEntityAttribute(BrightEntityAttribute.MAX_HP);
        double currentHpPercent = (double) currentHp / 100,
                missingHpPercent = (double) (maxHp - currentHp) / 100,
                maxHpPercent = (double) maxHp / 100;

        if (currentHp <= 0 || maxHp <= 0) {
            plugin.getLogger().log(Level.SEVERE,
                    "Current hp or max hp <= 0! How did we get here!");
            livingEntity.remove();
            return new ArrayList<>();
        }

        BrightItem weapon = damages.getFirst().dealer().getItemInMainHand();
        long flatArmorPen = 0L, percentArmorPen = 0L, flatMagicPen = 0L, percentMagicPen = 0L;
        if (weapon != null) {
            flatArmorPen = weapon.getAttribute(BrightItemAttribute.FLAT_ARMOR_PEN);
            percentArmorPen = weapon.getAttribute(BrightItemAttribute.PERCENT_ARMOR_PEN);
            flatMagicPen = weapon.getAttribute(BrightItemAttribute.FLAT_MAGIC_PEN);
            percentMagicPen = weapon.getAttribute(BrightItemAttribute.PERCENT_MAGIC_PEN);
        }

        double effectiveArmor = (getArmor() - flatArmorPen) * (1 - (double) percentArmorPen / 100),
                effectiveMR = (getArmor() - flatMagicPen) * (1 - (double) percentMagicPen / 100),
                physicalReduction = effectiveArmor / (effectiveArmor + Damage.getBalanceFactor()),
                magicalReduction = effectiveMR / (effectiveMR + Damage.getBalanceFactor());
        long universalReduction = getDamageReduction();

        double rawPhysical = 0L,
                rawMagic = 0L,
                rawTrue = 0L;
        boolean physicalCrit = false,
                magicCrit = false,
                trueCrit = false;
        for (Damage damage : damages) {
            switch (damage.type()) {
                case PHYSICAL -> {
                    rawPhysical += damage.amount();
                    physicalCrit = physicalCrit || damage.critical();
                }
                case MAGIC -> {
                    rawMagic += damage.amount();
                    magicCrit = magicCrit || damage.critical();
                }
                case TRUE -> {
                    rawTrue += damage.amount();
                    trueCrit = trueCrit || damage.critical();
                }

                case CURRENT_HP_PHYSICAL -> rawPhysical += damage.amount() * currentHpPercent;
                case CURRENT_HP_MAGIC -> rawMagic += damage.amount() * currentHpPercent;
                case CURRENT_HP_TRUE -> rawTrue += damage.amount() * currentHpPercent;

                case MISSING_HP_PHYSICAL -> rawPhysical += damage.amount() * missingHpPercent;
                case MISSING_HP_MAGIC -> rawMagic += damage.amount() * missingHpPercent;
                case MISSING_HP_TRUE -> rawTrue += damage.amount() * missingHpPercent;

                case MAX_HP_PHYSICAL -> rawPhysical += damage.amount() * maxHpPercent;
                case MAX_HP_MAGIC -> rawMagic += damage.amount() * maxHpPercent;
                case MAX_HP_TRUE -> rawTrue += damage.amount() * maxHpPercent;
            }
        }

        rawPhysical *= (1 - physicalReduction) * (1 - (double) universalReduction / 100);
        rawMagic *= (1 - magicalReduction) * (1 - (double) universalReduction / 100);

        long finalPhysical = (long) Math.max(0, rawPhysical),
                finalMagic = (long) Math.max(0, rawMagic),
                finalTrue = (long) Math.max(0, rawTrue),
                total = finalPhysical + finalMagic + finalTrue;

        if (total >= currentHp)
            setEntityAttribute(BrightEntityAttribute.CURRENT_HP, currentHp - total);
        else
            livingEntity.remove();

        return Arrays.asList(
                new Damage(DamageType.PHYSICAL, finalPhysical, dealer, physicalCrit),
                new Damage(DamageType.MAGIC, finalMagic, dealer, magicCrit),
                new Damage(DamageType.TRUE, finalTrue, dealer, trueCrit)
        );
    }

    private void showDamages(@NotNull LivingEntity target,
                             @NotNull List<Damage> damages) {
        for (Damage damage : damages) {
            if (damage.amount() > 0) showDamage(target, damage);
        }
    }

    private void showDamage(@NotNull LivingEntity target,
                            @NotNull Damage damage) {
        World world = livingEntity.getWorld();
        final String displayDamage = (damage.critical()) ?
                "" + damage.type().color + ChatColor.BOLD + "!  " + damage.amount() + "  !" :
                "" + damage.type().color + damage.amount();
        Location location = target.getLocation().clone()
                .add(new Random().nextDouble() * 2 - 1,
                        1,
                        new Random().nextDouble() * 2 - 1);
        world.spawn(location, ArmorStand.class, CreatureSpawnEvent.SpawnReason.CUSTOM, false,
                dmgIndicator -> {
                    dmgIndicator.setMarker(true);
                    dmgIndicator.setVisible(false);
                    dmgIndicator.setGravity(false);
                    dmgIndicator.setSmall(true);
                    dmgIndicator.setCustomNameVisible(true);
                    dmgIndicator.setCustomName(displayDamage);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            dmgIndicator.remove();
                        }
                    }.runTaskLater(plugin, 3 * 20L);
                });
    }

    public boolean update() {
        long currentHp = getCurrentHp();
        long maxHp = getMaxHp();
        long level = getEntityAttribute(BrightEntityAttribute.LEVEL);

        if (currentHp <= 0 || livingEntity.isDead()) {
            livingEntity.setHealth(0);
            return false;
        }

//        setGenericAttribute(Attribute.GENERIC_MOVEMENT_SPEED,
//                new AttributeModifier(
//                        new NamespacedKey(plugin, BrightEntityAttribute.SPEED.key),
//                        (double) getSpeed() / 100,
//                        AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY));

        updateHealthBar(currentHp, maxHp);

        String customName = ChatColor.GRAY + "[Level " + level + "] " +
                name + " " +
                ChatColor.RED + currentHp +
                ChatColor.GRAY + "/" +
                ChatColor.DARK_RED + maxHp +
                ChatColor.RED + "♥";

        livingEntity.setCustomNameVisible(true);
        livingEntity.setCustomName(customName);
        return true;
    }

    public void regenResources() {
        long currentHp = getCurrentHp();
        long maxHp = getMaxHp();
        long currentMana = getCurrentMana();
        long maxMana = getMaxMana();

        if (currentHp != maxHp)
            setEntityAttribute(BrightEntityAttribute.CURRENT_HP,
                    Math.min(currentHp + hpRegen, maxHp));

        if (currentMana != maxMana)
            setEntityAttribute(BrightEntityAttribute.CURRENT_MANA,
                    Math.min(currentMana + manaRegen, maxMana));
    }

    public void updateHealthBar(long currentHp, long maxHp) {
        long displayMaxHp = Math.min(40L, maxHp);
        double currentHpPercent = (double) currentHp / maxHp;
        AttributeInstance genericMaxHpAttribute = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (genericMaxHpAttribute == null) return;
        genericMaxHpAttribute.setBaseValue(displayMaxHp);
        livingEntity.setHealth(currentHpPercent * displayMaxHp);
    }

    public long getStrength() {
        long total = getEntityAttribute(BrightEntityAttribute.STRENGTH);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.STRENGTH);
            }
        }

        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.STRENGTH);
        return total;
    }

    public long getMaxHp() {
        long total = getEntityAttribute(BrightEntityAttribute.MAX_HP);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.MAX_HP);
            }
        }

        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.MAX_HP);
        return total;
    }

    public long getInt() {
        long total = getEntityAttribute(BrightEntityAttribute.INT);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.INT);
            }
        }
        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.INT);
        return total;
    }

    public long getCritChance() {
        long total = getEntityAttribute(BrightEntityAttribute.CRIT_CHANCE);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.CRIT_CHANCE);
            }
        }
        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.CRIT_CHANCE);
        return total;
    }

    public long getCritDamage() {
        long total = getEntityAttribute(BrightEntityAttribute.CRIT_DMG);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.CRIT_DMG);
            }
        }
        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.CRIT_DMG);
        return total;
    }

    public long getArmor() {
        long total = getEntityAttribute(BrightEntityAttribute.ARMOR);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.ARMOR);
            }
        }
        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.ARMOR);
        return total;
    }

    public long getMagicResist() {
        long total = getEntityAttribute(BrightEntityAttribute.MAGIC_RESIST);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.MAGIC_RESIST);
            }
        }
        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.MAGIC_RESIST);
        return total;
    }

    public long getDamageReduction() {
        long total = getEntityAttribute(BrightEntityAttribute.DMG_REDUCTION);

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            ItemStack[] armors = equipment.getArmorContents();
            for (ItemStack armor : armors) {
                if (armor == null) continue;
                BrightItem brightArmor = BrightItem.fromItemStack(armor);
                if (brightArmor == null) continue;
                total += brightArmor.getAttribute(BrightItemAttribute.DMG_REDUCTION);
            }
        }

        BrightItem mainHand = getItemInMainHand();
        if (mainHand != null) total += mainHand.getAttribute(BrightItemAttribute.DMG_REDUCTION);
        return total;
    }

    public long getCurrentHp() {
        return getEntityAttribute(BrightEntityAttribute.CURRENT_HP);
    }

    public long getCurrentMana() {
        return getEntityAttribute(BrightEntityAttribute.CURRENT_MANA);
    }

    public long getMaxMana() {
        return getInt();
    }

    public long getSpeed() {
        return getEntityAttribute(BrightEntityAttribute.SPEED);
    }

    public double setGenericAttribute(Attribute attribute, AttributeModifier modifier) {
        AttributeInstance attributeInstance = livingEntity.getAttribute(attribute);
        if (attributeInstance == null) return 0;
        for (AttributeModifier existingModifier : attributeInstance.getModifiers()) {
            attributeInstance.removeModifier(existingModifier);
        }
        attributeInstance.addModifier(modifier);
        return attributeInstance.getValue();
    }

    private boolean rollCrit(long critChance) {
        if (critChance <= 0) return false;
        if (critChance >= 100) return true;
        return new Random().nextInt(1, 101) <= critChance;
    }

    private String toPrettyString(String key) {
        return Arrays.stream(key.toLowerCase().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BrightEntity)) return false;
        return livingEntity.getUniqueId().equals(((BrightEntity) obj).getLivingEntity().getUniqueId());
    }
}
