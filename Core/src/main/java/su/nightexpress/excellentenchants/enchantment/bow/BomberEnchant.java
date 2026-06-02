package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.nio.file.Path;

@NullMarked
public class BomberEnchant extends GameEnchantment implements BowEnchant {

    private Modifier fuseTicks;

    public BomberEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
        this.addComponent(EnchantComponent.CHARGES, Charges.custom(Modifier.addictive(50).perLevel(10).build(), 1, 1,
            NightItem.fromType(Material.TNT)));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.fuseTicks = Modifier.load(config, "Bomber.Fuse_Ticks",
            Modifier.addictive(40).perLevel(10).capacity(200),
            "Sets TNT fuse ticks.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format((double) this.getFuseTicks(
            level) / 20D));
    }

    public int getFuseTicks(int level) {
        return (int) this.fuseTicks.getValue(level);
    }

    @Override

    public EnchantPriority getShootPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level) {
        if (!(event.getProjectile() instanceof Projectile projectile)) return false;

        int fuseTicks = Math.max(1, this.getFuseTicks(level));

        TNTPrimed primed = projectile.getWorld().spawn(projectile.getLocation(), TNTPrimed.class);
        primed.setVelocity(projectile.getVelocity().multiply(event.getForce() * 1.25));
        primed.setFuseTicks(fuseTicks);
        primed.setSource(shooter);
        event.setProjectile(primed);
        return true;
    }
}
