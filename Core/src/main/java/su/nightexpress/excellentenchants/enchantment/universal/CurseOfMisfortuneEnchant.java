package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

@NullMarked
public class CurseOfMisfortuneEnchant extends GameEnchantment implements MiningEnchant, KillEnchant {

    private boolean dropXP;

    public CurseOfMisfortuneEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 7));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.dropXP = ConfigValue.create("CurseOfMisfortune.Drop_XP",
            false,
            "Controls whether XP should drop."
        ).read(config);
    }

    public boolean isDropXP() {
        return this.dropXP;
    }

    @Override

    public EnchantPriority getBreakPriority() {
        return EnchantPriority.HIGHEST;
    }


    @Override
    public EnchantPriority getKillPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onBreak(BlockBreakEvent event, LivingEntity player, ItemStack item, int level) {
        event.setDropItems(false);
        if (!this.dropXP) event.setExpToDrop(0);
        return true;
    }

    @Override
    public boolean onKill(EntityDeathEvent event, LivingEntity entity, Player killer, ItemStack weapon, int level) {
        event.getDrops().clear();
        if (!this.dropXP) event.setDroppedExp(0);
        return true;
    }
}
