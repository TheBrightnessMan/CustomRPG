package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStatModifier;
import me.bright.brightrpg.BrightStats;
import me.bright.damage.BrightDamage;
import me.bright.entity.BrightEntity;
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
import java.util.List;

public abstract class BrightItem extends BrightStatModifier {

    private final BrightRPG plugin;

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private final PersistentDataContainer nbt;

    private final String name;
    public static final String KEY_ATTRIBUTE = "KEY", DEFAULT_KEY = "null";
    private Rarity rarity = Rarity.COMMON;
    private BrightSpell spell = null;
    private double baseDamage = 0;
    private List<String> additionalModifierDescription = new ArrayList<>();

    protected BrightItem(@NotNull String key, @NotNull Material material, @NotNull String name) {
        super(false);
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

        return (givenKey.equals(DEFAULT_KEY)) ?
                BrightItems.getVanillaItem(itemStack) :
                BrightItems.getCustomItem(givenKey);
    }

    public String getName() {
        return name;
    }

    protected void setBaseDamage(double baseDamage) {
        this.baseDamage = baseDamage;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    public @NotNull ItemStack buildItem() {
        List<String> finalLore = buildStatsLore();
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

    public @NotNull List<String> buildStatsLore() {
        final List<String> lore = new ArrayList<>();
        if (baseDamage != 0) {
            lore.add(ChatColor.GRAY + "Damage: " +
                    ChatColor.DARK_RED + "+" + baseDamage);
        }

        for (BrightStats stat : BrightStats.values()) {
            double value = getStatFlatMod(stat);
            if (value == 0) continue;
            StringBuilder line = new StringBuilder().append(ChatColor.GRAY).append(stat.displayName).append(": ")
                    .append(stat.color);
            if (value > 0)
                line.append("+");
            line.append(value);
            lore.add(line.toString());
        }
        return lore;
    }

    protected void setSpell(@Nullable BrightSpell spell) {
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

    protected void setAdditionalModifierDescription(@NotNull List<String> description) {
        this.additionalModifierDescription = description;
    }

    protected void setCustomModelData(int val) {
        itemMeta.setCustomModelData(val);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public @NotNull List<String> getAdditionalModifierDescription() {
        return this.additionalModifierDescription;
    }

    public List<BrightDamage> onMeleeHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        // No op
        return damages;
    }

    public List<BrightDamage> onArrowHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        // No op
        return damages;
    }

    public List<BrightDamage> onSpellHit(BrightEntity self, BrightEntity target, List<BrightDamage> damages) {
        // No op
        return damages;
    }

    public List<BrightDamage> onHurt(BrightEntity attacker, BrightEntity self, List<BrightDamage> damages) {
        // No op
        return damages;
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
