package su.nightexpress.excellentenchants.api.enchantment.component.impl;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

@NullMarked
public class ArrowComponent implements EnchantComponent<ArrowEffects> {

    @Override
    public String getName() {
        return "arrow";
    }

    @Override
    public ArrowEffects read(FileConfig config, ArrowEffects defaultValue) {
        UniParticle effect = ConfigValue.create("ArrowEffects.Trail", UniParticle::read, defaultValue.getTrailEffect(),
            "Sets projectile particle trail effect."
        ).read(config);

        return new ArrowEffects(effect);
    }
}
