package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface InventoryEnchant extends CustomEnchantment {

    boolean onDeath(PlayerDeathEvent event, Player player, ItemStack itemStack, int level);
}
