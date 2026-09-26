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
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Теневой удар (меч/топор): удар в спину сильнее. */
@NullMarked
public class BackstabEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier amount;

    public BackstabEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.amount = Modifier.load(config, "Backstab.Amount",
            Modifier.addictive(0).perLevel(15).capacity(60),
            "Extra damage (in percent) when hitting from behind."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.amount.getValue(level)));
    }

    @Override
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean onAttack(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                            ItemStack weapon, int level) {
        if (!GamaiUtils.isBehind(victim, damager)) return false;

        double percent = this.amount.getValue(level);
        if (percent <= 0D) return false;

        event.setDamage(event.getDamage() * (1D + percent / 100D));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.CRIT).play(victim.getEyeLocation(), 0.3, 0.1, 15);
            VanillaSound.of(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8F, 0.7F).play(victim.getLocation());
        }
        return true;
    }
}
