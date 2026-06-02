package su.nightexpress.excellentenchants.api.enchantment.component;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.component.impl.*;
import su.nightexpress.excellentenchants.api.enchantment.meta.*;
import su.nightexpress.nightcore.config.FileConfig;

@NullMarked
public interface EnchantComponent<T> {

    EnchantComponent<Probability>   PROBABILITY   = new ProbabilityComponent();
    EnchantComponent<PotionEffects> POTION_EFFECT = new EffectComponent();
    EnchantComponent<ArrowEffects>  ARROW         = new ArrowComponent();
    EnchantComponent<Period>        PERIODIC      = new PeriodComponent();
    EnchantComponent<Charges>       CHARGES       = new ChargesComponent();

    String getName();

    T read(FileConfig config, @NonNull T defaultValue);
}
