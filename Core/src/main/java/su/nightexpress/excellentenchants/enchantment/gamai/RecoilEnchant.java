package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.nio.file.Path;

/**
 * Боевая отдача (лук/арбалет): выстрел отбрасывает стрелка назад. Мобильность, а не урон.
 * Вверх толкает не больше чем на ~блок и не складывается с текущей скоростью, плюс кулдаун —
 * иначе выстрелами под себя (да ещё с мультизарядом) улетали на 7–10 блоков и ливали из боя.
 */
@NullMarked
public class RecoilEnchant extends GameEnchantment implements BowEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier strength;
    private Modifier maxVertical;
    private Modifier cooldown;

    public RecoilEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.strength = Modifier.load(config, "Recoil.Strength",
            Modifier.addictive(0.3).perLevel(0.2).capacity(2),
            "Recoil strength (scaled by the bow draw force)."
        );

        this.maxVertical = Modifier.load(config, "Recoil.Max_Vertical",
            Modifier.addictive(0.35).perLevel(0).capacity(0.35),
            "Max upward velocity from recoil (vanilla jump is 0.42). Doesn't stack with current velocity."
        );

        this.cooldown = Modifier.load(config, "Recoil.Cooldown",
            Modifier.addictive(1.0).perLevel(0).capacity(1.0),
            "Cooldown (in seconds). Also keeps Multishot from pushing once per arrow."
        );
    }

    @Override
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level) {
        Vector direction = event.getProjectile().getVelocity();
        if (direction.lengthSquared() < 1.0E-6) return false;
        if (!this.cooldowns.tryUse(shooter, this.cooldown.getValue(level))) return false;

        double power = this.strength.getValue(level) * Math.max(0.3F, event.getForce());
        Vector push = direction.normalize().multiply(-power);
        double pushY = Math.min(Math.max(push.getY(), 0.15D), this.maxVertical.getValue(level));

        Vector velocity = shooter.getVelocity();
        velocity.setX(velocity.getX() + push.getX());
        velocity.setZ(velocity.getZ() + push.getZ());
        velocity.setY(Math.max(velocity.getY(), pushY));
        shooter.setVelocity(velocity);

        if (this.hasVisualEffects()) {
            VanillaSound.of(Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.5F, 1.5F).play(shooter.getLocation());
        }
        return true;
    }
}
