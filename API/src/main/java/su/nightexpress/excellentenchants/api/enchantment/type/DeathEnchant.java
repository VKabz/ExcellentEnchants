package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface DeathEnchant extends CustomEnchantment {

    boolean onDeath(EntityDeathEvent event, LivingEntity entity, ItemStack item, int level);

    EnchantPriority getDeathPriority();
}
