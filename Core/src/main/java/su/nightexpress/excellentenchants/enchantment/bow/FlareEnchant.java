package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.nio.file.Path;

@NullMarked
public class FlareEnchant extends GameEnchantment implements ArrowEnchant {

    public FlareEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.ELECTRIC_SPARK));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
        this.addComponent(EnchantComponent.CHARGES, Charges.custom(Modifier.addictive(50), 1, 1, NightItem.fromType(
            Material.TORCH)));
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override

    public EnchantPriority getShootPriority() {
        return EnchantPriority.HIGH;
    }

    @Override
    public boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level) {
        return event.getProjectile() instanceof Arrow;
    }

    @Override
    public void onHit(ProjectileHitEvent event, LivingEntity shooter, Arrow projectile, int level) {
        Block block = event.getHitBlock();
        if (block == null) return;

        BlockFace face = event.getHitBlockFace();
        if (face == null || face == BlockFace.DOWN) return;

        Block relative = block.getRelative(face);
        if (!relative.getType().isAir()) return;

        if (projectile.getShooter() instanceof Player player) {
            BlockPlaceEvent placeEvent = new BlockPlaceEvent(relative, relative
                .getState(), block, new ItemStack(Material.FLINT_AND_STEEL), player, true, EquipmentSlot.HAND);
            plugin.getPluginManager().callEvent(placeEvent);
            if (placeEvent.isCancelled() || !placeEvent.canBuild()) return;
        }

        BlockIgniteEvent igniteEvent = new BlockIgniteEvent(relative, BlockIgniteEvent.IgniteCause.ARROW, projectile);
        plugin.getPluginManager().callEvent(igniteEvent);
        if (igniteEvent.isCancelled()) return;

        relative.setType(Material.FIRE, false);

        // Attach the fire to the hit block side, so it doesn't vanish on the next block update.
        if (face != BlockFace.UP && relative.getBlockData() instanceof MultipleFacing facing
            && facing.getAllowedFaces().contains(face.getOppositeFace())) {
            facing.setFace(face.getOppositeFace(), true);
            relative.setBlockData(facing, true);
        }
    }

    @Override
    public void onDamage(EntityDamageByEntityEvent event, LivingEntity shooter, LivingEntity victim, Arrow arrow,
                         int level) {

    }
}
