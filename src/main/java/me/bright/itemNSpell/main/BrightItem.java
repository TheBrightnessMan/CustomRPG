package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightRPG;
import me.bright.damage.ConditionalDamage;
import me.bright.damage.Damage;
import me.bright.damage.DamageType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrightItem {

    private final BrightRPG plugin;

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private final PersistentDataContainer nbt;

    private final String name;
    public static final String KEY_ATTRIBUTE = "KEY", DEFAULT_KEY = "null";
    private final Map<BrightItemAttribute, Long> attributes = new HashMap<>();
    private Rarity rarity = Rarity.COMMON;
    private BrightSpell spell = null;

    private Damage baseDamage = new Damage(DamageType.PHYSICAL, 0, null, false);
    private final List<ConditionalDamage> conditionalDamages = new ArrayList<>();
    private final List<String> additionalModifierDescription = new ArrayList<>();

    protected BrightItem(@NotNull String key, @NotNull Material material, @NotNull String name) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        this.nbt = itemMeta.getPersistentDataContainer();
        this.plugin = BrightRPG.getPlugin();
        this.name = name;
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        nbt.set(new NamespacedKey(plugin, KEY_ATTRIBUTE), PersistentDataType.STRING, key);
    }

    public static @Nullable BrightItem fromItemStack(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;
        PersistentDataContainer nbt = itemMeta.getPersistentDataContainer();
        String givenKey = nbt.getOrDefault(new NamespacedKey(BrightRPG.getPlugin(), KEY_ATTRIBUTE),
                PersistentDataType.STRING, DEFAULT_KEY);
        if (givenKey.equals(DEFAULT_KEY))
            return BrightItemList.getVanillaItem(itemStack);
        return BrightItemList.getCustomItem(givenKey);
    }

    public long getAttribute(@NotNull BrightItemAttribute attribute) {
        return attributes.getOrDefault(attribute, attribute.defaultValue);
    }

    public void setAttribute(@NotNull BrightItemAttribute attribute,
                             long value) {
        attributes.put(attribute, value);
    }

    public String getName() {
        return name;
    }

    protected void setBaseDamage(@NotNull Damage baseDamage) {
        this.baseDamage = baseDamage;
    }

    public @NotNull Damage getBaseDamage() {
        return this.baseDamage;
    }

    protected void addConditionalDamage(@NotNull ConditionalDamage conditionalDamage,
                                        @NotNull List<String> description) {
        conditionalDamages.add(conditionalDamage);
        additionalModifierDescription.addAll(description);
    }

    protected void clearConditionalDamage() {
        conditionalDamages.clear();
        additionalModifierDescription.clear();
    }

    public @NotNull List<ConditionalDamage> getConditionalDamage() {
        return conditionalDamages;
    }

    public @NotNull ItemStack buildItem() {
        List<String> finalLore = buildAttributeLore();
        if (!additionalModifierDescription.isEmpty()) {
            finalLore.add("");
            finalLore.addAll(additionalModifierDescription);
        }
        if (spell != null) {
            finalLore.add("");
            finalLore.add(ChatColor.YELLOW + "Spell: " + spell.getDisplayName() + " " +
                    ChatColor.GOLD + ChatColor.BOLD + "RIGHT CLICK");
            finalLore.addAll(spell.getDescription());
            finalLore.addAll(spell.buildAttributes());
        }
        finalLore.add("");
        finalLore.add(rarity.displayName);
        itemMeta.setLore(finalLore);
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        if (itemMeta.getAttributeModifiers() != null && itemMeta.getAttributeModifiers().isEmpty())
            itemMeta.addAttributeModifier(Attribute.GENERIC_LUCK, new AttributeModifier(
                    new NamespacedKey(plugin, "dummy"),
                    0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ANY
            ));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public @NotNull List<String> buildAttributeLore() {
        final List<String> lore = new ArrayList<>();
        if (baseDamage.amount() != 0) {
            lore.add(ChatColor.GRAY + "Damage: " +
                    ChatColor.RED + "+" + baseDamage.amount());
        }
        final BrightItemAttribute[] attributes = BrightItemAttribute.values();
        for (BrightItemAttribute attribute : attributes) {
            long value = getAttribute(attribute);
            if (value == attribute.defaultValue) continue;
            lore.add(ChatColor.GRAY + attribute.displayName + ": " +
                    ChatColor.RED + "+" + value);
        }
        return lore;
    }

    public void setSpell(@Nullable BrightSpell spell) {
        this.spell = spell;
    }

    public @NotNull String getKey() {
        return nbt.getOrDefault(new NamespacedKey(plugin, KEY_ATTRIBUTE),
                PersistentDataType.STRING, "null");
    }

    public @Nullable BrightSpell getSpell() {
        return spell;
    }

    public @NotNull Rarity getRarity() {
        return rarity;
    }

    protected void setRarity(@NotNull Rarity rarity) {
        this.rarity = rarity;
    }

    public List<String> getAdditionalModifierDescription() {
        return additionalModifierDescription;
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
