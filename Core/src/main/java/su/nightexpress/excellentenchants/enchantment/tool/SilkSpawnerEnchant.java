package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.spawner.Spawner;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.LangUtil;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.nio.file.Path;
import java.util.HashSet;

@NullMarked
public class SilkSpawnerEnchant extends GameEnchantment implements MiningEnchant, BlockEnchant {

    private String spawnerName;

    public SilkSpawnerEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 25));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.spawnerName = ConfigValue.create("SilkSpawner.Name",
            TagWrappers.GREEN.wrap(LangUtil.getSerializedName(Material.SPAWNER) + " " + TagWrappers.GRAY.wrap("(" +
                TagWrappers.WHITE.wrap(EnchantsPlaceholders.GENERIC_TYPE) + ")")),
            "Spawner item display name.",
            "Use '" + EnchantsPlaceholders.GENERIC_TYPE + "' for the mob name."
        ).read(config);
    }


    public ItemStack getSpawner(CreatureSpawner spawnerBlock) {
        ItemStack itemSpawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta stateMeta = (BlockStateMeta) itemSpawner.getItemMeta();
        if (stateMeta == null || spawnerBlock.getSpawnedType() == null) return itemSpawner;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateMeta.getBlockState();

        this.transferSettings(spawnerBlock, spawnerItem);
        spawnerItem.update(true);

        stateMeta.setBlockState(spawnerItem);
        ItemUtil.setCustomName(stateMeta, this.spawnerName.replace(EnchantsPlaceholders.GENERIC_TYPE, LangUtil
            .getSerializedName(spawnerBlock.getSpawnedType())));
        itemSpawner.setItemMeta(stateMeta);

        this.manager.setBlockEnchant(itemSpawner, this);
        return itemSpawner;
    }

    @Override

    public EnchantPriority getBreakPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean canPlaceInContainers() {
        return true;
    }

    @Override
    public boolean onBreak(BlockBreakEvent event, LivingEntity player, ItemStack item, int level) {
        if (!event.isDropItems()) return false;

        Block block = event.getBlock();
        if (!(block.getState() instanceof CreatureSpawner spawner)) return false;
        if (isCreativePlaced(block)) return false;

        event.setExpToDrop(0);
        event.setDropItems(false);

        Location location = LocationUtil.setCenter3D(block.getLocation());
        World world = block.getWorld();

        world.dropItemNaturally(location, this.getSpawner(spawner));
        return true;
    }

    private static boolean isCreativePlaced(Block block) {
        try {
            org.bukkit.plugin.Plugin guard = org.bukkit.Bukkit.getPluginManager().getPlugin("WaffCreativeGuard");
            if (guard != null && guard.isEnabled()) {
                Object marks = guard.getClass().getMethod("getCreativePlacedBlocks").invoke(guard);
                if (marks != null) {
                    Object res = marks.getClass().getMethod("contains", Block.class).invoke(marks, block);
                    if (Boolean.TRUE.equals(res)) return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public void onPlace(BlockPlaceEvent event, Player player, Block block, ItemStack itemStack) {
        if (block.getType() != Material.SPAWNER) return;
        if (!(itemStack.getItemMeta() instanceof BlockStateMeta stateMeta)) return;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateMeta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        this.transferSettings(spawnerItem, spawnerBlock);
        spawnerBlock.update(true);
    }

    private void transferSettings(Spawner from, Spawner to) {
        to.setPotentialSpawns(new HashSet<>());

        if (from.getPotentialSpawns().isEmpty()) {
            to.setSpawnedType(from.getSpawnedType());
        }
        else {
            from.getPotentialSpawns().forEach(to::addPotentialSpawn);
        }
        to.setDelay(from.getMinSpawnDelay());
        to.setMinSpawnDelay(from.getMinSpawnDelay());
        to.setMaxSpawnDelay(from.getMaxSpawnDelay());
        to.setMaxNearbyEntities(from.getMaxNearbyEntities());
        to.setRequiredPlayerRange(from.getRequiredPlayerRange());
        to.setSpawnCount(from.getSpawnCount());
        to.setSpawnRange(from.getSpawnRange());
    }
}
