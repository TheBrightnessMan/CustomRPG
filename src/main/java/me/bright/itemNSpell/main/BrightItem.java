package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightRPG;
import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import me.bright.entity.BrightEntity;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BrightItem {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private final PersistentDataContainer nbt;
    private final BrightRPG plugin;
    private final String name;
    private List<String> additionalModifierDescription = new ArrayList<>();
    private List<String> spellDescription = new ArrayList<>();
    private Rarity rarity = Rarity.COMMON;
    private final long id;
    private Predicate<BrightEntity> additionalDamagePredicate = brightEntity -> false;
    private Damage additionalDamage = new Damage(DamageType.TRUE, 0, null, false);

    protected BrightItem(long id, @NotNull Material material, @NotNull String name) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        this.nbt = itemMeta.getPersistentDataContainer();
        this.plugin = BrightRPG.getPlugin();
        this.name = name;
        this.id = id;
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        setAttribute(BrightItemAttribute.ID, id);
    }

    public static @Nullable BrightItem fromItemStack(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        PersistentDataContainer nbt = itemMeta.getPersistentDataContainer();
        long id = nbt.getOrDefault(new NamespacedKey(BrightRPG.getPlugin(), BrightItemAttribute.ID.key),
                PersistentDataType.LONG, BrightItemAttribute.ID.defaultValue);
        if (id == BrightItemAttribute.ID.defaultValue) {
            return new BrightItem(-1, itemStack.getType(), itemMeta.getDisplayName());
        }
        return BrightItemList.getItem(id);
    }

    public long getAttribute(@NotNull BrightItemAttribute attribute) {
        if (nbt == null) return attribute.defaultValue;
        return nbt.getOrDefault(new NamespacedKey(plugin, attribute.key),
                PersistentDataType.LONG, attribute.defaultValue);
    }

    public void setAttribute(@NotNull BrightItemAttribute attribute,
                             long value) {
        nbt.set(new NamespacedKey(plugin, attribute.key),
                PersistentDataType.LONG, value);
    }

    public @Nullable ItemStack buildItem() {
        if (itemStack == null || itemMeta == null) return null;
        List<String> finalLore = buildAttributeLore();
        if (!this.additionalModifierDescription.isEmpty()) {
            finalLore.add("");
            finalLore.addAll(this.additionalModifierDescription);
        }
        long spellId = getAttribute(BrightItemAttribute.SPELL);
        if (spellId != BrightItemAttribute.SPELL.defaultValue) {
            BrightSpell brightSpell = BrightSpellList.getSpell(spellId);
            if (brightSpell != null) {
                finalLore.add("");
                finalLore.add(ChatColor.YELLOW + "Spell: " + brightSpell.getDisplayName() + " " +
                        ChatColor.GOLD + ChatColor.BOLD + "RIGHT CLICK");
                finalLore.addAll(this.spellDescription);
                finalLore.addAll(brightSpell.buildAttributes());
            }
        }
        finalLore.add("");
        finalLore.add(rarity.displayName);
        itemMeta.setLore(finalLore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public String getName() {
        return name;
    }

    public @NotNull Rarity getRarity() {
        return rarity;
    }

    public void setRarity(@NotNull Rarity rarity) {
        this.rarity = rarity;
    }

    public @NotNull List<String> buildAttributeLore() {
        final List<String> lore = new ArrayList<>();
        final BrightItemAttribute[] attributes = BrightItemAttribute.values();
        for (int i = 0; i < attributes.length - 1; i++) {
            BrightItemAttribute attribute = attributes[i];
            long value = getAttribute(attribute);
            if (value == attribute.defaultValue) continue;
            lore.add(ChatColor.GRAY + attribute.displayName + ": " +
                    ChatColor.RED + "+" + value);
        }
        return lore;
    }

    public void setSpell(@NotNull BrightSpell spell) {
        setAttribute(BrightItemAttribute.SPELL, spell.getId());
    }

    public void setSpellDescription(@NotNull List<String> spellDescription) {
        this.spellDescription = spellDescription;
    }

    public long getId() {
        return this.id;
    }

    public List<String> getAdditionalModifierDescription() {
        return additionalModifierDescription;
    }

    public void setAdditionalModifierDescription(List<String> additionalModifierDescription) {
        this.additionalModifierDescription = additionalModifierDescription;
    }

    public List<String> getSpellDescription() {
        return spellDescription;
    }

    public Damage getAdditionalDamage() {
        return additionalDamage;
    }

    public void setAdditionalDamage(Predicate<BrightEntity> additionalDamagePredicate, Damage additionalDamage) {
        this.additionalDamagePredicate = additionalDamagePredicate;
        this.additionalDamage = additionalDamage;
    }

    public Predicate<BrightEntity> getAdditionalDamagePredicate() {
        return additionalDamagePredicate;
    }

    public enum Rarity {
        COMMON("" + ChatColor.WHITE + ChatColor.BOLD + "COMMON"),
        UNCOMMON("" + ChatColor.GREEN + ChatColor.BOLD + "UNCOMMON"),
        RARE("" + ChatColor.BLUE + ChatColor.BOLD + "RARE"),
        EPIC("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "EPIC"),
        LEGENDARY("" + ChatColor.GOLD + ChatColor.BOLD + "LEGENDARY"),
        MYTHIC("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "MYTHIC"),
        EXOTIC("" + ChatColor.AQUA + ChatColor.BOLD + "EXOTIC"),
        DIVINE("" + ChatColor.RED + ChatColor.BOLD + "D" +
                ChatColor.GOLD + ChatColor.BOLD + "I" +
                ChatColor.YELLOW + ChatColor.BOLD + "V" +
                ChatColor.GREEN + ChatColor.BOLD + "I" +
                ChatColor.AQUA + ChatColor.BOLD + "N" +
                ChatColor.DARK_PURPLE + ChatColor.BOLD + "E");

        public final String displayName;

        Rarity(String displayName) {
            this.displayName = displayName;
        }
    }
}
