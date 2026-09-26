package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.nio.file.Path;

/**
 * Посев (мотыга): ПКМ по культуре или грядке засеивает площадку вокруг той же культурой
 * из инвентаря. Работает до Земледельца (он сажает одно семя в центр).
 */
@NullMarked
public class SowerEnchant extends GameEnchantment implements InteractEnchant {

    private Modifier radius;
    private boolean  disableOnCrouch;

    public SowerEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.radius = Modifier.load(config, "Sower.Radius",
            Modifier.addictive(0).perLevel(1).capacity(4),
            "Square radius around the clicked block. 1 = 3x3, 2 = 5x5, ..."
        );
        this.disableOnCrouch = ConfigValue.create("Sower.Disable_On_Crouch",
            true,
            "When 'true', crouching disables the enchantment."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> {
            int size = this.radius.getIntValue(level) * 2 + 1;
            return size + "x" + size;
        });
    }

    @Override
    public EnchantPriority getInteractPriority() {
        return EnchantPriority.LOW;
    }

    @Nullable
    private Material pickSeed(Player player, Block clicked) {
        // Кликнули по культуре — сеем её же. По грядке — первое семя, которое есть в инвентаре.
        if (FarmUtils.isCrop(clicked.getType())) {
            Material seed = clicked.getBlockData().getPlacementMaterial();
            return FarmUtils.SEED_TO_CROP.containsKey(seed) ? seed : null;
        }
        if (clicked.getType() == Material.FARMLAND || clicked.getType() == Material.SOUL_SAND) {
            for (Material seed : FarmUtils.SEED_TO_CROP.keySet()) {
                if (!FarmUtils.isSoilFor(seed, clicked.getType())) continue;
                if (player.getInventory().first(seed) >= 0) return seed;
            }
        }
        return null;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event, LivingEntity entity, ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        if (this.disableOnCrouch && player.isSneaking()) return false;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return false;

        Material seed = this.pickSeed(player, clicked);
        if (seed == null) return false;
        Material crop = FarmUtils.SEED_TO_CROP.get(seed);

        // Уровень грядки: если кликнули по растению, земля на блок ниже.
        Block ground = FarmUtils.isCrop(clicked.getType()) ? clicked.getRelative(BlockFace.DOWN) : clicked;

        int radius = this.radius.getIntValue(level);
        if (radius <= 0) return false;

        int planted = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block soil = ground.getRelative(dx, 0, dz);
                if (!FarmUtils.isSoilFor(seed, soil.getType())) continue;

                Block plant = soil.getRelative(BlockFace.UP);
                if (!plant.isEmpty()) continue;
                if (player.getInventory().first(seed) < 0) break;
                if (!FarmUtils.canPlace(player, plant, soil, seed)) continue;
                if (!FarmUtils.takeSeed(player, seed)) break;

                plant.setType(crop, true);
                planted++;
            }
        }
        if (planted == 0) return false;

        player.swingMainHand();
        if (this.hasVisualEffects()) {
            VanillaSound.of(seed == Material.NETHER_WART ? Sound.ITEM_NETHER_WART_PLANT : Sound.ITEM_CROP_PLANT).play(player);
        }
        return true;
    }
}
