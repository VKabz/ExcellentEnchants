package su.nightexpress.excellentenchants.api;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;


@FunctionalInterface
@NullMarked
public interface EnchantKeyFactory {

    NamespacedKey create(String value);
}
