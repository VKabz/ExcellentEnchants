package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionEffects;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

@NullMarked
public class DragonHeartEnchant extends GameEnchantment implements PassiveEnchant {

    public DragonHeartEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);

        this.addComponent(EnchantComponent.POTION_EFFECT, PotionEffects.permanent(PotionEffectType.HEALTH_BOOST));
        this.addComponent(EnchantComponent.PERIODIC, Period.ofSeconds(5));
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override
    public boolean onTrigger(LivingEntity entity, ItemStack item, int level) {
        return this.addPotionEffect(entity, level);
    }
}
