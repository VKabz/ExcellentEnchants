package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.KillEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;

import java.nio.file.Path;

@NullMarked
public class CurseOfMediocrityEnchant extends GameEnchantment implements BlockDropEnchant, KillEnchant {

    public CurseOfMediocrityEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 15));
    }

    @Override
    protected void loadAdditional(FileConfig config) {

    }

    @Override

    public EnchantPriority getDropPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override

    public EnchantPriority getKillPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onDrop(BlockDropItemEvent event, LivingEntity player, ItemStack item, int level) {
        event.getItems().forEach(drop -> {
            ItemStack stack = drop.getItemStack();
            EnchantsUtils.removeAll(stack);
            drop.setItemStack(stack);
        });

        return true;
    }

    @Override
    public boolean onKill(EntityDeathEvent event, LivingEntity entity, Player killer, ItemStack weapon, int level) {
        event.getDrops().forEach(stack -> {
            ItemUtil.editMeta(stack, meta -> {
                meta.getEnchants().keySet().forEach(meta::removeEnchant);
            });
        });

        return true;
    }
}
