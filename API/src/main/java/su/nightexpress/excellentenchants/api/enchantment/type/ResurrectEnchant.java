package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface ResurrectEnchant extends CustomEnchantment {

    boolean onResurrect(EntityResurrectEvent event, LivingEntity entity, ItemStack item, int level);

    EnchantPriority getResurrectPriority();
}
