package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

/**
 * Точки входа, которых нет у штатных типов EE: присед, ПКМ по воздуху, наложение зелья,
 * смертельный удар, попавший в носителя снаряд. Вызывает их {@code GamaiListener} через тот же
 * конвейер, что и остальные зачарования (миры, заряды, порядок), поэтому новой книге
 * достаточно переопределить нужный метод.
 *
 * Методы без предмета ({@link #onFallDamage}, {@link #onHandChange}, {@link #forget}) — для состояния самого
 * зачарования: кто взлетел, у кого откат. Их зовут для всех зачарований этого типа.
 */
@NullMarked
public interface GamaiHooks extends CustomEnchantment {

    /** Игрок присел (только начало приседа). Шлем/броня. */
    default boolean onSneak(PlayerToggleSneakEvent event, Player player, ItemStack item, int level) {
        return false;
    }

    /** ПКМ основной рукой — и по блоку, и по воздуху. Предмет в руке. */
    default boolean onUse(PlayerInteractEvent event, Player player, ItemStack item, int level) {
        return false;
    }

    /** На носителя накладывается или меняется эффект зелья. Броня. */
    default boolean onPotionEffect(EntityPotionEffectEvent event, LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    /** Удар убьёт носителя (урон после всех снижений не меньше здоровья). Броня. */
    default boolean onLethalDamage(EntityDamageEvent event, LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    /** В носителя попал снаряд. Броня и щит во второй руке. */
    default boolean onProjectileTaken(ProjectileHitEvent event, LivingEntity entity, ItemStack item, int level) {
        return false;
    }

    /** Урон от падения — неважно, что в руках. true — урон отменить. */
    default boolean onFallDamage(EntityDamageEvent event, Player player) {
        return false;
    }

    /** В основной руке может оказаться другой предмет: смена слота, F, выброс. Для всех зачарований типа. */
    default void onHandChange(Player player) {

    }

    /** Игрок вышел: забыть его откаты, метки и временные модификаторы. */
    default void forget(Player player) {

    }
}
