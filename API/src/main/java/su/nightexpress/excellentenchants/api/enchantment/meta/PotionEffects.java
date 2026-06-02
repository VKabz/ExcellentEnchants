package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.Modifier;

@NullMarked
public class PotionEffects {

    private final PotionEffectType type;
    private final Modifier         duration;
    private final Modifier         amplifier;
    private final boolean          permanent;

    public PotionEffects(PotionEffectType type, Modifier duration, Modifier amplifier, boolean permanent) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.permanent = permanent;
    }

    public static Modifier permanentDuration(PotionEffectType type) {
        int duration = type == PotionEffectType.NIGHT_VISION ? 45 : 10;
        return Modifier.addictive(duration).capacity(duration).build();
    }

    public static PotionEffects permanent(PotionEffectType type) {
        Modifier amplifier = Modifier.addictive(0).perLevel(1).capacity(5).build();

        return permanent(type, amplifier);
    }

    public static PotionEffects permanent(PotionEffectType type, Modifier.Builder amplifier) {
        return permanent(type, amplifier.build());
    }

    public static PotionEffects permanent(PotionEffectType type, Modifier amplifier) {
        return new PotionEffects(type, permanentDuration(type), amplifier, true);
    }

    public static PotionEffects temporal(PotionEffectType type, Modifier.Builder duration) {
        return temporal(type, duration, Modifier.addictive(0).perLevel(1).capacity(5));
    }

    public static PotionEffects temporal(PotionEffectType type, Modifier.Builder duration, Modifier.Builder amplifier) {
        return temporal(type, duration.build(), amplifier.build());
    }

    public static PotionEffects temporal(PotionEffectType type, Modifier duration, Modifier amplifier) {
        return new PotionEffects(type, duration, amplifier, false);
    }

    public Modifier getDuration() {
        return this.duration;
    }

    public Modifier getAmplifier() {
        return this.amplifier;
    }

    public boolean isPermanent() {
        return this.permanent;
    }

    public PotionEffectType getType() {
        return this.type;
    }

    public int getDuration(int level) {
        return (int) (this.duration.getValue(level) * 20);
    }

    public int getAmplifier(int level) {
        return (int) this.amplifier.getValue(level);
    }

    public PotionEffect createEffect(int level, boolean particles) {
        int duration = this.getDuration(level);
        int amplifier = Math.max(0, this.getAmplifier(level) - 1);

        return new PotionEffect(this.type, duration, amplifier, this.permanent, particles);
    }

    public boolean addEffect(LivingEntity target, int level, boolean particles) {
        return target.addPotionEffect(this.createEffect(level, particles));
    }

    public boolean addEffect(Arrow arrow, int level, boolean particles) {
        return arrow.addCustomEffect(this.createEffect(level, particles), true);
    }
}
