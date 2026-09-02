package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Randomizer;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class CutterEnchant extends GameEnchantment implements AttackEnchant {

    private final ConfigProperty<Boolean> ignoreMythicMobs = ConfigProperty.of(ConfigTypes.BOOLEAN,
        "Cutter.Ignore-MythicMobs", false,
        "Whether enchantment has effect on mobs from the MythicMobs plugin.");

    private Modifier durabilityReduction;
    private boolean  allowPlayers;
    private boolean  allowMobs;

    public CutterEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(2, 1));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.durabilityReduction = Modifier.load(config, "Cutter.Durability_Reduction",
            Modifier.addictive(0).perLevel(0.01).capacity(1D),
            "Amount (in percent) of how much item durability will be reduced.");

        this.allowPlayers = ConfigValue.create("Cutter.Allow_Players",
            true,
            "Sets whether or not this enchantment will have effect on players.").read(config);

        this.allowMobs = ConfigValue.create("Cutter.Allow_Mobs",
            true,
            "Sets whether or not this enchantment will have effect on mobs.").read(config);

        this.ignoreMythicMobs.loadWithDefaults(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE, level -> NumberUtil.format(this.getDurabilityReduction(
            level) * 100D));
    }

    public final double getDurabilityReduction(int level) {
        return this.durabilityReduction.getValue(level);
    }

    @Override

    public EnchantPriority getAttackPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onAttack(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                            ItemStack weapon, int level) {
        EntityEquipment equipment = victim.getEquipment();
        if (equipment == null) return false;

        ItemStack[] armor = equipment.getArmorContents();
        if (armor.length == 0) return false;

        boolean isPlayer = victim instanceof Player;
        if (isPlayer && !this.allowPlayers || (!isPlayer && !this.allowMobs)) return false;
        if (!this.ignoreMythicMobs.get() && EnchantsUtils.isMythicMob(victim)) return false;

        int index = Randomizer.nextInt(armor.length);
        ItemStack itemCut = armor[index];

        if (itemCut == null || itemCut.getType().isAir() || itemCut.getType().getMaxDurability() == 0) return false;

        ItemMeta meta = itemCut.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return false;

        // Неразрушаемые вещи не портим: сеты вроде «Зари» помечены isUnbreakable, а прямой
        // setDamage() этот флаг не соблюдает — из-за этого Расчленитель ломал броню Зари.
        if (!damageable.isUnbreakable()) {
            // Урон добавляем к уже накопленному, а не выставляем абсолютом: иначе удар по
            // сильно потрёпанной броне её чинил.
            int extra = (int) (itemCut.getType().getMaxDurability() * this.getDurabilityReduction(level));
            int capped = Math.min(damageable.getDamage() + extra, itemCut.getType().getMaxDurability() - 1);
            damageable.setDamage(capped);
            itemCut.setItemMeta(damageable);
        }

        armor[index] = null;
        equipment.setArmorContents(armor);

        if (this.hasVisualEffects()) {
            UniParticle.itemCrack(itemCut).play(victim.getEyeLocation(), 0.25, 0.15, 30);
            VanillaSound.of(Sound.ENTITY_ITEM_BREAK).play(victim.getLocation());
        }

        this.giveCutItem(victim, itemCut);

        return true;
    }

    /**
     * Срезанная броня остаётся у своего хозяина: смысл зачарования в том, чтобы снять с
     * противника защиту, а не отобрать вещь. Поэтому предмет кладём в инвентарь ЖЕРТВЫ, а не
     * того, кто ударил. Изначально броня просто выбрасывалась под ноги, и в свалке её
     * подбирал атакующий — это и было воровством из карточки 987.
     *
     * Если инвентарь жертвы полон, вещь падает у её ног и привязана к ней: подобрать сможет
     * только хозяин, отбить добивающим ударом не выйдет.
     */
    private void giveCutItem(LivingEntity victim, ItemStack itemCut) {
        if (!(victim instanceof Player owner)) {
            // С моба броня просто падает рядом — инвентаря у него нет.
            this.dropCutItem(victim, itemCut, null);
            return;
        }

        Map<Integer, ItemStack> leftovers = owner.getInventory().addItem(itemCut.clone());
        for (ItemStack leftover : leftovers.values()) {
            this.dropCutItem(owner, leftover, owner.getUniqueId());
        }
    }

    private void dropCutItem(LivingEntity at, ItemStack itemCut, @Nullable UUID owner) {
        Location location = at.getLocation();
        Item drop = at.getWorld().dropItemNaturally(location, itemCut);
        drop.setPickupDelay(owner == null ? 50 : 0);
        if (owner != null) {
            // Ограничение ванильного Item: подобрать сможет только этот игрок.
            drop.setOwner(owner);
        }
    }
}
