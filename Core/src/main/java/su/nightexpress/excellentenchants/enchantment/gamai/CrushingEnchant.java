package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Сокрушение (кирка): камень, руда, сланец, незерак, земля, песок, гравий, дерево, обсидиан
 * и древние обломки ломаются быстрее, на III — мгновенно. С «Тоннелем» это Кирка Бога.
 * Мгновенно ломается только то, что с этой киркой даёт дроп: обсидиан каменной киркой
 * копается как обычно и не пропадает впустую.
 *
 * Мгновенное ломание — общий приём {@link InstantBreakEnchant}. На I–II уровне игроку на время
 * копания нужного блока вешается временный модификатор скорости копания (атрибут, его видит
 * и клиент, так что трещины идут ровно). Снимается, как только игрок бьёт другой блок, меняет
 * слот или выходит; на диск временный модификатор не пишется.
 */
@NullMarked
public class CrushingEnchant extends InstantBreakEnchant implements GamaiHooks {

    private final NamespacedKey modifierKey;

    private Set<Material> blocks;
    private int           instantLevel;
    private int[]         speedBonus;

    public CrushingEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.modifierKey = new NamespacedKey(plugin, "crushing");
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.blocks = ConfigValue.forSet("Crushing.Blocks",
            BukkitThing::getMaterial,
            (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::getAsString).toList()),
            CrushingEnchant::defaultBlocks,
            "Blocks that break faster."
        ).read(config);
        this.instantLevel = ConfigValue.create("Crushing.Instant_Level",
            3,
            "From this level on the blocks break instantly."
        ).read(config);
        this.speedBonus = ConfigValue.create("Crushing.Speed_Bonus",
            new int[]{50, 100},
            "Extra mining speed (in percent) per level below Instant_Level: I, II."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(GamaiMath.byLevel(this.speedBonus, level)));
    }

    @Override
    protected boolean isTarget(Material material) {
        return this.blocks.contains(material);
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event, LivingEntity entity, ItemStack item, int level) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || !(entity instanceof Player player)) return false;

        if (level >= this.instantLevel) {
            this.removeBoost(player);
            Block clicked = event.getClickedBlock();
            if (clicked == null || clicked.getDrops(item, player).isEmpty()) return false;
            return super.onInteract(event, entity, item, level);
        }

        Block block = event.getClickedBlock();
        boolean target = block != null && this.isTarget(block.getType())
            && event.useItemInHand() != Event.Result.DENY && event.useInteractedBlock() != Event.Result.DENY;
        if (!target) {
            this.removeBoost(player);
            return false;
        }

        double bonus = GamaiMath.breakSpeedBonus(GamaiMath.byLevel(this.speedBonus, level));
        AttributeInstance speed = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (speed == null || bonus <= 0D) return false;

        AttributeModifier current = speed.getModifier(this.modifierKey);
        if (current != null && current.getAmount() == bonus) return false;
        if (current != null) speed.removeModifier(this.modifierKey);

        speed.addTransientModifier(new AttributeModifier(this.modifierKey, bonus, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        return true;
    }

    @Override
    public void onHandChange(Player player) {
        this.removeBoost(player);
    }

    @Override
    public void forget(Player player) {
        this.removeBoost(player);
    }

    private void removeBoost(Player player) {
        AttributeInstance speed = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (speed != null && speed.getModifier(this.modifierKey) != null) {
            speed.removeModifier(this.modifierKey);
        }
    }

    private static Set<Material> defaultBlocks() {
        Set<Material> blocks = new HashSet<>(Lists.newSet(
            Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT, Material.GRASS_BLOCK, Material.PODZOL,
            Material.MYCELIUM,
            Material.SAND, Material.RED_SAND, Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.GRAVEL,
            Material.OBSIDIAN, Material.CRYING_OBSIDIAN, Material.ANCIENT_DEBRIS
        ));
        blocks.addAll(Tag.LOGS.getValues());
        blocks.addAll(stoneAndOres());
        return blocks;
    }

    private static Set<Material> stoneAndOres() {
        return Lists.newSet(
            Material.STONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.GRANITE, Material.DIORITE,
            Material.ANDESITE, Material.TUFF, Material.CALCITE, Material.DRIPSTONE_BLOCK,
            Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
            Material.NETHERRACK, Material.BASALT, Material.SMOOTH_BASALT, Material.BLACKSTONE,
            Material.END_STONE,
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE
        );
    }
}
