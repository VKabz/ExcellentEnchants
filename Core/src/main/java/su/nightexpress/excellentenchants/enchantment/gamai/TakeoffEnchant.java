package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.GlideEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Взлёт (элитры): при старте полёта даёт рывок по направлению взгляда. */
@NullMarked
public class TakeoffEnchant extends GameEnchantment implements GlideEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier strength;
    private Modifier cooldown;

    public TakeoffEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.strength = Modifier.load(config, "Takeoff.Strength",
            Modifier.addictive(0.5).perLevel(0.3).capacity(2.5),
            "Boost strength on takeoff."
        );
        this.cooldown = Modifier.load(config, "Takeoff.Cooldown",
            Modifier.addictive(10),
            "Cooldown (in seconds)."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format(this.cooldown.getValue(level)));
    }

    @Override
    public EnchantPriority getGlidePriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onGlide(EntityToggleGlideEvent event, Player player, ItemStack item, int level) {
        if (!event.isGliding()) return false;
        if (!this.cooldowns.tryUse(player, this.cooldown.getValue(level))) return false;

        double power = this.strength.getValue(level);

        // Скорость выставляем следующим тиком: в момент события клиент ещё сам решает, стартовал ли полёт.
        player.getScheduler().run(this.plugin, task -> {
            if (!player.isOnline() || !player.isGliding()) return;

            Vector boost = player.getLocation().getDirection().multiply(power);
            player.setVelocity(player.getVelocity().add(boost));

            if (this.hasVisualEffects()) {
                UniParticle.of(Particle.CLOUD).play(player.getLocation(), 0.4, 0.1, 25);
                VanillaSound.of(Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.8F, 1.3F).play(player.getLocation());
            }
        }, null);
        return true;
    }
}
