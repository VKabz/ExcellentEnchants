package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ледяные стрелы (лук, арбалет): попадание с шансом замораживает игрока — сильная медлительность
 * и иней на экране. Замена Морозного Выстрела.
 */
@NullMarked
public class FrostArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    /** С 140 тиков заморозки игра начинает бить уроном — держимся ниже, иней только для вида. */
    private static final int MAX_FREEZE_TICKS = 139;

    private int[]   chances;
    private int[]   seconds;
    private int     slownessLevel;
    private boolean playersOnly;

    public FrostArrowsEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.chances = ConfigValue.create("FrostArrows.Chance",
            new int[]{15, 20, 25},
            "Freeze chance (in percent) per level: I, II, III."
        ).read(config);
        this.seconds = ConfigValue.create("FrostArrows.Duration",
            new int[]{1, 2, 3},
            "Freeze duration (in seconds) per level: I, II, III."
        ).read(config);
        this.slownessLevel = ConfigValue.create("FrostArrows.Slowness_Level",
            7,
            "Slowness level while frozen. 7 and above stops walking completely."
        ).read(config);
        this.playersOnly = ConfigValue.create("FrostArrows.Players_Only",
            true,
            "Freeze only players (as in the book description). false = mobs too."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(GamaiMath.byLevel(this.chances, level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DURATION, level -> NumberUtil.format(GamaiMath.byLevel(this.seconds, level)));
    }

    @Override
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(EntityShootBowEvent event, LivingEntity shooter, ItemStack bow, int level) {
        return true; // Только пометить стрелу, шанс бросаем при попадании.
    }

    @Override
    public void onHit(ProjectileHitEvent event, LivingEntity shooter, Arrow projectile, int level) {

    }

    @Override
    public void onDamage(EntityDamageByEntityEvent event, LivingEntity shooter, LivingEntity victim, Arrow projectile,
                         int level) {
        if (this.playersOnly && !(victim instanceof Player)) return;
        if (victim == shooter) return;
        if (!GamaiMath.rolled(ThreadLocalRandom.current().nextDouble(), GamaiMath.byLevel(this.chances, level))) return;

        int ticks = (int) (GamaiMath.byLevel(this.seconds, level) * 20D);
        if (ticks <= 0) return;

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, Math.max(0, this.slownessLevel - 1)));
        victim.setFreezeTicks(Math.min(MAX_FREEZE_TICKS, Math.max(victim.getFreezeTicks(), ticks + 40)));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.SNOWFLAKE).play(victim.getLocation().add(0, 1, 0), 0.4, 0.05, 25);
            VanillaSound.of(Sound.BLOCK_GLASS_BREAK, 0.6F, 1.6F).play(victim.getLocation());
        }
    }
}
