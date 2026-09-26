package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

/** Термостойкость (кирка): пока кирка в руке — огнестойкость. */
@NullMarked
public class HeatResistanceEnchant extends GameEnchantment implements PassiveEnchant {

    // Чуть длиннее шага пассивного тика, чтобы эффект не мигал между срабатываниями.
    private static final int EFFECT_TICKS = 60;

    public HeatResistanceEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PERIODIC, Period.ofSeconds(1));
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override
    public boolean onTrigger(LivingEntity entity, ItemStack item, int level) {
        PotionEffect current = entity.getPotionEffect(PotionEffectType.FIRE_RESISTANCE);
        if (current != null && current.getDuration() > EFFECT_TICKS) return false; // Настоящее зелье не трогаем.

        return entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, EFFECT_TICKS, 0, true, false));
    }
}
