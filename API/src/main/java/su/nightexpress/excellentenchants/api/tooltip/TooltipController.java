package su.nightexpress.excellentenchants.api.tooltip;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;


@NullMarked
public interface TooltipController {

    boolean hasHandler();

    ItemStack addDescription(ItemStack itemStack);

    boolean isReadyForTooltipUpdate(Player player);

    boolean isEnchantTooltipAllowed(ItemStack item);

    void addToUpdateStopList(Player player);

    void removeFromUpdateStopList(Player player);

    void runInStopList(Player player, Runnable runnable);
}
