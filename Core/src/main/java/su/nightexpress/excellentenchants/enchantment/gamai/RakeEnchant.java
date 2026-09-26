package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

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
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.nio.file.Path;

/** Грабли (мотыга): ПКМ по земле вспахивает всю площадку вокруг, а не один блок. */
@NullMarked
public class RakeEnchant extends GameEnchantment implements InteractEnchant {

    private Modifier radius;
    private boolean  disableOnCrouch;

    public RakeEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.radius = Modifier.load(config, "Rake.Radius",
            Modifier.addictive(0).perLevel(1).capacity(4),
            "Square radius around the clicked block. 1 = 3x3, 2 = 5x5, ..."
        );
        this.disableOnCrouch = ConfigValue.create("Rake.Disable_On_Crouch",
            true,
            "When 'true', crouching player tills a single block as usual."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> {
            int size = this.radius.getIntValue(level) * 2 + 1;
            return size + "x" + size;
        });
    }

    @Override
    public EnchantPriority getInteractPriority() {
        return EnchantPriority.HIGH;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event, LivingEntity entity, ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (event.getHand() != EquipmentSlot.HAND) return false;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        if (this.disableOnCrouch && player.isSneaking()) return false;

        Block clicked = event.getClickedBlock();
        if (clicked == null || !FarmUtils.isTillable(clicked.getType())) return false;
        if (!clicked.getRelative(0, 1, 0).isEmpty()) return false;

        int radius = this.radius.getIntValue(level);
        if (radius <= 0) return false;

        int tilled = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue; // Центр вспашет сама ваниль.

                Block block = clicked.getRelative(dx, 0, dz);
                if (!FarmUtils.isTillable(block.getType())) continue;
                if (!block.getRelative(0, 1, 0).isEmpty()) continue;
                if (!FarmUtils.canPlace(player, block, block, Material.FARMLAND)) continue;

                block.setType(Material.FARMLAND, true);
                tilled++;
            }
        }
        if (tilled == 0) return false;

        // Прочность — по блоку за каждую вспаханную клетку, как если бы игрок тыкал сам.
        player.getInventory().getItemInMainHand().damage(tilled, player);

        if (this.hasVisualEffects()) {
            VanillaSound.of(Sound.ITEM_HOE_TILL).play(clicked.getLocation());
        }
        return true;
    }
}
