package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/**
 * База для «мгновенно ломает X по ЛКМ» — тот же приём, что у Разбивания стекла:
 * player.breakBlock() прогоняет защиту регионов, дроп по инструменту и прочность.
 */
@NullMarked
public abstract class InstantBreakEnchant extends GameEnchantment implements InteractEnchant {

    protected InstantBreakEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    protected abstract boolean isTarget(Material material);

    @Override
    public EnchantPriority getInteractPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event, LivingEntity entity, ItemStack item, int level) {
        if (event.useItemInHand() == Event.Result.DENY) return false;
        if (event.useInteractedBlock() == Event.Result.DENY) return false;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return false;
        if (!(entity instanceof Player player)) return false;

        Block block = event.getClickedBlock();
        if (block == null || !this.isTarget(block.getType())) return false;

        Material material = block.getType();
        var soundGroup = block.getBlockData().getSoundGroup();
        if (!player.breakBlock(block)) return false;

        if (this.hasVisualEffects()) {
            UniParticle.blockCrack(material).play(LocationUtil.setCenter3D(block.getLocation()), 0.5, 0.1, 10);
            VanillaSound.of(soundGroup.getBreakSound()).play(player);
        }
        return true;
    }
}
