package su.nightexpress.excellentenchants.enchantment.gamai;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.JumpEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Рывок (ботинки): прыжок с зажатым SHIFT — рывок вперёд по взгляду. */
@NullMarked
public class DashEnchant extends GameEnchantment implements JumpEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier strength;
    private Modifier cooldown;

    public DashEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.strength = Modifier.load(config, "Dash.Strength",
            Modifier.addictive(0.7).perLevel(0.3).capacity(2.5),
            "Dash strength."
        );
        this.cooldown = Modifier.load(config, "Dash.Cooldown",
            Modifier.addictive(6),
            "Cooldown (in seconds)."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format(this.cooldown.getValue(level)));
    }

    @Override
    public EnchantPriority getJumpPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onJump(PlayerJumpEvent event, Player player, ItemStack item, int level) {
        if (!player.isSneaking()) return false;

        Vector direction = GamaiUtils.horizontal(player.getLocation().getDirection());
        if (direction == null) return false;
        if (!this.cooldowns.tryUse(player, this.cooldown.getValue(level))) return false;

        Vector velocity = direction.multiply(this.strength.getValue(level)).setY(0.35);

        // Прыжок сам выставит вертикальную скорость на этом же тике; наш вектор кладём следом.
        player.getScheduler().run(this.plugin, task -> {
            if (!player.isOnline()) return;

            player.setVelocity(velocity);
            if (this.hasVisualEffects()) {
                UniParticle.of(Particle.CLOUD).play(player.getLocation(), 0.3, 0.1, 12);
                VanillaSound.of(Sound.ENTITY_BREEZE_JUMP, 0.8F, 1.1F).play(player.getLocation());
            }
        }, null);
        return true;
    }
}
