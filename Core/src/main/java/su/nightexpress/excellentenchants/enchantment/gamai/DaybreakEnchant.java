package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/**
 * Рассвет (нагрудник): игрок, который бьёт носителя вблизи, наносит меньше урона; мобы — всегда.
 * Зона ослабления Зари в виде книги: у сета было 30 %, тут 8 / 12 / 16 %.
 */
@NullMarked
public class DaybreakEnchant extends GameEnchantment implements DefendEnchant {

    private Modifier reduction;
    private double   playerRadius;

    public DaybreakEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.reduction = Modifier.load(config, "Daybreak.Damage_Reduction",
            Modifier.addictive(4).perLevel(4).capacity(60),
            "Damage reduction (in percent). Default: 8 / 12 / 16."
        );
        this.playerRadius = ConfigValue.create("Daybreak.Player_Radius",
            5D,
            "Players are weakened only within this distance (in blocks). Mobs are weakened at any distance."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.reduction.getValue(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(this.playerRadius));
    }

    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onProtect(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim, ItemStack weapon, int level) {
        if (damager == victim) return false;

        // damager — тот, кто бьёт или стрелял; стрелок издалека в зону не попадает.
        if (damager instanceof Player && !this.isNear(damager, victim)) return false;

        double percent = this.reduction.getValue(level);
        if (percent <= 0D) return false;

        event.setDamage(GamaiMath.cutDamage(event.getDamage(), percent));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.WAX_OFF).play(victim.getLocation().add(0, 1, 0), 0.3, 0.1, 4);
        }
        return true;
    }

    private boolean isNear(LivingEntity damager, LivingEntity victim) {
        return damager.getWorld() == victim.getWorld()
            && damager.getLocation().distanceSquared(victim.getLocation()) <= this.playerRadius * this.playerRadius;
    }
}
