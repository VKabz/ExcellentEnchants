package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public final class FarmUtils {

    /** Семя → культура. То же, что у Земледельца, плюс какао не берём — оно растёт на стволе. */
    public static final Map<Material, Material> SEED_TO_CROP = new HashMap<>();

    static {
        SEED_TO_CROP.put(Material.WHEAT_SEEDS, Material.WHEAT);
        SEED_TO_CROP.put(Material.BEETROOT_SEEDS, Material.BEETROOTS);
        SEED_TO_CROP.put(Material.MELON_SEEDS, Material.MELON_STEM);
        SEED_TO_CROP.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM);
        SEED_TO_CROP.put(Material.POTATO, Material.POTATOES);
        SEED_TO_CROP.put(Material.CARROT, Material.CARROTS);
        SEED_TO_CROP.put(Material.NETHER_WART, Material.NETHER_WART);
    }

    private FarmUtils() {}

    /** Культура с грядки (пшеница, морковь, ... адский нарост, какао). */
    public static boolean isCrop(Material material) {
        return Tag.CROPS.isTagged(material) || material == Material.NETHER_WART || material == Material.COCOA;
    }

    public static boolean isGrowingCrop(Block block) {
        return isCrop(block.getType()) && block.getBlockData() instanceof Ageable ageable
            && ageable.getAge() < ageable.getMaximumAge();
    }

    public static boolean isRipeCrop(Block block) {
        return isCrop(block.getType()) && block.getBlockData() instanceof Ageable ageable
            && ageable.getAge() >= ageable.getMaximumAge();
    }

    /** Подходит ли грунт для семени: адский нарост — на песок душ, остальное — на грядку. */
    public static boolean isSoilFor(Material seed, Material ground) {
        return seed == Material.NETHER_WART ? ground == Material.SOUL_SAND : ground == Material.FARMLAND;
    }

    public static boolean isTillable(Material material) {
        return material == Material.GRASS_BLOCK || material == Material.DIRT || material == Material.DIRT_PATH;
    }

    /** Списывает одно семя из инвентаря. */
    public static boolean takeSeed(Player player, Material seed) {
        int slot = player.getInventory().first(seed);
        if (slot < 0) return false;

        ItemStack stack = player.getInventory().getItem(slot);
        if (stack == null || stack.getType().isAir()) return false;

        stack.setAmount(stack.getAmount() - 1);
        return true;
    }

    /**
     * Спрашивает у сервера (регионы, приваты), можно ли игроку поставить блок здесь.
     * Тот же приём, что у Костра: синтетический BlockPlaceEvent.
     */
    public static boolean canPlace(Player player, Block target, Block against, Material item) {
        BlockPlaceEvent event = new BlockPlaceEvent(target, target.getState(), against, new ItemStack(item), player,
            true, EquipmentSlot.HAND);
        player.getServer().getPluginManager().callEvent(event);
        return !event.isCancelled() && event.canBuild();
    }

    public static void setAge(Block block, int age) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Ageable ageable)) return;

        ageable.setAge(Math.min(age, ageable.getMaximumAge()));
        block.setBlockData(ageable, true);
    }
}
