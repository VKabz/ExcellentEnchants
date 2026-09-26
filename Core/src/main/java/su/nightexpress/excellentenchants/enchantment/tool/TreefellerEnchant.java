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

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NullMarked
public class TreefellerEnchant extends GameEnchantment implements MiningEnchant {

    private static final BlockFace[] BLOCK_SIDES = {BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
    };

    // Потолок посещённых блоков за один удар — страховка тика на гигантских джунглевых деревьях.
    private static final int LOOKUP_LIMIT = 2048;

    private Modifier blocksLimit;
    private boolean  disableOnCrouch;

    public TreefellerEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.disableOnCrouch = ConfigValue.create("Treefeller.Disable_On_Crouch",
            true,
            "Controls whether enchantment effect can be bypassed by crouching."
        ).read(config);

        this.blocksLimit = Modifier.load(config, "Treefeller.Block_Limit",
            Modifier.addictive(96).perLevel(16).capacity(256),
            "Max. amount of LOGS to cut down. Leaves are NOT counted against this limit.");
    }


    /**
     * Обход дерева. Лимит считается ТОЛЬКО по брёвнам: раньше в него шли и листья, из-за чего
     * бюджет выедался кроной, а на тёмном дубе — самим стволом 2x2, и половина дерева оставалась
     * висеть в воздухе.
     *
     * Листья по-прежнему обходим (через них связаны ветки), но вглубь листвы не уходим и в счёт
     * их не берём. Отдельный потолок посещённых блоков держит стоимость одного удара конечной.
     */
    private List<Block> collectLogs(Block source, int logLimit) {
        Set<Block> visited = new HashSet<>();
        List<Block> logs = new ArrayList<>();
        Deque<Block> queue = new ArrayDeque<>();

        visited.add(source);
        queue.add(source);

        while (!queue.isEmpty() && logs.size() < logLimit && visited.size() < LOOKUP_LIMIT) {
            Block block = queue.poll();
            boolean fromLeaves = isLeaves(block.getType());

            if (!fromLeaves) logs.add(block);

            for (BlockFace face : BLOCK_SIDES) {
                if (visited.size() >= LOOKUP_LIMIT) break;

                Block relative = block.getRelative(face);
                if (!isLogOrLeaves(relative.getType())) continue;
                // Из листвы шагаем только на брёвна, иначе обход расползается по всей кроне.
                if (fromLeaves && isLeaves(relative.getType())) continue;

                if (visited.add(relative)) queue.add(relative);
            }
        }
        return logs;
    }

    private static boolean isLogOrLeaves(Material material) {
        return isLog(material) || isLeaves(material);
    }

    private static boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material);
    }

    private static boolean isLeaves(Material material) {
        return Tag.LEAVES.isTagged(material);
    }

    private void chopTree(Player player, Block source, ItemStack tool, int level) {
        List<Block> logsToBreak = this.collectLogs(source, this.blocksLimit.getIntValue(level));

        for (Block log : logsToBreak) {
            if (tool.getAmount() <= 0) break; // Item broke.
            if (log.equals(source)) continue; // Этот блок ломает сам игрок.

            EnchantsUtils.safeBusyBreak(player, log);
        }
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
        if (!isLog(block.getType())) return false;

        this.chopTree(player, block, tool, level);
        return true;
    }
}
