package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Несгибаемость (шлем): медлительность, слабость, слепота, тошнота и голод на носителе
 * короче на 15 / 25 / 40 %. Яд и Иссушение не трогает — танка должно быть чем пробить.
 *
 * Изменить длительность в событии нельзя, поэтому событие отменяется и эффект кладётся заново,
 * уже укороченный; повторный вызов от нашего же addPotionEffect отсекает {@link #reapplying}.
 */
@NullMarked
public class UnbendingEnchant extends GameEnchantment implements GamaiHooks {

    private final Set<UUID> reapplying = new HashSet<>();

    private int[]                 percents;
    private Set<PotionEffectType> effects;

    public UnbendingEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.percents = ConfigValue.create("Unbending.Shorten_Percent",
            new int[]{15, 25, 40},
            "How much shorter (in percent) the effects are, per level: I, II, III."
        ).read(config);
        this.effects = ConfigValue.forSet("Unbending.Effects",
            BukkitThing::getEffectType,
            (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::getAsString).toList()),
            () -> Lists.newSet(PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS,
                PotionEffectType.NAUSEA, PotionEffectType.HUNGER),
            "Effects that get shorter. Poison and Wither are left out on purpose."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(GamaiMath.byLevel(this.percents, level)));
    }

    @Override
    public boolean onPotionEffect(EntityPotionEffectEvent event, LivingEntity entity, ItemStack item, int level) {
        if (this.reapplying.contains(entity.getUniqueId())) return false;

        PotionEffect effect = event.getNewEffect();
        if (effect == null || effect.isInfinite() || effect.getDuration() <= 0) return false;
        if (!this.effects.contains(effect.getType())) return false;

        int duration = GamaiMath.shortenDuration(effect.getDuration(), GamaiMath.byLevel(this.percents, level));
        if (duration >= effect.getDuration()) return false;

        event.setCancelled(true);
        this.reapplying.add(entity.getUniqueId());
        try {
            entity.addPotionEffect(effect.withDuration(duration));
        }
        finally {
            this.reapplying.remove(entity.getUniqueId());
        }
        return true;
    }
}
