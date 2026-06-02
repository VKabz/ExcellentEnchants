package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

@NullMarked
public interface BlockEnchant extends CustomEnchantment {

    void onPlace(BlockPlaceEvent event, Player player, Block block, ItemStack itemStack);

    boolean canPlaceInContainers();
}
