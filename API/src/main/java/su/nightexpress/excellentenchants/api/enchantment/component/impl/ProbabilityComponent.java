package su.nightexpress.excellentenchants.api.enchantment.component.impl;


import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.nightcore.config.FileConfig;

@NullMarked
public class ProbabilityComponent implements EnchantComponent<Probability> {

    @Override
    public String getName() {
        return "probability";
    }

    @Override
    public Probability read(FileConfig config, Probability defaultValue) {
        Modifier triggerChance = Modifier.load(config, "Probability.Trigger_Chance", defaultValue.getTriggerChance());

        return new Probability(triggerChance);
    }
}
