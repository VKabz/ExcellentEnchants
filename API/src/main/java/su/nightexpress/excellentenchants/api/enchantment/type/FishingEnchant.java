package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface FishingEnchant extends CustomEnchantment {

    boolean onFishing(PlayerFishEvent event, ItemStack item, int level);

    EnchantPriority getFishingPriority();
}
