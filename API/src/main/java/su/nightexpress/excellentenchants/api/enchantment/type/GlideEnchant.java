package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

/**
 * Зачарование, срабатывающее при старте/остановке полёта на элитрах (gamai.ru).
 */
@NullMarked
public interface GlideEnchant extends CustomEnchantment {

    boolean onGlide(EntityToggleGlideEvent event, Player player, ItemStack item, int level);

    EnchantPriority getGlidePriority();
}
