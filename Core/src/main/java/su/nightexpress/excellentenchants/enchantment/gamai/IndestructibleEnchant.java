package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.DurabilityEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

/**
 * Неразрушимость (любой предмет с прочностью): предмет не теряет прочность. Замена неразрушимости
 * Кирки Бога. Срабатывает на HIGHEST — после «Проклятия поломки» (NORMAL), которое иначе добавило бы
 * урон, и до «Восстановления» (MONITOR): урон обнуляется, поэтому «Восстановление» не тратится зря.
 */
@NullMarked
public class IndestructibleEnchant extends GameEnchantment implements DurabilityEnchant {

    public IndestructibleEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override
    public EnchantPriority getItemDamagePriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onItemDamage(PlayerItemDamageEvent event, Player player, ItemStack itemStack, int level) {
        if (event.getDamage() <= 0 && event.isCancelled()) return false;

        event.setDamage(0);
        event.setCancelled(true);
        return true;
    }
}
