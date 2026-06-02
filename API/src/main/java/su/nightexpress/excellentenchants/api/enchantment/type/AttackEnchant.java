package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface AttackEnchant extends CustomEnchantment {

    boolean onAttack(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim, ItemStack weapon,
                     int level);

    EnchantPriority getAttackPriority();
}
