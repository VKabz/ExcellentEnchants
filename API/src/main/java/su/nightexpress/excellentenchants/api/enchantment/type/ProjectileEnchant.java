package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface ProjectileEnchant<T extends AbstractArrow> extends CustomEnchantment {

    void onHit(ProjectileHitEvent event, LivingEntity shooter, T projectile, int level);

    void onDamage(EntityDamageByEntityEvent event, LivingEntity shooter, LivingEntity victim, T projectile, int level);
}
