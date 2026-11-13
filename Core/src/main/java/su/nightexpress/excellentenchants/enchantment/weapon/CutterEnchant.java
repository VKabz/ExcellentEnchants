package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jspecify.annotations.NullMarked;

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
import java.util.ArrayList;
import java.util.List;

@NullMarked
public class CutterEnchant extends GameEnchantment implements AttackEnchant {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

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
            Modifier.addictive(0).perLevel(0.10).capacity(1D),
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

        boolean isPlayer = victim instanceof Player;
        if (isPlayer && !this.allowPlayers || (!isPlayer && !this.allowMobs)) return false;
        if (!this.ignoreMythicMobs.get() && EnchantsUtils.isMythicMob(victim)) return false;

        // Неразрушаемые вещи не портим: прочности у них нет.
        List<EquipmentSlot> damageable = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack item = equipment.getItem(slot);
            if (item.getType().isAir() || item.getType().getMaxDurability() <= 0) continue;
            if (item.getItemMeta() instanceof Damageable meta && meta.isUnbreakable()) continue;
            damageable.add(slot);
        }
        if (damageable.isEmpty()) return false;

        EquipmentSlot slot = damageable.get(Randomizer.nextInt(damageable.size()));
        ItemStack item = equipment.getItem(slot);
        int amount = (int) Math.ceil(item.getType().getMaxDurability() * this.getDurabilityReduction(level));
        if (amount <= 0) return false;

        ItemStack crack = item.clone();

        // Броня остаётся на противнике и теряет прочность (#1288: «ломал броню», а не снимал её).
        // Урон — ванильным путём, как от обычного удара: учитывает «Прочность», вызывает
        // PlayerItemDamageEvent (его отменяют «Неразрушимость» и вещи с сайта) и ломает вещь
        // с анимацией, когда прочность кончилась.
        victim.damageItemStack(slot, amount);

        if (this.hasVisualEffects()) {
            UniParticle.itemCrack(crack).play(victim.getEyeLocation(), 0.25, 0.15, 30);
            VanillaSound.of(Sound.ENTITY_ITEM_BREAK).play(victim.getLocation());
        }

        return true;
    }
}
