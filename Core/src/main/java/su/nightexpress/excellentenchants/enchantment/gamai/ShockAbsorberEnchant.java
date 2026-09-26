package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
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
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Амортизаторы (ботинки): часть урона от падения не наносится, а лечит. */
@NullMarked
public class ShockAbsorberEnchant extends GameEnchantment implements ProtectionEnchant {

    private Modifier amount;

    public ShockAbsorberEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.amount = Modifier.load(config, "ShockAbsorber.Amount",
            Modifier.addictive(0).perLevel(25).capacity(90),
            "Percent of fall damage converted into healing."
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
        if (event.getDamageSource().getDamageType() != DamageType.FALL) return false;

        double percent = this.amount.getValue(level);
        if (percent <= 0D) return false;

        double heal = event.getDamage() * percent / 100D;
        damageBonus.addPenalty(percent, 100D);

        // Лечим следующим тиком — после того как остаток урона уже снят.
        this.plugin.runTask(() -> {
            if (entity.isDead() || !entity.isValid()) return;

            double max = EntityUtil.getAttributeValue(entity, Attribute.MAX_HEALTH);
            entity.setHealth(Math.min(max, entity.getHealth() + heal));

            if (this.hasVisualEffects()) {
                UniParticle.of(Particle.HEART).play(entity.getLocation().add(0, 1, 0), 0.3, 0.1, 4);
            }
        });
        return true;
    }
}
