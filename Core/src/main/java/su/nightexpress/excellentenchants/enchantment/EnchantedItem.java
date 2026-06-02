package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

import java.util.Map;

@NullMarked
public class EnchantedItem<T extends CustomEnchantment> {

    private final ItemStack       itemStack;
    private final Map<T, Integer> enchants;

    public EnchantedItem(ItemStack itemStack, Map<T, Integer> enchants) {
        this.itemStack = itemStack;
        this.enchants = enchants;
    }


    public ItemStack getItemStack() {
        return this.itemStack;
    }


    public Map<T, Integer> getEnchants() {
        return this.enchants;
    }
}
