package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Головорез (лук/арбалет): попадание в голову наносит больше урона. Награда за меткость. */
@NullMarked
public class HeadshotEnchant extends GameEnchantment implements ArrowEnchant {

    private Modifier amount;
    private double   headZone;
    private double   minHeight;

    public HeadshotEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.amount = Modifier.load(config, "Headshot.Amount",
            Modifier.addictive(5).perLevel(15).capacity(80),
            "Extra damage (in percent) for a headshot."
        );
        this.headZone = ConfigValue.create("Headshot.Head_Zone",
            0.4D,
            "How far (in blocks) below the eye level still counts as head."
        ).read(config);
        this.minHeight = ConfigValue.create("Headshot.Min_Target_Height",
            1.2D,
            "Targets shorter than this have no 'head' (chickens, spiders, ...)."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.amount.getValue(level)));
    }

    @Override
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level) {
        return true; // Только пометить стрелу, чтобы onDamage нашёл зачарование.
    }

    @Override
    public void onHit(ProjectileHitEvent event, LivingEntity shooter, Arrow projectile, int level) {

    }

    @Override
    public void onDamage(EntityDamageByEntityEvent event, LivingEntity shooter, LivingEntity victim, Arrow projectile,
                         int level) {
        if (victim.getHeight() < this.minHeight) return;

        double hitY = projectile.getLocation().getY();
        double eyeY = victim.getLocation().getY() + victim.getEyeHeight();
        if (hitY < eyeY - this.headZone) return;

        double percent = this.amount.getValue(level);
        if (percent <= 0D) return;

        event.setDamage(event.getDamage() * (1D + percent / 100D));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CRIT).play(victim.getEyeLocation(), 0.3, 0.1, 20);
            if (shooter instanceof Player player) {
                VanillaSound.of(Sound.ENTITY_PLAYER_ATTACK_CRIT, 1F, 1.3F).play(player);
            }
        }
    }
}
