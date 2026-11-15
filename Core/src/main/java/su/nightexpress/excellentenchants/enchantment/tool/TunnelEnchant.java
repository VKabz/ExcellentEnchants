package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Тоннель: I — 2 блока (вниз), II — крест из 5, III — квадрат 3×3 в плоскости удара.
 * С «Сокрушением III» это замена Кирки Бога (та ломала 3×3×3, Тоннель — один слой).
 *
 * Плоскость берётся по грани, в которую пришёлся удар (луч от глаз сквозь траву, факелы и любой
 * воздух, включая пещерный), а если луч ушёл мимо — по повороту головы, как у Кирки Бога.
 * Раньше луч упирался в пещерный воздух ущелий, траву или снег, и Тоннель молча ломал один блок;
 * ещё требовался непрозрачный центральный блок, а обсидиан вокруг не ломался.
 */
@NullMarked
public class TunnelEnchant extends GameEnchantment implements MiningEnchant {

    // Смещения в плоскости удара (a, b); первый — сам блок. b = -1 — вниз для стены, I уровень = тоннель 1×2.
    private static final int[][]       PLANE_OFFSETS       = new int[][]{{0, 0}, {0, -1}, {-1, 0}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1},};
    private static final Set<Material> INTERACTABLE_BLOCKS = new HashSet<>();
    private static final Set<Material> PROTECTED_BLOCKS    = new HashSet<>();
    private static final double        RAY_DISTANCE        = 8D;

    static {
        INTERACTABLE_BLOCKS.add(Material.REDSTONE_ORE);
        INTERACTABLE_BLOCKS.add(Material.DEEPSLATE_REDSTONE_ORE);

        // Как у Кирки Бога: спавнеры добываются своими правилами, их тоннелем не сносим.
        PROTECTED_BLOCKS.add(Material.SPAWNER);
        PROTECTED_BLOCKS.add(Material.TRIAL_SPAWNER);
        PROTECTED_BLOCKS.add(Material.VAULT);
    }

    private boolean disableOnSneak;

    public TunnelEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.disableOnSneak = ConfigValue.create("Tunnel.Disable_On_Crouch",
            true,
            "Sets whether or not enchantment will have no effect when crouching.").read(config);
    }

    @Override
    public EnchantPriority getBreakPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onBreak(BlockBreakEvent event, LivingEntity entity, ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (EnchantsUtils.isBusy()) return false;
        if (this.disableOnSneak && player.isSneaking()) return false;

        Block block = event.getBlock();
        Material type = block.getType();
        if (type.isInteractable() && !INTERACTABLE_BLOCKS.contains(type)) return false;
        if (block.getDrops(item).isEmpty()) return false;

        BlockFace face = hitFace(player, block);
        for (int[] offset : areaOffsets(face, blocksByLevel(level))) {
            if (item.getType().isAir()) break;

            Block extra = block.getRelative(offset[0], offset[1], offset[2]);
            if (!canBreakExtra(extra, item)) continue;

            EnchantsUtils.safeBusyBreak(player, extra);
        }
        return true;
    }

    private static boolean canBreakExtra(Block block, ItemStack item) {
        Material type = block.getType();
        if (type.isAir() || block.isLiquid()) return false;
        if (type.getHardness() < 0F) return false; // бедрок, барьер, рамка портала
        if (PROTECTED_BLOCKS.contains(type)) return false;
        if (type.isInteractable() && !INTERACTABLE_BLOCKS.contains(type)) return false; // сундуки, печи, двери
        // Без дропа (алмаз каменной киркой, обсидиан не той киркой) — не ломаем впустую.
        return !block.getDrops(item).isEmpty();
    }

    private static BlockFace hitFace(Player player, Block block) {
        Location eye = player.getEyeLocation();
        RayTraceResult hit = player.getWorld().rayTraceBlocks(eye, eye.getDirection(), RAY_DISTANCE, FluidCollisionMode.NEVER, true);
        if (hit != null && block.equals(hit.getHitBlock()) && hit.getHitBlockFace() != null) {
            return hit.getHitBlockFace();
        }
        return lookFace(eye.getYaw(), eye.getPitch());
    }

    /** Блоков в узоре по уровню: I — 2, II — 5, III и выше — 9. */
    static int blocksByLevel(int level) {
        if (level <= 1) return 2;
        if (level == 2) return 5;
        return PLANE_OFFSETS.length;
    }

    /** Грань, в которую смотрит игрок, по повороту головы (те же пороги, что у Кирки Бога). */
    static BlockFace lookFace(float yaw, float pitch) {
        if (pitch > 45F) return BlockFace.UP;     // смотрит вниз — бьёт в верхнюю грань
        if (pitch < -45F) return BlockFace.DOWN;

        float angle = (yaw % 360F + 360F) % 360F;
        if (angle < 45F || angle >= 315F) return BlockFace.NORTH; // смотрит на юг
        if (angle < 135F) return BlockFace.EAST;                  // на запад
        if (angle < 225F) return BlockFace.SOUTH;                 // на север
        return BlockFace.WEST;                                    // на восток
    }

    /** Смещения (x, y, z) соседей в плоскости, перпендикулярной грани удара; сам блок не входит. */
    static int[][] areaOffsets(BlockFace face, int blocks) {
        int count = Math.max(0, Math.min(blocks, PLANE_OFFSETS.length) - 1);
        int[][] result = new int[count][];
        for (int i = 0; i < count; i++) {
            int a = PLANE_OFFSETS[i + 1][0];
            int b = PLANE_OFFSETS[i + 1][1];
            result[i] = switch (face) {
                case UP, DOWN -> new int[]{a, 0, b};
                case EAST, WEST -> new int[]{0, b, a};
                default -> new int[]{a, b, 0};
            };
        }
        return result;
    }
}
