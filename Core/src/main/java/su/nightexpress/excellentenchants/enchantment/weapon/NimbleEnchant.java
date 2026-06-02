package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Players;

import java.nio.file.Path;

@NullMarked
public class NimbleEnchant extends GameEnchantment implements KillEnchant {

    private boolean ignorePlayers;

    public NimbleEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.ignorePlayers = ConfigValue.create("Nimble.Ignore_Players",
            false,
            "Sets whether or not to ignore drops from players."
        ).read(config);
    }


    @Override
    public EnchantPriority getKillPriority() {
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onKill(EntityDeathEvent event, LivingEntity entity, Player killer, ItemStack weapon, int level) {
        if (this.ignorePlayers && entity instanceof Player) return false;

        event.getDrops().forEach(item -> Players.addItem(killer, item));
        event.getDrops().clear();
        return true;
    }
}
