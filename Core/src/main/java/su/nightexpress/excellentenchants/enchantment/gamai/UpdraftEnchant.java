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

/** Ветра пустыни (щит): удар, принятый на щит, подбрасывает атакующего в воздух. */
@NullMarked
public class UpdraftEnchant extends GameEnchantment implements DefendEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier power;
    private Modifier cooldown;

    public UpdraftEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.power = Modifier.load(config, "Updraft.Power",
            Modifier.addictive(0.5).perLevel(0.25).capacity(2),
            "Upward launch power."
        );
        this.cooldown = Modifier.load(config, "Updraft.Cooldown",
            Modifier.addictive(13).perLevel(-1).capacity(60),
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
        if (!this.cooldowns.tryUse(victim, this.cooldown.getValue(level))) return false;

        Vector velocity = damager.getVelocity();
        velocity.setY(this.power.getValue(level));
        damager.setVelocity(velocity);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.GUST).play(damager.getLocation(), 0.3, 0.1, 3);
            VanillaSound.of(Sound.ENTITY_BREEZE_JUMP, 1F, 0.8F).play(damager.getLocation());
        }
        return true;
    }
}
