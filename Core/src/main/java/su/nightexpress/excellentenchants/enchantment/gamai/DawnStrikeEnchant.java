package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/**
 * Удар Зари (меч, топор): раз в N секунд удар считается так, будто брони у цели на 30 % меньше.
 *
 * Броню игра учтёт уже после события, поэтому поднимаем урон ровно во столько раз, во сколько
 * ослабленная броня пропустила бы больше. Это усиление удара, а не обход защиты: зачарования
 * защиты, «Рассвет» и прочие срезы отработают как обычно. Перенесено с Меча Зари L5.
 */
@NullMarked
public class DawnStrikeEnchant extends GameEnchantment implements AttackEnchant, GamaiHooks {

    private static final double MAX_FACTOR = 3D;

    private final Cooldown cooldowns = new Cooldown();

    private Modifier cooldown;
    private double   armorIgnore;
    private boolean  fullChargeOnly;

    public DawnStrikeEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.cooldown = Modifier.load(config, "DawnStrike.Cooldown",
            Modifier.addictive(23).perLevel(-3),
            "Cooldown (in seconds). Default: 20 / 17 / 14."
        );
        this.armorIgnore = ConfigValue.create("DawnStrike.Armor_Ignore_Percent",
            30D,
            "Share of the target's armor (in percent) the strike passes through."
        ).read(config);
        this.fullChargeOnly = ConfigValue.create("DawnStrike.Full_Charge_Only",
            true,
            "Only a fully charged swing (attack cooldown restored) can trigger the strike."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format(this.cooldown.getValue(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.armorIgnore));
    }

    @Override
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onAttack(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim, ItemStack weapon, int level) {
        if (this.fullChargeOnly && damager instanceof Player player && player.getAttackCooldown() < 0.9F) return false;
        if (!this.cooldowns.isReady(damager)) return false;

        double armor = attribute(victim, Attribute.ARMOR);
        if (armor <= 0D) return false; // По голой цели удар не тратим.

        double toughness = attribute(victim, Attribute.ARMOR_TOUGHNESS);
        double factor = GamaiMath.armorPierceFactor(event.getDamage(), armor, toughness, this.armorIgnore / 100D, MAX_FACTOR);
        if (factor <= 1.01D) return false;

        this.cooldowns.start(damager, this.cooldown.getValue(level));
        event.setDamage(event.getDamage() * factor);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.FLASH, Color.fromRGB(0xFB, 0xAB, 0x08)).play(victim.getEyeLocation(), 0D, 0D, 1);
            UniParticle.of(Particle.END_ROD).play(victim.getEyeLocation(), 0.3, 0.05, 12);
            VanillaSound.of(Sound.ITEM_TRIDENT_THUNDER, 0.6F, 1.6F).play(victim.getLocation());
        }
        return true;
    }

    @Override
    public void forget(Player player) {
        this.cooldowns.forget(player);
    }

    private static double attribute(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0D : instance.getValue();
    }
}
