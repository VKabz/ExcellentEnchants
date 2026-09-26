package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
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
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.random.Rnd;

import java.nio.file.Path;
import java.util.Set;

/** Золотая перчатка (лопата): в сыпучих блоках с шансом попадаются самородки. */
@NullMarked
public class ProspectorEnchant extends GameEnchantment implements BlockDropEnchant {

    private Set<Material> blocks;
    private double        goldChance;

    public ProspectorEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(2, 2));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.blocks = ConfigValue.forSet("Prospector.Blocks",
            BukkitThing::getMaterial,
            (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::getAsString).toList()),
            () -> Lists.newSet(Material.GRAVEL, Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT,
                Material.SAND, Material.RED_SAND, Material.CLAY, Material.SOUL_SAND, Material.SOUL_SOIL),
            "Blocks that can contain nuggets."
        ).read(config);
        this.goldChance = ConfigValue.create("Prospector.Gold_Chance",
            30D,
            "Chance (in percent) that the nugget is gold instead of iron."
        ).read(config);
    }

    @Override
    public EnchantPriority getDropPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onDrop(BlockDropItemEvent event, LivingEntity entity, ItemStack item, int level) {
        if (!this.blocks.contains(event.getBlockState().getType())) return false;

        Material nugget = Rnd.chance(this.goldChance) ? Material.GOLD_NUGGET : Material.IRON_NUGGET;
        EnchantsUtils.populateResource(event, new ItemStack(nugget));
        return true;
    }
}
