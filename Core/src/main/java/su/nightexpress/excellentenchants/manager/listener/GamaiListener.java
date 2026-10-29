package su.nightexpress.excellentenchants.manager.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.gamai.GamaiHooks;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.manager.AbstractListener;

/**
 * События для {@link GamaiHooks} — книг, которые пришли на смену предметам Зари. Всё идёт через
 * {@link EnchantManager#handleInSlots}, как у штатных типов: запрет в мирах, заряды и кэш брони
 * работают так же. Регистрируется после {@link EnchantListener}, так что на HIGHEST смертельный
 * удар проверяется уже после всех зачарований защиты.
 */
@NullMarked
public class GamaiListener extends AbstractListener<EnchantsPlugin> {

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND};

    private final EnchantManager manager;

    public GamaiListener(EnchantsPlugin plugin, EnchantManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        this.manager.handleInSlots(player, ARMOR_SLOTS, EnchantRegistry.GAMAI, (item, enchant, level) -> enchant
            .onSneak(event, player, item, level));
    }

    // ПКМ по воздуху Bukkit присылает уже «отменённым», поэтому ignoreCancelled здесь нельзя;
    // смотрим на useItemInHand — его запрещают защита регионов и другие плагины.
    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY) return;

        Player player = event.getPlayer();
        this.manager.handleInSlot(player, EquipmentSlot.HAND, EnchantRegistry.GAMAI, (item, enchant, level) -> enchant
            .onUse(event, player, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED && event.getAction() != EntityPotionEffectEvent.Action.CHANGED) return;
        if (event.getNewEffect() == null || !(event.getEntity() instanceof LivingEntity entity)) return;

        this.manager.handleInSlots(entity, ARMOR_SLOTS, EnchantRegistry.GAMAI, (item, enchant, level) -> enchant
            .onPotionEffect(event, entity, item, level));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && entity instanceof Player player) {
            for (GamaiHooks enchant : EnchantRegistry.GAMAI.getEnchants()) {
                if (enchant.onFallDamage(event, player)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (entity.getHealth() - event.getFinalDamage() > 0D) return;

        this.manager.handleInSlots(entity, ARMOR_SLOTS, EnchantRegistry.GAMAI, (item, enchant, level) -> enchant
            .onLethalDamage(event, entity, item, level));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof LivingEntity entity)) return;

        this.manager.handleInSlots(entity, ARMOR_SLOTS, EnchantRegistry.GAMAI, (item, enchant, level) -> {
            if (event.isCancelled()) return false;

            return enchant.onProjectileTaken(event, entity, item, level);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        this.handChanged(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        this.handChanged(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        this.handChanged(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        EnchantRegistry.GAMAI.getEnchants().forEach(enchant -> enchant.forget(player));
    }

    private void handChanged(Player player) {
        EnchantRegistry.GAMAI.getEnchants().forEach(enchant -> enchant.onHandChange(player));
    }
}
