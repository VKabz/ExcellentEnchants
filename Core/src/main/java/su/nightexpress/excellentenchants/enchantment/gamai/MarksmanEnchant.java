package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

/** Меткий стрелок (лук/арбалет): стрела летит по прямой, без гравитации. */
@NullMarked
public class MarksmanEnchant extends GameEnchantment implements BowEnchant {

    private int straightTicks;

    public MarksmanEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.straightTicks = ConfigValue.create("Marksman.Straight_Ticks",
            60,
            "How long (in ticks) the arrow keeps flying straight before gravity returns.",
            "Prevents arrows from flying forever into unloaded chunks."
        ).read(config);
    }

    @Override
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level) {
        Entity projectile = event.getProjectile();
        projectile.setGravity(false);

        projectile.getScheduler().runDelayed(this.plugin, task -> {
            if (projectile.isValid()) projectile.setGravity(true);
        }, null, this.straightTicks);
        return true;
    }
}
