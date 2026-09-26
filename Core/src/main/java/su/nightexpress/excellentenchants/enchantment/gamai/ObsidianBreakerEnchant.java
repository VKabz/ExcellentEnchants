package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

/** Обсидиановое уничтожение (кирка): с шансом ломает обсидиан одним ударом. */
@NullMarked
public class ObsidianBreakerEnchant extends InstantBreakEnchant {

    public ObsidianBreakerEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(5, 15));
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override
    protected boolean isTarget(Material material) {
        return material == Material.OBSIDIAN || material == Material.CRYING_OBSIDIAN;
    }
}
