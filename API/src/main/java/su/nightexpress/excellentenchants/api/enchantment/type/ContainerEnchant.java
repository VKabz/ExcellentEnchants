package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface ContainerEnchant extends CustomEnchantment {

    boolean onClick(InventoryClickEvent event, Player player, ItemStack itemStack, int level);

    EnchantPriority getClickPriority();
}
