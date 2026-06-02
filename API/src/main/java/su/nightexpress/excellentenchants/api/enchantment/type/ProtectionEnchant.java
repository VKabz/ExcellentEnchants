package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface ProtectionEnchant extends CustomEnchantment {

    boolean onProtection(EntityDamageEvent event, DamageBonus damageBonus, LivingEntity entity, ItemStack itemStack,
                         int level);

    DamageBonus getDamageBonus();

    EnchantPriority getProtectionPriority();
}
