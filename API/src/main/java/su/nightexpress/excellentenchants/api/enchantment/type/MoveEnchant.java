package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface MoveEnchant extends CustomEnchantment {

    boolean onMove(PlayerMoveEvent event, Player player, ItemStack itemStack, int level);

    EnchantPriority getMovePriority();
}
