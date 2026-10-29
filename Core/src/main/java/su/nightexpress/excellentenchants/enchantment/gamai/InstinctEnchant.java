package su.nightexpress.excellentenchants.enchantment.gamai;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.glow.GlowHandler;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Чутьё (шлем): присел и держишь N секунд — игроки и мобы вокруг подсвечены сквозь стены,
 * но только для тебя. Замена Фонаря Душ.
 *
 * Список целей снимается один раз в момент срабатывания: кто пришёл позже, не светится.
 * Никаких тиков по всем игрокам — проверка одна, через N секунд после начала приседа.
 */
@NullMarked
public class InstinctEnchant extends GameEnchantment implements GamaiHooks {

    private final Cooldown        cooldowns   = new Cooldown();
    private final Map<UUID, Long> sneakTokens = new HashMap<>();

    private int[]   radius;
    private int[]   duration;
    private double  cooldown;
    private double  holdSeconds;
    private int     maxTargets;
    private boolean showPlayers;
    private boolean showMobs;
    private boolean colorPlayers;
    private @Nullable NamedTextColor playerColor;
    private @Nullable NamedTextColor mobColor;

    public InstinctEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.radius = ConfigValue.create("Instinct.Radius",
            new int[]{20, 30},
            "Search radius (in blocks) per level: I, II."
        ).read(config);
        this.duration = ConfigValue.create("Instinct.Duration",
            new int[]{10, 15},
            "Highlight duration (in seconds) per level: I, II."
        ).read(config);
        this.cooldown = ConfigValue.create("Instinct.Cooldown",
            60D,
            "Cooldown (in seconds)."
        ).read(config);
        this.holdSeconds = ConfigValue.create("Instinct.Sneak_Seconds",
            2D,
            "How long (in seconds) to hold sneak to trigger."
        ).read(config);
        this.maxTargets = ConfigValue.create("Instinct.Max_Targets",
            60,
            "Highlight at most this many nearest targets (keeps packets small in mob farms)."
        ).read(config);
        this.showPlayers = ConfigValue.create("Instinct.Show_Players",
            true,
            "Highlight players."
        ).read(config);
        this.showMobs = ConfigValue.create("Instinct.Show_Mobs",
            true,
            "Highlight mobs."
        ).read(config);
        this.colorPlayers = ConfigValue.create("Instinct.Color_Players",
            false,
            "Paint players' glow with Player_Color. Uses a fake scoreboard team on the viewer's client,",
            "which can hide TAB name tag prefixes of those players for the viewer until TAB refreshes them.",
            "Off: players glow white."
        ).read(config);
        this.playerColor = color(ConfigValue.create("Instinct.Player_Color",
            "red",
            "Glow color for players (vanilla color name: red, blue, gold, ...)."
        ).read(config));
        this.mobColor = color(ConfigValue.create("Instinct.Mob_Color",
            "blue",
            "Glow color for mobs (vanilla color name). Empty = white."
        ).read(config));

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(GamaiMath.byLevel(this.radius, level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DURATION, level -> NumberUtil.format(GamaiMath.byLevel(this.duration, level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format(this.cooldown));
    }

    @Override
    public boolean onSneak(PlayerToggleSneakEvent event, Player player, ItemStack item, int level) {
        if (!event.isSneaking()) return false;
        if (GamaiGlow.get() == null) return false;
        if (!this.cooldowns.isReady(player)) return false;

        long token = System.nanoTime();
        this.sneakTokens.put(player.getUniqueId(), token);

        long delay = Math.max(1L, (long) (this.holdSeconds * 20D));
        player.getScheduler().runDelayed(this.plugin, task -> this.activate(player, token), null, delay);
        return false; // Сработает (или нет) позже, когда присед продержится.
    }

    private void activate(Player player, long token) {
        Long current = this.sneakTokens.get(player.getUniqueId());
        if (current == null || current != token) return; // Отпустил и присел заново — считает новый таймер.
        this.sneakTokens.remove(player.getUniqueId());

        if (!player.isOnline() || !player.isSneaking()) return;

        ItemStack helmet = player.getInventory().getHelmet();
        int level = helmet == null ? 0 : EnchantsUtils.getLevel(helmet, this.getBukkitEnchantment());
        if (level <= 0) return; // Снял шлем, пока сидел.

        GlowHandler glow = GamaiGlow.get();
        if (glow == null) return;
        if (!this.cooldowns.tryUse(player, this.cooldown)) return;

        List<Entity> targets = this.findTargets(player, GamaiMath.byLevel(this.radius, level));
        long session = glow.show(player, targets, this.colorPlayers ? this.playerColor : null, this.mobColor);

        long ticks = Math.max(20L, (long) (GamaiMath.byLevel(this.duration, level) * 20D));
        player.getScheduler().runDelayed(this.plugin, task -> glow.hide(player, session), null, ticks);

        if (this.hasVisualEffects()) {
            VanillaSound.of(Sound.ENTITY_WARDEN_HEARTBEAT, 1F, 1.2F).play(player);
        }
    }

    private List<Entity> findTargets(Player player, double radius) {
        double radiusSquared = radius * radius;
        List<Entity> targets = new ArrayList<>();

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (entity.getLocation().distanceSquared(player.getLocation()) > radiusSquared) continue;

            if (entity instanceof Player other) {
                if (!this.showPlayers || other.getGameMode() == GameMode.SPECTATOR || !player.canSee(other)) continue;
            }
            else if (!this.showMobs || !(entity instanceof Mob)) continue;

            targets.add(entity);
        }

        targets.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())));
        return targets.size() > this.maxTargets ? new ArrayList<>(targets.subList(0, Math.max(0, this.maxTargets))) : targets;
    }

    @Override
    public void forget(Player player) {
        this.sneakTokens.remove(player.getUniqueId());
        GlowHandler glow = GamaiGlow.get();
        if (glow != null) glow.forget(player);
    }

    private static @Nullable NamedTextColor color(String name) {
        return name.isBlank() ? null : NamedTextColor.NAMES.value(name.trim().toLowerCase());
    }
}
