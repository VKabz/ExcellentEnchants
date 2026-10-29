package su.nightexpress.excellentenchants.enchantment.gamai;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/**
 * Второе дыхание (нагрудник): смертельный удар отменяется — носитель встаёт с частью здоровья
 * и коротким Сопротивлением. Раз в N минут. Срабатывает раньше тотема, так что тотем сберегается.
 * Пустоту и /kill не спасает. Перенесено с сета Зари L4.
 */
@NullMarked
public class SecondWindEnchant extends GameEnchantment implements GamaiHooks {

    private final Cooldown cooldowns = new Cooldown();

    private double cooldown;
    private double healthPercent;
    private int    resistanceAmplifier;
    private int    resistanceTicks;
    private String actionBar;

    public SecondWindEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.cooldown = ConfigValue.create("SecondWind.Cooldown",
            600D,
            "Cooldown (in seconds)."
        ).read(config);
        this.healthPercent = ConfigValue.create("SecondWind.Health_Percent",
            35D,
            "Health (in percent of max health) the wearer is left with."
        ).read(config);
        this.resistanceAmplifier = ConfigValue.create("SecondWind.Resistance_Level",
            3,
            "Resistance level given after the save (3 = Resistance III)."
        ).read(config) - 1;
        this.resistanceTicks = ConfigValue.create("SecondWind.Resistance_Seconds",
            3,
            "Resistance duration (in seconds)."
        ).read(config) * 20;
        this.actionBar = ConfigValue.create("SecondWind.ActionBar",
            "<#7CDB8C>✔ Второе дыхание: вы спасены от смерти</#7CDB8C>",
            "Action bar text for the saved player (MiniMessage). Empty = no text."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format(this.cooldown / 60D));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.healthPercent));
    }

    @Override
    public boolean onLethalDamage(EntityDamageEvent event, LivingEntity entity, ItemStack item, int level) {
        if (event.isCancelled()) return false; // Уже спасли другим предметом.
        DamageCause cause = event.getCause();
        if (cause == DamageCause.VOID || cause == DamageCause.KILL) return false;
        if (!this.cooldowns.isReady(entity)) return false;

        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return false;

        this.cooldowns.start(entity, this.cooldown);
        event.setCancelled(true);

        double health = Math.max(1D, maxHealth.getValue() * this.healthPercent / 100D);
        entity.setHealth(Math.min(maxHealth.getValue(), health));
        entity.setFireTicks(0);
        if (this.resistanceTicks > 0) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, this.resistanceTicks, Math.max(0, this.resistanceAmplifier)));
        }

        VanillaSound.of(Sound.ITEM_TOTEM_USE, 0.8F, 1.2F).play(entity.getLocation());
        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.TOTEM_OF_UNDYING).play(entity.getLocation().add(0, 1, 0), 0.4, 0.2, 40);
        }
        if (entity instanceof Player player && !this.actionBar.isBlank()) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize(this.actionBar));
        }
        return true;
    }

    @Override
    public void forget(Player player) {
        // Откат намеренно НЕ сбрасываем при выходе: иначе перезаход обнулял бы 10 минут.
    }
}
