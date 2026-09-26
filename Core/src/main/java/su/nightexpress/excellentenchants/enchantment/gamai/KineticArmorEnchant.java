package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.damage.DamageBonusType;
import su.nightexpress.excellentenchants.api.enchantment.type.ProtectionEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Кинетическая энергия (элитры): снижает урон от столкновения со стеной на лету. */
@NullMarked
public class KineticArmorEnchant extends GameEnchantment implements ProtectionEnchant {

    private Modifier amount;

    public KineticArmorEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.amount = Modifier.load(config, "KineticArmor.Amount",
            Modifier.addictive(10).perLevel(30).capacity(100),
            "Percent of fly-into-wall damage negated. 100 = full immunity."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.amount.getValue(level)));
    }

    @Override
    public EnchantPriority getProtectionPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public DamageBonus getDamageBonus() {
        return new DamageBonus(DamageBonusType.MULTIPLIER);
    }

    @Override
    public boolean onProtection(EntityDamageEvent event, DamageBonus damageBonus, LivingEntity entity,
                                ItemStack itemStack, int level) {
        if (event.getDamageSource().getDamageType() != DamageType.FLY_INTO_WALL) return false;

        double percent = this.amount.getValue(level);
        if (percent <= 0D) return false;

        if (percent >= 100D) {
            event.setCancelled(true);
        }
        else {
            damageBonus.addPenalty(percent, 100D);
        }

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CLOUD).play(entity.getLocation().add(0, 1, 0), 0.5, 0.1, 15);
        }
        return true;
    }
}
