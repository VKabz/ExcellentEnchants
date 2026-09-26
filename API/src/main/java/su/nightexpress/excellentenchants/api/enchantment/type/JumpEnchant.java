package su.nightexpress.excellentenchants.api.enchantment.type;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

/**
 * Зачарование, срабатывающее при прыжке игрока (gamai.ru). Paper-only событие.
 */
@NullMarked
public interface JumpEnchant extends CustomEnchantment {

    boolean onJump(PlayerJumpEvent event, Player player, ItemStack item, int level);

    EnchantPriority getJumpPriority();
}
