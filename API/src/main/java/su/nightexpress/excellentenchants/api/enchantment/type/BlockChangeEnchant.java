package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface BlockChangeEnchant extends CustomEnchantment {

    boolean onBlockChange(EntityChangeBlockEvent event, LivingEntity entity, ItemStack itemStack, int level);
}
