package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

@NullMarked
public class RiverMasterEnchant extends GameEnchantment implements FishingEnchant {

    private Modifier distanceMod;

    public RiverMasterEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.distanceMod = Modifier.load(config, "RiverMaster.Distance_Modifier",
            Modifier.addictive(1).perLevel(0.25).capacity(3D),
            "Multiplies the casted fish hook's velocity by specified value.",
            "This does not bypasses the hook distance limits."
        );
    }

    public double getDistanceMod(int level) {
        return this.distanceMod.getValue(level);
    }

    @Override

    public EnchantPriority getFishingPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onFishing(PlayerFishEvent event, ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.FISHING) return false;

        FishHook hook = event.getHook();
        hook.setVelocity(hook.getVelocity().multiply(this.getDistanceMod(level)));
        return true;
    }
}
