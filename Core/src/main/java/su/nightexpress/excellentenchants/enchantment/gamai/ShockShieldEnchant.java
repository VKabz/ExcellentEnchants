package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Location;
import org.bukkit.Particle;
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
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Электрошок (щит): удар, принятый на щит, с шансом бьёт атакующего молнией. */
@NullMarked
public class ShockShieldEnchant extends GameEnchantment implements DefendEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier damage;
    private Modifier cooldown;

    public ShockShieldEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(15, 10));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.damage = Modifier.load(config, "ShockShield.Damage",
            Modifier.addictive(2).perLevel(1).capacity(8),
            "Lightning damage dealt to the attacker."
        );
        this.cooldown = Modifier.load(config, "ShockShield.Cooldown",
            Modifier.addictive(3),
            "Cooldown (in seconds)."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE, level -> NumberUtil.format(this.damage.getValue(level)));
    }

    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onProtect(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                             ItemStack weapon, int level) {
        if (!GamaiUtils.isBlockedByShield(victim, event)) return false;
        if (!this.cooldowns.tryUse(victim, this.cooldown.getValue(level))) return false;

        Location location = damager.getLocation();
        damager.getWorld().strikeLightningEffect(location);
        GamaiUtils.retaliate(damager, victim, this.damage.getValue(level));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.ELECTRIC_SPARK).play(LocationUtil.setCenter3D(location.clone()), 0.75, 0.05, 80);
        }
        return true;
    }
}
