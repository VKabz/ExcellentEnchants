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
import su.nightexpress.excellentenchants.api.enchantment.type.DefendEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Отражение (щит): часть заблокированного урона возвращается атакующему. */
@NullMarked
public class ReflectionEnchant extends GameEnchantment implements DefendEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier amount;
    private Modifier cooldown;

    public ReflectionEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.amount = Modifier.load(config, "Reflection.Amount",
            Modifier.addictive(5).perLevel(10).capacity(60),
            "Percent of the blocked damage returned to the attacker."
        );
        this.cooldown = Modifier.load(config, "Reflection.Cooldown",
            Modifier.addictive(8),
            "Cooldown (in seconds)."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.amount.getValue(level)));
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
        if (!this.cooldowns.isReady(victim)) return false;

        // getDamage() — «сырой» урон до блока, финальный после щита равен нулю.
        double reflected = event.getDamage() * this.amount.getValue(level) / 100D;
        if (reflected <= 0D) return false;

        this.cooldowns.start(victim, this.cooldown.getValue(level));
        GamaiUtils.retaliate(damager, victim, reflected);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CRIT).play(damager.getEyeLocation(), 0.3, 0.1, 15);
            VanillaSound.of(Sound.ITEM_SHIELD_BLOCK, 1F, 0.6F).play(victim.getLocation());
        }
        return true;
    }
}
