package su.nightexpress.excellentenchants.manager;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface EnchantUsage<T extends CustomEnchantment> {

    boolean useEnchant(ItemStack item, T enchant, int level);
}
