package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantPriority;

@NullMarked
public interface TridentEnchant extends ProjectileEnchant<Trident> {

    boolean onLaunch(ProjectileLaunchEvent event, LivingEntity shooter, ItemStack trident, int level);

    EnchantPriority getLaunchPriority();
}
