package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface BlockDropEnchant extends CustomEnchantment {

    boolean onDrop(BlockDropItemEvent event, LivingEntity player, ItemStack item, int level);

    EnchantPriority getDropPriority();
}
