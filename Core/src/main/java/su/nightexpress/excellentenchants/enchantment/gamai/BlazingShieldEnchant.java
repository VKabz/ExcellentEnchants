package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.DefendEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Пылающий щит: удар, принятый на щит, с шансом поджигает атакующего. */
@NullMarked
public class BlazingShieldEnchant extends GameEnchantment implements DefendEnchant {

    private Modifier fireDuration;

    public BlazingShieldEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(20, 10));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.fireDuration = Modifier.load(config, "BlazingShield.Duration",
            Modifier.addictive(2).perLevel(1).capacity(8),
            "Fire duration (in seconds)."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DURATION, level -> NumberUtil.format(this.fireDuration.getValue(level)));
    }

    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onProtect(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                             ItemStack weapon, int level) {
        if (!GamaiUtils.isBlockedByShield(victim, event)) return false;

        int ticks = (int) (this.fireDuration.getValue(level) * 20);
        if (damager.getFireTicks() >= ticks) return false;

        damager.setFireTicks(ticks);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.FLAME).play(damager.getEyeLocation(), 0.4, 0.1, 25);
            VanillaSound.of(Sound.ITEM_FIRECHARGE_USE).play(victim.getLocation());
        }
        return true;
    }
}
