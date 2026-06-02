package su.nightexpress.excellentenchants.api.enchantment.component.impl;

import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionEffects;
import su.nightexpress.nightcore.config.FileConfig;

@NullMarked
public class EffectComponent implements EnchantComponent<PotionEffects> {

    @Override
    public String getName() {
        return "potion_effect";
    }

    @Override
    public PotionEffects read(FileConfig config, PotionEffects defaultValue) {
        PotionEffectType effectType = defaultValue.getType();
        boolean isPassive = defaultValue.isPermanent();
        Modifier durationMod = Modifier.load(config, "PotionEffect.Duration", defaultValue.getDuration(),
            "Effect duration (in seconds).");
        Modifier amplifierMod = Modifier.load(config, "PotionEffect.Amplifier", defaultValue.getAmplifier(),
            "Effect amplifier.");

        return new PotionEffects(effectType, durationMod, amplifierMod, isPassive);
    }
}
