package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

/** Адреналин (щит): успешный блок даёт короткий прилив силы. */
@NullMarked
public class AdrenalineEnchant extends GameEnchantment implements DefendEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier duration;
    private Modifier cooldown;

    public AdrenalineEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.duration = Modifier.load(config, "Adrenaline.Duration",
            Modifier.addictive(1.5).perLevel(1).capacity(8),
            "Strength I duration (in seconds)."
        );
        this.cooldown = Modifier.load(config, "Adrenaline.Cooldown",
            Modifier.addictive(10),
            "Cooldown (in seconds)."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DURATION, level -> NumberUtil.format(this.duration.getValue(level)));
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

        int ticks = (int) (this.duration.getValue(level) * 20);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, 0, true, true));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CRIT).play(victim.getEyeLocation(), 0.4, 0.1, 20);
            VanillaSound.of(Sound.ITEM_SHIELD_BLOCK, 1F, 1.4F).play(victim.getLocation());
        }
        return true;
    }
}
