package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface BowEnchant extends CustomEnchantment {

    boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level);

    EnchantPriority getShootPriority();
}
