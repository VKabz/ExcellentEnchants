package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;

@NullMarked
public record EnchantContext(String id,
                             Enchantment enchantment,
                             EnchantDefinition definition,
                             EnchantDistribution distribution,
                             boolean curse) {
}
