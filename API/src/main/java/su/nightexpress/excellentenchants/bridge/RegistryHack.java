package su.nightexpress.excellentenchants.bridge;

import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.item.ItemSet;

@NullMarked
public interface RegistryHack {

    void unfreezeRegistry();

    void freezeRegistry();

    void addExclusives(CustomEnchantment enchantment);

    void createItemsSet(ItemSet itemSet);

    @Nullable
    Enchantment registerEnchantment(EnchantCatalogEntry entry, DistributionSettings settings);
}
