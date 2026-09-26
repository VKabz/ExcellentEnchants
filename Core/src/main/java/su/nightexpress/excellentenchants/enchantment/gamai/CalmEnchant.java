package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;

/**
 * Спокойствие (шлем): криперы рядом взрываются с задержкой — есть время отойти.
 *
 * Замедление снимается само, когда носитель ушёл: иначе один игрок в шлеме постепенно
 * оставил бы за собой карту навсегда замедленных криперов.
 */
@NullMarked
public class CalmEnchant extends GameEnchantment implements PassiveEnchant {

    private static final int  VANILLA_FUSE = 30;
    private static final long PERIOD_SECONDS = 2L;

    private final NamespacedKey seenKey;

    private Modifier fuseBonus;
    private double   radius;

    public CalmEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.seenKey = new NamespacedKey(plugin, "calm_seen");
        this.addComponent(EnchantComponent.PERIODIC, Period.ofSeconds((int) PERIOD_SECONDS));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.fuseBonus = Modifier.load(config, "Calm.Fuse_Bonus",
            Modifier.addictive(0).perLevel(50).capacity(200),
            "Extra creeper fuse time (in percent of the vanilla 1.5s)."
        );
        this.radius = ConfigValue.create("Calm.Radius",
            8D,
            "Radius (in blocks) around the player."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.fuseBonus.getValue(level)));
    }

    @Override
    public boolean onTrigger(LivingEntity entity, ItemStack item, int level) {
        int fuse = (int) (VANILLA_FUSE * (1D + this.fuseBonus.getValue(level) / 100D));
        if (fuse <= VANILLA_FUSE) return false;

        long now = System.currentTimeMillis();
        boolean any = false;

        for (Creeper creeper : entity.getLocation().getNearbyEntitiesByType(Creeper.class, this.radius)) {
            boolean fresh = creeper.getPersistentDataContainer().has(this.seenKey, PersistentDataType.LONG);
            creeper.getPersistentDataContainer().set(this.seenKey, PersistentDataType.LONG, now);

            if (creeper.getMaxFuseTicks() < fuse) creeper.setMaxFuseTicks(fuse);
            if (!fresh) this.scheduleRestore(creeper);
            any = true;
        }
        return any;
    }

    /**
     * Один сторож на крипера: пока носитель рядом, метка обновляется и сторож переносится;
     * как только обновления прекратились — фитиль возвращается к ванильному.
     */
    private void scheduleRestore(Creeper creeper) {
        long delay = (PERIOD_SECONDS + 1L) * 20L;

        creeper.getScheduler().runDelayed(this.plugin, task -> {
            if (!creeper.isValid()) return;

            Long seen = creeper.getPersistentDataContainer().get(this.seenKey, PersistentDataType.LONG);
            if (seen != null && System.currentTimeMillis() - seen < PERIOD_SECONDS * 1000L + 500L) {
                this.scheduleRestore(creeper); // Носитель всё ещё рядом.
                return;
            }

            creeper.setMaxFuseTicks(VANILLA_FUSE);
            creeper.getPersistentDataContainer().remove(this.seenKey);
        }, null, delay);
    }
}
