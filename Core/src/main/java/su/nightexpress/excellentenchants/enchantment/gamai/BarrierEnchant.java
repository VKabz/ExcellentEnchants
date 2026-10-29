package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/**
 * Барьер (щит в левой руке): раз в секунду мобы рядом отлетают, а снаряды мобов рядом
 * исчезают; снаряд, всё-таки долетевший до носителя, гасится. Игроков не трогает. Замена Щита Отражения.
 *
 * Скан идёт только вокруг носителей книги и только раз в секунду — тот же тик, что у пассивок.
 */
@NullMarked
public class BarrierEnchant extends GameEnchantment implements PassiveEnchant, GamaiHooks {

    private Modifier radius;
    private double   strength;
    private double   lift;
    private boolean  destroyPlayerProjectiles;

    public BarrierEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PERIODIC, Period.ofSeconds(1));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.radius = Modifier.load(config, "Barrier.Radius",
            Modifier.addictive(1.5).perLevel(1.5).capacity(10),
            "Radius (in blocks). Default: 3 / 4.5."
        );
        this.strength = ConfigValue.create("Barrier.Knockback_Strength",
            0.8D,
            "Horizontal knockback strength for mobs."
        ).read(config);
        this.lift = ConfigValue.create("Barrier.Knockback_Lift",
            0.4D,
            "Vertical knockback for mobs."
        ).read(config);
        this.destroyPlayerProjectiles = ConfigValue.create("Barrier.Destroy_Player_Projectiles",
            false,
            "Also destroy arrows and projectiles shot by players. Off: the book is against mobs, PvP stays as is."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(this.radius.getValue(level)));
    }

    @Override
    public boolean onTrigger(LivingEntity entity, ItemStack item, int level) {
        if (!isInOffHand(entity, item)) return false;

        double radius = this.radius.getValue(level);
        if (radius <= 0D) return false;

        boolean worked = false;
        for (Entity nearby : entity.getNearbyEntities(radius, radius, radius)) {
            if (nearby instanceof Projectile projectile) {
                if (this.shouldDestroy(projectile, entity)) {
                    this.destroy(projectile);
                    worked = true;
                }
                continue;
            }
            if (!(nearby instanceof LivingEntity mob) || !(nearby instanceof Enemy) || nearby instanceof Boss) continue;
            if (mob.getLocation().distanceSquared(entity.getLocation()) > radius * radius) continue;

            Vector direction = GamaiUtils.horizontal(mob.getLocation().toVector().subtract(entity.getLocation().toVector()));
            if (direction == null) continue;

            mob.setVelocity(direction.multiply(this.strength).setY(this.lift));
            worked = true;
        }

        if (worked && this.hasVisualEffects()) {
            UniParticle.of(Particle.ENCHANTED_HIT).play(entity.getLocation().add(0, 1, 0), radius / 2D, 0.2, 15);
        }
        return worked;
    }

    @Override
    public boolean onProjectileTaken(ProjectileHitEvent event, LivingEntity entity, ItemStack item, int level) {
        if (!isInOffHand(entity, item)) return false;

        Projectile projectile = event.getEntity();
        if (!this.shouldDestroy(projectile, entity)) return false;

        event.setCancelled(true);
        this.destroy(projectile);
        if (this.hasVisualEffects()) {
            VanillaSound.of(Sound.ITEM_SHIELD_BLOCK, 0.7F, 1.5F).play(entity.getLocation());
        }
        return true;
    }

    private boolean shouldDestroy(Projectile projectile, LivingEntity holder) {
        if (projectile.getShooter() == holder) return false;
        if (projectile instanceof AbstractArrow arrow && arrow.isInBlock()) return false; // Уже воткнулась.
        if (projectile.getShooter() instanceof Player) return this.destroyPlayerProjectiles;
        return true;
    }

    private void destroy(Projectile projectile) {
        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CRIT).play(projectile.getLocation(), 0.1, 0.05, 6);
        }
        projectile.remove();
    }

    private static boolean isInOffHand(LivingEntity entity, ItemStack item) {
        EntityEquipment equipment = entity.getEquipment();
        return equipment != null && equipment.getItemInOffHand().equals(item);
    }
}
