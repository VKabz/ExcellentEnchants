package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockChangeEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

@NullMarked
public class LightweightEnchant extends GameEnchantment implements BlockChangeEnchant, InteractEnchant {

    public LightweightEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override
    public boolean onBlockChange(EntityChangeBlockEvent event, LivingEntity entity, ItemStack itemStack, int level) {
        Block block = event.getBlock();
        Material origin = block.getType();
        Material target = event.getTo();

        if (origin == Material.FARMLAND && target == Material.DIRT) {
            event.setCancelled(true);
            return true;
        }

        if (origin == Material.BIG_DRIPLEAF && target == Material.BIG_DRIPLEAF && !entity.isSneaking()) {
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event, LivingEntity entity, ItemStack item, int level) {
        if (event.getAction() != Action.PHYSICAL) return false;

        Block block = event.getClickedBlock();
        if (block == null) return false;

        if (block.getType() == Material.TURTLE_EGG) {
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    @Override

    public EnchantPriority getInteractPriority() {
        return EnchantPriority.NORMAL;
    }
}
