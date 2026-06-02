package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.DurabilityEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;

@NullMarked
public class CurseOfBreakingEnchant extends GameEnchantment implements DurabilityEnchant {

    private Modifier durabilityAmount;

    public CurseOfBreakingEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 10));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.durabilityAmount = Modifier.load(config, "CurseOfBreaking.Amount",
            Modifier.addictive(0).perLevel(1).capacity(5),
            "Amount of durability points to be taken from the item.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getDurabilityAmount(
            level)));
    }

    public int getDurabilityAmount(int level) {
        return (int) this.durabilityAmount.getValue(level);
    }

    @Override

    public EnchantPriority getItemDamagePriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onItemDamage(PlayerItemDamageEvent event, Player player, ItemStack itemStack, int level) {
        if (event.getDamage() == 0) return false;

        int durabilityAmount = this.getDurabilityAmount(level);
        if (durabilityAmount <= 0) return false;

        event.setDamage(event.getDamage() + durabilityAmount);
        return true;
    }
}
