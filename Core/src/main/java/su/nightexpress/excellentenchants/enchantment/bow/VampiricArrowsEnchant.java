package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

@NullMarked
public class VampiricArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    private Modifier healAmount;

    public VampiricArrowsEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.ARROW, new ArrowEffects(UniParticle.redstone(Color.RED, 1F)));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(20, 5));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.healAmount = Modifier.load(config, "Vampire.Heal_Amount",
            Modifier.addictive(1).perLevel(0.5).capacity(5),
            "Amount of hearts to be restore."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getHealAmount(level)));
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    @Override

    public EnchantPriority getShootPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level) {
        return event.getProjectile() instanceof Arrow;
    }

    @Override
    public void onHit(ProjectileHitEvent event, LivingEntity shooter, Arrow arrow, int level) {

    }

    @Override
    public void onDamage(EntityDamageByEntityEvent event, LivingEntity shooter, LivingEntity victim, Arrow arrow,
                         int level) {
        if (shooter.isDead() || shooter.getHealth() <= 0D) return;

        double healAmount = this.getHealAmount(level);
        if (healAmount <= 0D) return;

        double health = shooter.getHealth();
        double maxHealth = EntityUtil.getAttributeValue(shooter, Attribute.MAX_HEALTH);
        if (health >= maxHealth) return;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(shooter, healAmount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getPluginManager().callEvent(healthEvent);
        if (healthEvent.isCancelled()) return;

        shooter.setHealth(Math.min(maxHealth, health + healAmount));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(shooter.getEyeLocation(), 0.25f, 0.15f, 5);
        }
    }
}
