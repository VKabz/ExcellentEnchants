package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

/** Теплица (шлем): культуры вокруг игрока периодически получают лишнюю стадию роста. */
@NullMarked
public class GreenhouseEnchant extends GameEnchantment implements PassiveEnchant {

    private Modifier radius;
    private Modifier chance;
    private int      maxBlocks;
    private int      height;

    public GreenhouseEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PERIODIC, Period.ofSeconds(10));
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.radius = Modifier.load(config, "Greenhouse.Radius",
            Modifier.addictive(3).perLevel(1).capacity(8),
            "Horizontal radius (in blocks) around the player.",
            "Каждый +1 к радиусу — это квадратичный рост числа проверяемых блоков",
            "на каждого носителя, поэтому потолок намеренно низкий."
        );
        this.chance = Modifier.load(config, "Greenhouse.Chance",
            Modifier.addictive(20).perLevel(10).capacity(100),
            "Chance (in percent) for each crop to grow one stage per trigger."
        );
        this.height = ConfigValue.create("Greenhouse.Height",
            1,
            "Vertical reach (in blocks) up and down from the player."
        ).read(config);
        this.maxBlocks = ConfigValue.create("Greenhouse.Max_Blocks",
            32,
            "Max. crops grown per trigger — keeps the cost predictable on big farms."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(this.radius.getValue(level)));
    }

    @Override
    public boolean onTrigger(LivingEntity entity, ItemStack item, int level) {
        World world = entity.getWorld();
        Location center = entity.getLocation();
        int radius = this.radius.getIntValue(level);
        double chance = this.chance.getValue(level);

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int grown = 0;

        for (int x = cx - radius; x <= cx + radius && grown < this.maxBlocks; x++) {
            for (int z = cz - radius; z <= cz + radius && grown < this.maxBlocks; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

                for (int y = cy - this.height; y <= cy + this.height && grown < this.maxBlocks; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!FarmUtils.isGrowingCrop(block)) continue;
                    if (!Rnd.chance(chance)) continue;

                    Ageable ageable = (Ageable) block.getBlockData();
                    FarmUtils.setAge(block, ageable.getAge() + 1);
                    grown++;

                    if (this.hasVisualEffects()) {
                        UniParticle.of(Particle.HAPPY_VILLAGER).play(LocationUtil.setCenter3D(block.getLocation()), 0.3, 0.05, 3);
                    }
                }
            }
        }
        return grown > 0;
    }
}
