package su.nightexpress.excellentenchants.manager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentenchants.EnchantsFiles;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.ProjectileEnchant;
import su.nightexpress.excellentenchants.api.item.ItemSetDefaults;
import su.nightexpress.excellentenchants.enchantment.EnchantCatalog;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.EnchantHolder;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.EnchantSettings;
import su.nightexpress.excellentenchants.enchantment.EnchantedItem;
import su.nightexpress.excellentenchants.manager.block.TickedBlock;
import su.nightexpress.excellentenchants.manager.damage.Explosion;
import su.nightexpress.excellentenchants.manager.listener.AnvilListener;
import su.nightexpress.excellentenchants.manager.listener.EnchantListener;
import su.nightexpress.excellentenchants.manager.listener.GenericListener;
import su.nightexpress.excellentenchants.manager.listener.SlotListener;
import su.nightexpress.excellentenchants.manager.menu.EnchantsMenu;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.bridge.RegistryType;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

@NullMarked
public class EnchantManager extends AbstractManager<EnchantsPlugin> {

    private final Map<AbstractArrow, Set<UniParticle>> arrowEffects;
    private final Map<Location, TickedBlock>           tickedBlocks;
    private final Map<UUID, Explosion>                 explosions;

    private final EnchantSettings settings;

    private final NamespacedKey entitySpawnKey;
    private final NamespacedKey blockEnchantKey;

    private EnchantsMenu enchantsMenu;

    public EnchantManager(EnchantsPlugin plugin) {
        super(plugin);
        this.arrowEffects = new ConcurrentHashMap<>();
        this.tickedBlocks = new HashMap<>();
        this.explosions = new HashMap<>();
        this.settings = new EnchantSettings();

        this.entitySpawnKey = new NamespacedKey(plugin, "entity.spawn_reason");
        this.blockEnchantKey = new NamespacedKey(plugin, "block.enchant");
    }

    protected void onLoad() {
        this.settings.load(this.plugin.getConfig());
        this.loadEnchants();

        this.enchantsMenu = new EnchantsMenu(this.plugin);

        this.addListener(new GenericListener(this.plugin, this));
        this.addListener(new AnvilListener(this.plugin, this.settings));
        this.addListener(new EnchantListener(this.plugin, this));

        if (Version.isPaper()) {
            this.addListener(new SlotListener(this.plugin, this));
        }

        this.addAsyncTask(this::tickArrowEffects, this.settings.getArrowEffectsTickInterval());

        if (!EnchantRegistry.PASSIVE.isEmpty()) {
            this.addTask(this::tickPassiveEnchants, this.settings.getPassiveEnchantsTickInterval());
        }

        this.addTask(this::tickBlocks, 1L);
    }

    @Override
    protected void onShutdown() {
        this.restoreBlocks();

        if (this.enchantsMenu != null) this.enchantsMenu.clear();

        this.arrowEffects.clear();
        this.tickedBlocks.clear();
        this.explosions.clear();
    }

    private void loadEnchants() {
        EnchantCatalog.enabled().forEach(this::loadEnchant);
        ItemSetDefaults.clearAll(); // Clear default item sets from memory.

        this.plugin.info("Loaded " + EnchantRegistry.getRegistered().size() + " enchantments.");
    }

    private boolean loadEnchant(EnchantCatalog catalog) {
        String id = catalog.getId();
        CustomEnchantment registered = EnchantRegistry.getById(id);
        if (registered != null) {
            registered.load();
            return true;
        }

        Path file = Path.of(this.plugin.getDataFolder() + EnchantsFiles.DIR_ENCHANTS, FileConfig.withExtension(id));
        if (!Files.exists(file)) {
            this.plugin.error("No config file present for the '%s' enchantment.".formatted(id));
            return false;
        }

        Enchantment bukkitEnchant = BukkitThing.getByKey(RegistryType.ENCHANTMENT, catalog.getKey());
        if (bukkitEnchant == null) {
            this.plugin.error("No registered bukkit enchant found for '%s'.".formatted(id));
            return false;
        }

        EnchantContext context = new EnchantContext(id, bukkitEnchant, catalog.getDefinition(), catalog
            .getDistribution(), catalog.isCurse());
        CustomEnchantment enchantment = catalog.createEnchantment(this.plugin, this, file, context);

        enchantment.load();
        EnchantRegistry.registerEnchant(enchantment);
        return true;
    }

    public void updateCache(LivingEntity entity, EquipmentSlot slot, @Nullable ItemStack itemStack) {
        EnchantRegistry.getHolders().forEach(holder -> {
            if (!holder.isCacheable()) return;

            if (itemStack == null || itemStack.getType().isAir() || !EnchantsUtils.hasEnchantsAndNotABook(
                itemStack) || !EnchantsUtils.isValidSlotForEnchantEffects(itemStack, slot)) {
                holder.removeCache(entity, slot);
                return;
            }

            Map<CustomEnchantment, Integer> allEnchants = EnchantsUtils.getCustomEnchantments(itemStack);
            holder.updateCache(entity, slot, itemStack, allEnchants);
        });
    }

    public void clearCache(LivingEntity entity) {
        EnchantRegistry.getHolders().forEach(holder -> {
            if (!holder.isCacheable()) return;

            holder.clearCache(entity);
        });
    }

    public void reCache(LivingEntity entity) {
        this.clearCache(entity);

        EntityUtil.getEquippedItems(entity).forEach((slot, itemStack) -> {
            this.updateCache(entity, slot, itemStack);
        });
    }


    public EnchantSettings getSettings() {
        return this.settings;
    }

    public void openEnchantsMenu(Player player) {
        this.enchantsMenu.open(player);
    }

    public void addArrowEffect(AbstractArrow arrow, UniParticle particle) {
        this.arrowEffects.computeIfAbsent(arrow, k -> new HashSet<>()).add(particle);
    }

    public void removeArrowEffects(AbstractArrow arrow) {
        this.arrowEffects.remove(arrow);
    }

    private void tickArrowEffects() {
        this.arrowEffects.keySet().removeIf(arrow -> !arrow.isValid() || arrow.isDead());
        this.arrowEffects.forEach((arrow, effects) -> {
            effects.forEach(particle -> particle.play(arrow.getLocation(), 0f, 0f, 10));
        });
    }

    private void tickBlocks() {
        this.tickedBlocks.values().removeIf(tickedBlock -> {
            tickedBlock.tick();
            return tickedBlock.isDead();
        });
    }

    private void restoreBlocks() {
        this.tickedBlocks.values().forEach(TickedBlock::restore);
    }

    private void tickPassiveEnchants() {
        this.getPassiveEnchantEntities().forEach(entity -> {
            this.handleInSlots(entity, EntityUtil.EQUIPMENT_SLOTS, EnchantRegistry.PASSIVE, (item, enchant,
                                                                                             level) -> enchant
                                                                                                 .onTrigger(entity,
                                                                                                     item, level));
        });
    }


    private Set<LivingEntity> getPassiveEnchantEntities() {
        Set<LivingEntity> entities = new HashSet<>(Players.getOnline());

        if (this.settings.isPassiveEnchantsAllowedForMobs()) {
            this.plugin.getServer().getWorlds().forEach(world -> {
                entities.addAll(world.getLivingEntities());
            });
        }

        entities.removeIf(Entity::isDead);

        return entities;
    }

    public void addTickedBlock(Block block, Material origin, Material transform, int lifeTime) {
        Location location = block.getLocation();
        TickedBlock tickedBlock = new TickedBlock(location, origin, lifeTime);
        this.tickedBlocks.put(location, tickedBlock);

        block.setType(transform);
    }

    public boolean removeTickedBlock(Block block) {
        return this.removeTickedBlock(block.getLocation());
    }

    public boolean removeTickedBlock(Location location) {
        TickedBlock tickedBlock = this.tickedBlocks.remove(location);
        if (tickedBlock == null) return false;

        tickedBlock.restore();
        return true;
    }

    public void setBlockEnchant(ItemStack itemStack, BlockEnchant enchant) {
        PDCUtil.set(itemStack, this.blockEnchantKey, enchant.getId());
    }

    @Nullable
    public BlockEnchant getBlockEnchant(ItemStack itemStack) {
        String enchantId = PDCUtil.getString(itemStack, this.blockEnchantKey).orElse(null);
        if (enchantId == null) return null;

        return EnchantRegistry.BLOCK.getEnchant(enchantId);
    }

    public void setSpawnReason(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        PDCUtil.set(entity, this.entitySpawnKey, reason.name());
    }

    public CreatureSpawnEvent.@Nullable SpawnReason getSpawnReason(Entity entity) {
        String name = PDCUtil.getString(entity, this.entitySpawnKey).orElse(null);
        return name == null ? null : Enums.get(name, CreatureSpawnEvent.SpawnReason.class);
    }

    public boolean createExplosion(LivingEntity entity, Location location, float power, boolean fire, boolean destroy,
                                   Consumer<Explosion> consumer) {
        Explosion explosion = new Explosion(entity);
        consumer.accept(explosion);

        this.explosions.put(entity.getUniqueId(), explosion);

        return entity.getWorld().createExplosion(location, power, fire, destroy, entity);
    }

    public void handleEnchantExplosion(EntityExplodeEvent event, LivingEntity entity) {
        Explosion explosion = this.explosions.get(entity.getUniqueId());
        if (explosion == null) return;

        explosion.handleExplosion(event);

        this.plugin.runTask(() -> this.explosions.remove(entity.getUniqueId()));
    }

    public void handleEnchantExplosionDamage(EntityDamageByEntityEvent event, LivingEntity entity) {
        Explosion explosion = this.explosions.get(entity.getUniqueId());
        if (explosion == null) return;

        explosion.handleDamage(event);
    }

    public <T extends CustomEnchantment> void handleInventoryEnchants(Player player, EnchantHolder<T> holder,
                                                                      EnchantUsage<T> usage) {
        this.handleFully(player, EnchantsUtils.getAll(player, holder), holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleItemEnchants(LivingEntity entity,
                                                                 ItemStack itemStack,
                                                                 EnchantHolder<T> holder,
                                                                 EnchantUsage<T> usage) {
        Map<ItemStack, Map<T, Integer>> enchants = new HashMap<>();
        enchants.put(itemStack, EnchantsUtils.getCustomEnchantments(itemStack, holder));

        this.handleFully(entity, enchants, holder::getPriority, usage);
    }

    public <P extends AbstractArrow, T extends ProjectileEnchant<P>> void handleArrowEnchants(P projectile,
                                                                                              EnchantHolder<T> holder,
                                                                                              EnchantUsage<T> usage) {
        ItemStack bow = projectile.getWeapon();
        if (bow == null || !EnchantsUtils.hasEnchantsAndNotABook(bow)) return;

        Map<ItemStack, Map<T, Integer>> enchants = new HashMap<>();
        enchants.put(bow, EnchantsUtils.getArrowEnchants(projectile, holder));

        this.handleDirect(enchants, holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleInSlot(LivingEntity entity,
                                                           EquipmentSlot slot,
                                                           EnchantHolder<T> holder,
                                                           EnchantUsage<T> usage) {
        this.handleInSlots(entity, new EquipmentSlot[]{slot}, holder, usage);
    }

    public <T extends CustomEnchantment> void handleInSlots(LivingEntity entity,
                                                            EquipmentSlot[] slots,
                                                            EnchantHolder<T> holder,
                                                            EnchantUsage<T> usage) {

        Map<ItemStack, Map<T, Integer>> enchantMap = new HashMap<>();
        boolean noCache = entity.getType() != EntityType.PLAYER || !holder.isCacheable() || Version.isSpigot();

        for (EquipmentSlot slot : slots) {
            if (noCache || slot == EquipmentSlot.HAND) { // Main hand is not cached
                ItemStack itemStack = EntityUtil.getItemInSlot(entity, slot);
                if (itemStack == null || itemStack.getType().isAir()) continue; // Ignore empty slots.
                if (!EnchantsUtils.hasEnchantsAndNotABook(itemStack)) continue; // Ignore books and items without enchants.
                if (!EnchantsUtils.isValidSlotForEnchantEffects(itemStack, slot)) continue; // Ignore armor items when holding in hands.

                enchantMap.put(itemStack, EnchantsUtils.getCustomEnchantments(itemStack, holder));
            }
            else {
                EnchantedItem<T> enchantedItem = holder.getCached(entity, slot);
                if (enchantedItem == null) continue;

                enchantMap.put(enchantedItem.getItemStack(), enchantedItem.getEnchants());
            }
        }

        this.handleFully(entity, enchantMap, holder::getPriority, usage);
    }

    public <T extends CustomEnchantment> void handleFully(LivingEntity entity,
                                                          Map<ItemStack, Map<T, Integer>> enchantMap,
                                                          Function<T, EnchantPriority> priority,
                                                          EnchantUsage<T> usage) {

        this.handleDirect(enchantMap, priority, (itemStack, enchant, level) -> {
            if (this.settings.isEnchantDisabledInWorld(entity.getWorld(), enchant)) return false;
            if (enchant.isOutOfCharges(itemStack)) return false;
            if (enchant.hasComponent(EnchantComponent.PERIODIC) && !enchant.isTriggerTime(entity)) return false;
            if (enchant.hasComponent(EnchantComponent.PROBABILITY) && !enchant.testTriggerChance(level)) return false;
            if (!usage.useEnchant(itemStack, enchant, level)) return false;

            enchant.consumeCharges(itemStack, level); // TODO Re-add equipment for mobs to apply changes
            return true;
        });
    }

    public <T extends CustomEnchantment> void handleDirect(Map<ItemStack, Map<T, Integer>> enchantMap,
                                                           Function<T, EnchantPriority> priority,
                                                           EnchantUsage<T> usage) {
        enchantMap.forEach((itemStack, enchants) -> {
            enchants.entrySet().stream().sorted(Comparator.comparingInt(entry -> priority.apply(entry.getKey())
                .ordinal())).forEach(entry -> {
                    T enchant = entry.getKey();
                    int level = entry.getValue();

                    usage.useEnchant(itemStack, enchant, level);
                });
        });
    }
}

