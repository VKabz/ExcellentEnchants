package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Вознесение (булава): ПКМ подбрасывает вверх на 3 / 4 / 5 блоков, урон от падения после взлёта
 * не идёт. Связка «взлетел — ударил булавой». Замена Булавы Вознесения.
 *
 * Защита от падения живёт до первого приземления с уроном или до таймаута — отдельного
 * слежения за полётом нет.
 */
@NullMarked
public class AscensionEnchant extends GameEnchantment implements GamaiHooks {

    private final Cooldown        cooldowns = new Cooldown();
    private final Map<UUID, Long> safeUntil = new HashMap<>();

    private int[]   heights;
    private int[]   cooldown;
    private double  fallProtection;
    private boolean requireGround;
    private boolean showItemCooldown;

    public AscensionEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.heights = ConfigValue.create("Ascension.Height",
            new int[]{3, 4, 5},
            "Launch height (in blocks) per level: I, II, III."
        ).read(config);
        this.cooldown = ConfigValue.create("Ascension.Cooldown",
            new int[]{8, 6, 5},
            "Cooldown (in seconds) per level: I, II, III."
        ).read(config);
        this.fallProtection = ConfigValue.create("Ascension.Fall_Protection_Seconds",
            10D,
            "No fall damage for this long after a launch (or until the first landing)."
        ).read(config);
        this.requireGround = ConfigValue.create("Ascension.Require_Ground",
            true,
            "Launch only from the ground (no chaining launches in the air, no boost while gliding)."
        ).read(config);
        this.showItemCooldown = ConfigValue.create("Ascension.Show_Item_Cooldown",
            true,
            "Show the cooldown as the white overlay on the mace in the hotbar."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(GamaiMath.byLevel(this.heights, level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format(GamaiMath.byLevel(this.cooldown, level)));
    }

    @Override
    @SuppressWarnings("deprecation") // isOnGround у игрока — со слов клиента, но для подброса этого хватает.
    public boolean onUse(PlayerInteractEvent event, Player player, ItemStack item, int level) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            // ПКМ по сундуку, двери, рычагу — это не взлёт.
            if (block != null && block.getType().isInteractable() && !player.isSneaking()) return false;
        }
        if (player.isInsideVehicle() || player.isGliding() || player.isFlying()) return false;
        if (this.requireGround && !player.isOnGround()) return false;
        if (!this.cooldowns.isReady(player)) return false;

        double seconds = GamaiMath.byLevel(this.cooldown, level);
        this.cooldowns.start(player, seconds);
        if (this.showItemCooldown) {
            player.setCooldown(Material.MACE, (int) (seconds * 20D));
        }

        Vector velocity = player.getVelocity();
        player.setVelocity(new Vector(velocity.getX(), GamaiMath.launchVelocity(GamaiMath.byLevel(this.heights, level)), velocity.getZ()));
        player.setFallDistance(0F);
        this.safeUntil.put(player.getUniqueId(), System.currentTimeMillis() + (long) (this.fallProtection * 1000D));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.GUST).play(player.getLocation(), 0.3, 0.1, 3);
            VanillaSound.of(Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.8F, 1.1F).play(player.getLocation());
        }
        return true;
    }

    @Override
    public boolean onFallDamage(EntityDamageEvent event, Player player) {
        Long until = this.safeUntil.remove(player.getUniqueId());
        return until != null && until >= System.currentTimeMillis();
    }

    @Override
    public void forget(Player player) {
        this.safeUntil.remove(player.getUniqueId());
        this.cooldowns.forget(player);
    }
}
