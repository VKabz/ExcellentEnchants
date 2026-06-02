package su.nightexpress.excellentenchants.enchantment;


import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.nio.file.Path;

import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface EnchantFactory<T extends CustomEnchantment> {

    T create(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context);
}
