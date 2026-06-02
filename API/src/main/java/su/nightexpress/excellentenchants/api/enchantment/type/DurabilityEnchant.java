package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface DurabilityEnchant extends CustomEnchantment {

    boolean onItemDamage(PlayerItemDamageEvent event, Player player, ItemStack itemStack, int level);

    EnchantPriority getItemDamagePriority();
}
