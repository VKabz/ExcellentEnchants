package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.DefendEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;

/**
 * Бронированные крылья (элитры): часть урона от существ гасится — элитры перестают
 * быть «голым нагрудником».
 */
@NullMarked
public class ArmoredWingsEnchant extends GameEnchantment implements DefendEnchant {

    private Modifier amount;

    public ArmoredWingsEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.amount = Modifier.load(config, "ArmoredWings.Amount",
            Modifier.addictive(0).perLevel(8).capacity(30),
            "Percent of incoming entity damage negated."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.amount.getValue(level)));
    }

    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onProtect(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                             ItemStack weapon, int level) {
        double percent = this.amount.getValue(level);
        if (percent <= 0D) return false;

        event.setDamage(event.getDamage() * (1D - percent / 100D));
        return true;
    }
}
