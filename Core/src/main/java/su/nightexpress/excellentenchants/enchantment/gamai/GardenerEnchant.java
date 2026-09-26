package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

/** Садовник (мотыга): не даёт сломать невыросшую культуру. Присев — можно. */
@NullMarked
public class GardenerEnchant extends GameEnchantment implements MiningEnchant {

    private boolean bypassOnCrouch;

    public GardenerEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.bypassOnCrouch = ConfigValue.create("Gardener.Bypass_On_Crouch",
            true,
            "When 'true', crouching player can break unripe crops as usual."
        ).read(config);
    }

    @Override
    public EnchantPriority getBreakPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onBreak(BlockBreakEvent event, LivingEntity entity, ItemStack item, int level) {
        if (this.bypassOnCrouch && entity instanceof Player player && player.isSneaking()) return false;
        if (!FarmUtils.isGrowingCrop(event.getBlock())) return false;

        event.setCancelled(true);
        return true;
    }
}
