package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
public class VeinminerEnchant extends GameEnchantment implements MiningEnchant {

    private static final BlockFace[] AREA = {BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH
    };

    private Modifier      blocksLimit;
    private Set<Material> affectedBlocks;
    private boolean       disableOnCrouch;

    public VeinminerEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.disableOnCrouch = ConfigValue.create("Veinminer.Disable_On_Crouch",
            true,
            "Sets whether or not enchantment will have no effect when crouching."
        ).read(config);

        this.blocksLimit = Modifier.load(config, "Veinminer.Block_Limit",
            Modifier.addictive(4).perLevel(1).capacity(16),
            "Max. possible amount of blocks to be mined at the same time.");

        this.affectedBlocks = ConfigValue.forSet("Veinminer.Block_List",
            BukkitThing::getMaterial,
            (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
            () -> {
                Set<Material> set = new HashSet<>();
                set.addAll(Tag.COAL_ORES.getValues());
                set.addAll(Tag.COPPER_ORES.getValues());
                set.addAll(Tag.DIAMOND_ORES.getValues());
                set.addAll(Tag.EMERALD_ORES.getValues());
                set.addAll(Tag.GOLD_ORES.getValues());
                set.addAll(Tag.IRON_ORES.getValues());
                set.addAll(Tag.LAPIS_ORES.getValues());
                set.addAll(Tag.REDSTONE_ORES.getValues());
                set.add(Material.NETHER_GOLD_ORE);
                set.add(Material.NETHER_QUARTZ_ORE);
                return set;
            },
            "List of blocks affected by this enchantment."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> String.valueOf(this.getBlocksLimit(level)));
    }


    public Set<Material> getAffectedBlocks() {
        return this.affectedBlocks;
    }

    public int getBlocksLimit(int level) {
        return (int) this.blocksLimit.getValue(level);
    }


    private Set<Block> getNearby(Block block) {
        return Stream.of(AREA).map(block::getRelative)
            .filter(blockAdded -> blockAdded.getType() == block.getType()).collect(Collectors.toSet());
    }

    private void vein(Player player, Block source, int level) {
        Set<Block> ores = new HashSet<>();
        Set<Block> prepare = new HashSet<>(this.getNearby(source));

        int limit = Math.min(this.getBlocksLimit(level), 30); // TODO Limit
        if (limit < 0) return;

        while (ores.addAll(prepare) && ores.size() < limit) {
            Set<Block> nearby = new HashSet<>();
            prepare.forEach(prepared -> nearby.addAll(this.getNearby(prepared)));
            prepare.clear();
            prepare.addAll(nearby);
        }
        ores.remove(source);
        ores.forEach(ore -> EnchantsUtils.safeBusyBreak(player, ore));
    }

    @Override

    public EnchantPriority getBreakPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onBreak(BlockBreakEvent event, LivingEntity entity, ItemStack tool, int level) {
        if (!(entity instanceof Player player)) return false;
        if (EnchantsUtils.isBusy()) return false;
        if (this.disableOnCrouch && player.isSneaking()) return false;

        Block block = event.getBlock();
        if (block.getDrops(tool, player).isEmpty()) return false;
        if (!this.getAffectedBlocks().contains(block.getType())) return false;

        this.vein(player, block, level);
        return true;
    }
}
