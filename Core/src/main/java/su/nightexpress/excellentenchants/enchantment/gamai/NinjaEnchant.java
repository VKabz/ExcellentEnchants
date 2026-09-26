package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;

/** Ниндзя (меч): удар из приседа сильнее. Присед исключает крит — размен честный. */
@NullMarked
public class NinjaEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier amount;

    public NinjaEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.amount = Modifier.load(config, "Ninja.Amount",
            Modifier.addictive(0).perLevel(10).capacity(40),
            "Extra damage (in percent) while sneaking."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.amount.getValue(level)));
    }

    @Override
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean onAttack(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                            ItemStack weapon, int level) {
        if (!(damager instanceof Player player) || !player.isSneaking()) return false;

        double percent = this.amount.getValue(level);
        if (percent <= 0D) return false;

        event.setDamage(event.getDamage() * (1D + percent / 100D));
        return true;
    }
}
