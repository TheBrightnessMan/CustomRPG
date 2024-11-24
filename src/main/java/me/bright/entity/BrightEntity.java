package me.bright.entity;

import me.bright.brightrpg.BrightRPG;
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

import java.util.Random;
import java.util.logging.Level;

public class BrightEntity {

    private final BrightRPG plugin;
    private final LivingEntity livingEntity;
    private final PersistentDataContainer nbt;
    private final String name;
    private final long hpRegen = 5L;
    private final long manaRegen = 10L;

    public BrightEntity(@NotNull LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        this.plugin = BrightRPG.getPlugin();
        this.nbt = livingEntity.getPersistentDataContainer();
        this.name = livingEntity.getName();
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public String getName() {
        return name;
    }

    public long getEntityAttribute(@NotNull BrightEntityAttribute attribute) {
        return nbt.getOrDefault(new NamespacedKey(plugin, attribute.key),
                PersistentDataType.LONG, attribute.defaultValue);
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

    public Damage physicalHit(@NotNull BrightEntity target) {
        BrightItem weapon = getItemInMainHand();
        long weaponDamage = (weapon == null) ? 0 : weapon.getAttribute(BrightItemAttribute.DAMAGE);
        long strength = getStrength();
        long critChance = getCritChance();
        long critDamage = getCritDamage();
        double amount = (weaponDamage + 5) * (1 + (double) strength / 100);
        boolean crit = false;

        if (critChance == 0) {
            return target.takeDamage(new Damage(DamageType.PHYSICAL,
                    (long) amount, this, false));
        }

        if (critChance == 100 || new Random().nextInt(1, 101) <= critChance) {
            amount *= (1 + (double) critDamage / 100);
            crit = true;
        }

        if (this instanceof BrightPlayer player)
            amount *= Math.pow(player.getPlayer().getAttackCooldown(), 3);

        Damage real = target.takeDamage(
                new Damage(DamageType.PHYSICAL, (long) amount, this, crit));

        showDamage(target.getLivingEntity(), real);
        return real;
    }

    public Damage spellHit(@NotNull BrightEntity target, @NotNull BrightSpell brightSpell) {
        long intelligence = getInt();
        long spellDamage = getEntityAttribute(BrightEntityAttribute.SPELL_DMG);
        Damage baseDamage = brightSpell.getBaseDamage();
        Damage actual = new Damage(baseDamage.type(), baseDamage.amount(), this, baseDamage.critical());
        switch (actual.type()) {
            case PHYSICAL, MAGIC, TRUE -> {
                double amount = actual.amount() + (1 + brightSpell.getPower() / 10 * intelligence / 100) * spellDamage / 100;
                actual = new Damage(actual.type(), (long) amount, this, actual.critical());
            }
        }
        target.getLivingEntity().damage(0.01);
        Damage real = target.takeDamage(actual);
        showDamage(target.getLivingEntity(), real);
        return real;
    }

    public Damage takeDamage(@NotNull Damage damage) {
        if (damage.dealer() == null) return Damage.noDamage;
        BrightItem mainHand = damage.dealer().getItemInMainHand();
        long currentHp = getEntityAttribute(BrightEntityAttribute.CURRENT_HP);
        long maxHp = getEntityAttribute(BrightEntityAttribute.MAX_HP);

        if (currentHp <= 0 || maxHp <= 0) {
            plugin.getLogger().log(Level.SEVERE,
                    "Current hp or max hp <= 0! How did we get here!");
            return Damage.noDamage;
        }

        double resistance = 0L;
        long flatPen = 0L;
        long percentPen = 0L;

        switch (damage.type()) {
            case PHYSICAL, CURRENT_HP_PHYSICAL, MISSING_HP_PHYSICAL, MAX_HP_PHYSICAL -> {
                resistance = getArmor();
                if (mainHand == null) break;
                flatPen = mainHand.getAttribute(BrightItemAttribute.FLAT_ARMOR_PEN);
                percentPen = mainHand.getAttribute(BrightItemAttribute.PERCENT_ARMOR_PEN);
            }
            case MAGIC, CURRENT_HP_MAGIC, MISSING_HP_MAGIC, MAX_HP_MAGIC -> {
                resistance = getMagicResist();
                if (mainHand == null) break;
                flatPen = mainHand.getAttribute(BrightItemAttribute.FLAT_MAGIC_PEN);
                percentPen = mainHand.getAttribute(BrightItemAttribute.PERCENT_MAGIC_PEN);
            }
        }
        resistance *= (1 - (double) percentPen / 100);
        resistance -= flatPen;
        double resistanceDamageReduction = resistance / (resistance + Damage.getBalanceFactor());
        double actualDmg = 0L;

        switch (damage.type()) {
            case PHYSICAL, MAGIC, TRUE -> actualDmg += damage.amount();
            case CURRENT_HP_PHYSICAL, CURRENT_HP_MAGIC, CURRENT_HP_TRUE ->
                    actualDmg += (double) (damage.amount() * currentHp) / 100;
            case MISSING_HP_PHYSICAL, MISSING_HP_MAGIC, MISSING_HP_TRUE ->
                    actualDmg += (double) (damage.amount() * (maxHp - currentHp)) / 100;
            case MAX_HP_PHYSICAL, MAX_HP_MAGIC, MAX_HP_TRUE -> actualDmg += (double) (damage.amount() * maxHp) / 100;
        }

        actualDmg *= (1 - resistanceDamageReduction / 100);
        actualDmg *= (1 - (double) getDamageReduction() / 100);

        long finalDamage = (long) Math.max(0, actualDmg);
        setEntityAttribute(BrightEntityAttribute.CURRENT_HP, Math.max(currentHp - finalDamage, 0));
        return new Damage(damage.type(), finalDamage, damage.dealer(), damage.critical());
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BrightEntity)) return false;
        return livingEntity.getUniqueId().equals(((BrightEntity) obj).getLivingEntity().getUniqueId());
    }
}
