package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.nio.file.Path;

@NullMarked
public class SeasonedAnglerEnchant extends GameEnchantment implements FishingEnchant {

    private Modifier xpModifier;

    public SeasonedAnglerEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.xpModifier = Modifier.load(config, "SeasonedAngler.XP_Modifier",
            Modifier.addictive(0).perLevel(50).capacity(300),
            "Amount (in percent) of additional XP from fishing.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPPercent(level)));
    }

    public int getXPPercent(int level) {
        return (int) this.xpModifier.getValue(level);
    }

    @Override

    public EnchantPriority getFishingPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onFishing(PlayerFishEvent event, ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
        if (event.getExpToDrop() == 0) return false;

        int expDrop = event.getExpToDrop();
        int expPercent = this.getXPPercent(level);
        int expModified = (int) Math.ceil(expDrop * (1D + expPercent / 100D));

        event.setExpToDrop(expModified);
        return true;
    }
}
