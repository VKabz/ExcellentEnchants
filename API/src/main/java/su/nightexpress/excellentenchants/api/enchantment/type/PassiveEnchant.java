package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface PassiveEnchant extends CustomEnchantment {

    boolean onTrigger(LivingEntity entity, ItemStack item, int level);
}
