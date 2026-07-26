package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.InventoryEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

@NullMarked
public class SoulboundEnchant extends GameEnchantment implements InventoryEnchant {

    public SoulboundEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(20, 0));
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override
    public boolean onDeath(PlayerDeathEvent event, Player player, ItemStack itemStack, int level) {
        if (event.getKeepInventory()) return false;
        if (!this.testTriggerChance(level)) return false;

        event.getDrops().remove(itemStack);
        event.getItemsToKeep().add(itemStack);
        return true;
    }
}
