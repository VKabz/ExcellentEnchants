package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface MiningEnchant extends CustomEnchantment {

    boolean onBreak(BlockBreakEvent event, LivingEntity player, ItemStack item, int level);

    EnchantPriority getBreakPriority();
}
