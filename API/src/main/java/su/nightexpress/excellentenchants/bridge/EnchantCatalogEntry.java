package su.nightexpress.excellentenchants.bridge;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;

@NullMarked
public interface EnchantCatalogEntry {

    String getId();

    NamespacedKey getKey();

    EnchantDefinition getDefinition();

    EnchantDistribution getDistribution();

    boolean isCurse();
}
