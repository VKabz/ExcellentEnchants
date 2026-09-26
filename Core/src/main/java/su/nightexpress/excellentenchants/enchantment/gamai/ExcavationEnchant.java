package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

/** Раскопки (мотыга): при сборе спелой культуры с шансом попадается костная мука. */
@NullMarked
public class ExcavationEnchant extends GameEnchantment implements BlockDropEnchant {

    public ExcavationEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(5, 3));
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override
    public EnchantPriority getDropPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onDrop(BlockDropItemEvent event, LivingEntity entity, ItemStack item, int level) {
        // Сам блок к этому моменту уже воздух — смотрим на состояние до ломания.
        BlockState state = event.getBlockState();
        if (!FarmUtils.isCrop(state.getType())) return false;
        if (!(state.getBlockData() instanceof Ageable ageable)) return false;
        if (ageable.getAge() < ageable.getMaximumAge()) return false;

        EnchantsUtils.populateResource(event, new ItemStack(Material.BONE_MEAL));
        return true;
    }
}
