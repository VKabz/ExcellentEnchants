package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.DefendEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Отталкивающий щит: удар, принятый на щит, отбрасывает атакующего. */
@NullMarked
public class RepulseEnchant extends GameEnchantment implements DefendEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier strength;
    private Modifier cooldown;

    public RepulseEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.strength = Modifier.load(config, "Repulse.Strength",
            Modifier.addictive(0.6).perLevel(0.3).capacity(2.5),
            "Knockback strength."
        );
        this.cooldown = Modifier.load(config, "Repulse.Cooldown",
            Modifier.addictive(10).perLevel(-1).capacity(60),
            "Cooldown (in seconds)."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format(this.cooldown.getValue(level)));
    }

    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onProtect(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                             ItemStack weapon, int level) {
        if (!GamaiUtils.isBlockedByShield(victim, event)) return false;

        Vector direction = GamaiUtils.horizontal(damager.getLocation().toVector().subtract(victim.getLocation().toVector()));
        if (direction == null) direction = GamaiUtils.horizontal(victim.getLocation().getDirection());
        if (direction == null) return false;

        if (!this.cooldowns.tryUse(victim, this.cooldown.getValue(level))) return false;

        damager.setVelocity(direction.multiply(this.strength.getValue(level)).setY(0.35));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CLOUD).play(damager.getLocation().add(0, 1, 0), 0.4, 0.1, 20);
            VanillaSound.of(Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.8F, 1.2F).play(victim.getLocation());
        }
        return true;
    }
}
