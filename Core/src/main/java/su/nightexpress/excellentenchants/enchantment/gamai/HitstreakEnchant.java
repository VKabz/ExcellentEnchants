package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хитстрик (меч): серия ударов по одной цели без пауз — каждый следующий сильнее.
 * Смена цели или пауза сбрасывают серию.
 */
@NullMarked
public class HitstreakEnchant extends GameEnchantment implements AttackEnchant {

    private record Streak(UUID target, int hits, long lastHit) {}

    private final Map<UUID, Streak> streaks = new HashMap<>();

    private Modifier perStack;
    private int      maxStacksMobs;
    private int      maxStacksPlayers;
    private double   window;

    public HitstreakEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.perStack = Modifier.load(config, "Hitstreak.Per_Stack",
            Modifier.addictive(2).perLevel(2).capacity(15),
            "Extra damage (in percent) per streak stack."
        );
        this.maxStacksMobs = ConfigValue.create("Hitstreak.Max_Stacks.Mobs",
            3,
            "Max. stacks against mobs."
        ).read(config);
        this.maxStacksPlayers = ConfigValue.create("Hitstreak.Max_Stacks.Players",
            5,
            "Max. stacks against players."
        ).read(config);
        this.window = ConfigValue.create("Hitstreak.Window",
            4D,
            "Max. pause (in seconds) between hits that keeps the streak."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.perStack.getValue(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MAX, level -> String.valueOf(this.maxStacksPlayers));
    }

    @Override
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean onAttack(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                            ItemStack weapon, int level) {
        long now = System.currentTimeMillis();
        Streak streak = this.streaks.get(damager.getUniqueId());

        int hits = 1;
        if (streak != null && streak.target().equals(victim.getUniqueId()) && now - streak.lastHit() <= this.window * 1000D) {
            hits = streak.hits() + 1;
        }
        this.streaks.put(damager.getUniqueId(), new Streak(victim.getUniqueId(), hits, now));

        if (this.streaks.size() > 2048) {
            this.streaks.values().removeIf(entry -> now - entry.lastHit() > this.window * 1000D);
        }

        int maxStacks = victim instanceof Player ? this.maxStacksPlayers : this.maxStacksMobs;
        int stacks = Math.min(hits - 1, maxStacks);
        if (stacks <= 0) return false;

        double percent = stacks * this.perStack.getValue(level);
        event.setDamage(event.getDamage() * (1D + percent / 100D));
        return true;
    }
}
