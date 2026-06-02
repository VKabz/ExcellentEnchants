package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Randomizer;
import su.nightexpress.nightcore.util.sound.VanillaSound;

import java.nio.file.Path;

@NullMarked
public class RocketEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier fireworkPower;

    public RocketEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 2));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.fireworkPower = Modifier.load(config, "Rocket.Firework_Power",
            Modifier.addictive(1).perLevel(0.25).capacity(3D),
            "Firework power. The more power = the higher fly distance.");
    }

    public final double getFireworkPower(int level) {
        return this.fireworkPower.getValue(level);
    }

    @Override

    public EnchantPriority getAttackPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onAttack(EntityDamageByEntityEvent event, LivingEntity damager, LivingEntity victim,
                            ItemStack weapon, int level) {
        if (victim.isInsideVehicle()) {
            victim.leaveVehicle();
        }

        Firework firework = this.createRocket(victim.getWorld(), victim.getLocation(), level);
        firework.addPassenger(victim);

        VanillaSound.of(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH).play(victim.getLocation());
        return true;
    }


    private Firework createRocket(World world, Location location, int level) {
        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Type type = Randomizer.pick(FireworkEffect.Type.values());
        Color color = Color.fromBGR(Randomizer.nextInt(256), Randomizer.nextInt(256), Randomizer.nextInt(256));
        Color fade = Color.fromBGR(Randomizer.nextInt(256), Randomizer.nextInt(256), Randomizer.nextInt(256));
        FireworkEffect effect = FireworkEffect.builder().flicker(Randomizer.nextBoolean()).withColor(color).withFade(
            fade).with(type).trail(Randomizer.nextBoolean()).build();
        meta.addEffect(effect);
        meta.setPower((int) this.getFireworkPower(level));
        firework.setFireworkMeta(meta);
        return firework;
    }
}
