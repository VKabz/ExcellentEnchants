package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.Particle;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.nightcore.util.wrapper.UniParticle;

@NullMarked
public class ArrowEffects {

    private final UniParticle trailEffect;

    public ArrowEffects(UniParticle trailEffect) {
        this.trailEffect = trailEffect;
    }

    public static ArrowEffects basic(Particle particle) {
        return new ArrowEffects(UniParticle.of(particle));
    }

    public UniParticle getTrailEffect() {
        return this.trailEffect;
    }
}
