package me.bright.itemNSpell.main;

import me.bright.brightrpg.BrightRPG;
import me.bright.brightrpg.BrightStat;
import me.bright.brightrpg.BrightStatModifier;
import me.bright.damage.BrightDamage;
import me.bright.enchant.BrightEnchant;
import me.bright.enchant.BrightEnchants;
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

    private static final BrightRPG plugin = BrightRPG.getPlugin();
    private static final NamespacedKey KEY_NAMESPACE = new NamespacedKey(plugin, "KEY"),
            ENCHANTS_NAMESPACE = new NamespacedKey(plugin, "ENCHANTS");
    public static final String DEFAULT_KEY = "NULL";

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private final PersistentDataContainer nbt;
    private final String name;
    private Rarity rarity = Rarity.COMMON;
    private BrightSpell spell = null;
    private List<String> additionalModifierDescription = new ArrayList<>();

    protected BrightItem(@NotNull String key, @NotNull Material material, @NotNull String name) {
        super(false);
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        this.nbt = itemMeta.getPersistentDataContainer();
        this.name = name;
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        nbt.set(KEY_NAMESPACE, PersistentDataType.STRING, key);
    }

    public static @Nullable BrightItem fromItemStack(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) return null;
        PersistentDataContainer nbt = itemMeta.getPersistentDataContainer();
        String givenKey = nbt.getOrDefault(KEY_NAMESPACE,
                PersistentDataType.STRING, DEFAULT_KEY);

        BrightItem item = (givenKey.equals(DEFAULT_KEY)) ?
                BrightItems.getVanillaItem(itemStack) :
                BrightItems.getCustomItem(givenKey);
        if (item == null) {
            return null;
        }
        var itemPdc = item.getItemMeta().getPersistentDataContainer();
        itemMeta.getPersistentDataContainer().copyTo(itemPdc, true);
        item.loadEnchantStats();
        return item;
    }

    public void addEnchant(BrightEnchant enchant, int level) {
        PersistentDataContainer enchants =
                nbt.getOrDefault(ENCHANTS_NAMESPACE,
                        PersistentDataType.TAG_CONTAINER,
                        nbt.getAdapterContext().newPersistentDataContainer());
        enchants.set(new NamespacedKey(plugin, enchant.getKey()), PersistentDataType.INTEGER, level);
        nbt.set(ENCHANTS_NAMESPACE, PersistentDataType.TAG_CONTAINER, enchants);
    }

    public void removeEnchant(BrightEnchant enchant) {
        PersistentDataContainer enchants = nbt.get(ENCHANTS_NAMESPACE, PersistentDataType.TAG_CONTAINER);
        if (enchants == null) {
            return;
        }
        enchants.remove(new NamespacedKey(plugin, enchant.getKey()));
        nbt.set(ENCHANTS_NAMESPACE, PersistentDataType.TAG_CONTAINER, enchants);
    }

    public Long hasEnchant(BrightEnchant enchant) {
        PersistentDataContainer enchants =
                nbt.get(ENCHANTS_NAMESPACE, PersistentDataType.TAG_CONTAINER);
        if (enchants == null) {
            return null;
        }
        return enchants.get(new NamespacedKey(plugin, enchant.getKey()), PersistentDataType.LONG);
    }

    public String getName() {
        return name;
    }

    private void loadEnchantStats() {
        PersistentDataContainer enchants =
                nbt.get(ENCHANTS_NAMESPACE, PersistentDataType.TAG_CONTAINER);
        if (enchants == null || enchants.isEmpty()) {
            return;
        }
        BrightStatModifier mergedEnchants = new BrightStatModifier(false);
        for (NamespacedKey enchantKey : enchants.getKeys()) {
            BrightEnchant enchant = BrightEnchants.fromKey(enchantKey.getKey().toUpperCase());
            if (enchant == null) {
                continue;
            }
            int level = enchants.getOrDefault(enchantKey, PersistentDataType.INTEGER, 0);
            BrightStatModifier enchantMod = enchant.getModifier(level);
            for (BrightStat stat : BrightStat.values()) {
                double newFlat = mergedEnchants.getStatFlatMod(stat) + enchantMod.getStatFlatMod(stat),
                        newAdd = mergedEnchants.getStatAddMod(stat) + enchantMod.getStatAddMod(stat),
                        newMul = (1 + mergedEnchants.getStatMulMod(stat) / 100) * (1 + enchantMod.getStatMulMod(stat) / 100);
                newMul--;
                newMul *= 100;
                mergedEnchants.setStatFlatMod(stat, newFlat);
                mergedEnchants.setStatAddMod(stat, newAdd);
                mergedEnchants.setStatMulMod(stat, newMul);
            }
        }
        for (BrightStat stat : BrightStat.values()) {
            double finalValue = getStatFlatMod(stat) + mergedEnchants.getStatFlatMod(stat);
            finalValue *= (1 + mergedEnchants.getStatAddMod(stat) / 100);
            finalValue *= (1 + mergedEnchants.getStatMulMod(stat) / 100);
            setStatFlatMod(stat, finalValue);
        }
    }

    public @NotNull ItemStack buildItem() {
        buildLore();
        hideFlags();
        itemMeta.setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void buildLore() {
        List<String> finalLore = buildStatsLore();
        if (!additionalModifierDescription.isEmpty()) {
            finalLore.add("");
            finalLore.addAll(additionalModifierDescription);
        }
        if (spell != null) {
            finalLore.add("");
            finalLore.addAll(buildSpellLore());
        }
        finalLore.add("");
        finalLore.add(rarity.displayName);
        itemMeta.setLore(finalLore);
    }

    private @NotNull List<String> buildSpellLore() {
        final List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Spell: " + spell.getDisplayName() + " " +
                ChatColor.GOLD + ChatColor.BOLD + "RIGHT CLICK");
        lore.addAll(spell.getDescription());
        lore.addAll(spell.buildAttributes());
        return lore;
    }

    private @NotNull List<String> buildStatsLore() {
        final List<String> lore = new ArrayList<>();

        for (BrightStat stat : BrightStat.values()) {
            double value = getStatFlatMod(stat);
            if (value == 0) continue;
            StringBuilder line = new StringBuilder()
                    .append(ChatColor.GRAY).append(stat.displayName).append(": ")
                    .append(stat.color);
            if (value > 0)
                line.append("+");
            line.append((long) value);
            lore.add(line.toString());
        }
        return lore;
    }

    private void hideFlags() {
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        if (itemMeta.getAttributeModifiers() == null) {
            return;
        }
        if (!itemMeta.getAttributeModifiers().isEmpty()) {
            return;
        }
        var dummyAttributeModifier = new AttributeModifier(new NamespacedKey(plugin, "dummy"),
                0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY);
        itemMeta.addAttributeModifier(Attribute.GENERIC_LUCK, dummyAttributeModifier);
    }

    protected void setSpell(@Nullable BrightSpell spell) {
        this.spell = spell;
    }

    public @NotNull String getKey() {
        return nbt.getOrDefault(KEY_NAMESPACE,
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
