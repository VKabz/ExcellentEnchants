package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.nio.file.Path;

/** Крюк (удочка): зацепился крючком за блок и подтянул — притягивает игрока к крючку. */
@NullMarked
public class GrappleEnchant extends GameEnchantment implements FishingEnchant {

    private final Cooldown cooldowns = new Cooldown();

    private Modifier strength;
    private Modifier cooldown;

    public GrappleEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.strength = Modifier.load(config, "Grapple.Strength",
            Modifier.addictive(0.7).perLevel(0.3).capacity(3),
            "Pull strength towards the hook."
        );
        this.cooldown = Modifier.load(config, "Grapple.Cooldown",
            Modifier.addictive(1),
            "Cooldown (in seconds)."
        );
    }

    @Override
    public EnchantPriority getFishingPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onFishing(PlayerFishEvent event, ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.IN_GROUND) return false;

        Player player = event.getPlayer();
        FishHook hook = event.getHook();
        if (!this.cooldowns.tryUse(player, this.cooldown.getValue(level))) return false;

        Vector pull = hook.getLocation().toVector().subtract(player.getLocation().toVector());
        double distance = pull.length();
        if (distance < 1.5D) return false;

        // Тяга растёт с дистанцией, но не дальше уровня — иначе с 30 блоков улетаешь за крючок.
        double power = Math.min(this.strength.getValue(level), 0.35D + distance * 0.08D);
        pull.normalize().multiply(power);
        pull.setY(Math.max(pull.getY() + 0.3D, 0.45D));
        player.setVelocity(pull);

        if (this.hasVisualEffects()) {
            VanillaSound.of(Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1F, 0.7F).play(player.getLocation());
        }
        return true;
    }
}
