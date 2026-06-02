package su.nightexpress.excellentenchants.api.item;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentenchants.bridge.ItemTagLookup;
import su.nightexpress.nightcore.util.Lists;

@NullMarked
public enum ItemSetDefaults {

    HELMET(tagLookup -> ItemSet.buildByName("helmet", tagLookup.getHelmets()).slots(EquipmentSlot.HEAD).name("Helmet")
        .build()),
    CHESTPLATE(tagLookup -> ItemSet.buildByName("chestplate", tagLookup.getChestplates()).slots(EquipmentSlot.CHEST)
        .name("Chestplate").build()),
    LEGGINGS(tagLookup -> ItemSet.buildByName("leggings", tagLookup.getLeggings()).slots(EquipmentSlot.LEGS).name(
        "Leggings").build()),
    BOOTS(tagLookup -> ItemSet.buildByName("boots", tagLookup.getBoots()).slots(EquipmentSlot.FEET).name("Boots")
        .build()),
    ELYTRA(tagLookup -> ItemSet.buildByType("elytra", Material.ELYTRA).slots(EquipmentSlot.CHEST).name("Elytra")
        .build()),
    SWORD(tagLookup -> ItemSet.buildByName("sword", tagLookup.getSwords()).slots(EquipmentSlot.HAND).name("Sword")
        .build()),
    AXE(tagLookup -> ItemSet.buildByName("axe", tagLookup.getAxes()).slots(EquipmentSlot.HAND).name("Axe").build()),
    HOE(tagLookup -> ItemSet.buildByName("hoe", tagLookup.getHoes()).slots(EquipmentSlot.HAND).name("Hoe").build()),
    PICKAXE(tagLookup -> ItemSet.buildByName("pickaxe", tagLookup.getPickaxes()).slots(EquipmentSlot.HAND).name(
        "Pickaxe").build()),
    SHOVEL(tagLookup -> ItemSet.buildByName("shovel", tagLookup.getShovels()).slots(EquipmentSlot.HAND).name("Shovel")
        .build()),
    SPEAR(tagLookup -> ItemSet.buildByName("spear", tagLookup.getSpears()).slots(EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND).name("Spear").build()),
    TRIDENT(tagLookup -> ItemSet.buildByType("trident", Material.TRIDENT).slots(EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND).name("Trident").build()),
    BOW(tagLookup -> ItemSet.buildByType("bow", Material.BOW).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name(
        "Bow").build()),
    CROSSBOW(tagLookup -> ItemSet.buildByType("crossbow", Material.CROSSBOW).slots(EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND).name("Crossbow").build()),
    FISHING_ROD(tagLookup -> ItemSet.buildByType("fishing_rod", Material.FISHING_ROD).slots(EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND).name("Fishing Rod").build()),
    SHIELD(tagLookup -> ItemSet.buildByType("shield", Material.SHIELD).slots(EquipmentSlot.OFF_HAND).name("Shield")
        .build()),
    BREAKABLE(tagLookup -> ItemSet.buildByName("breakable", tagLookup.getBreakable()).slots(EquipmentSlot.values())
        .name("Brekable").build()),
    ARMOR(tagLookup -> ItemSet.buildByName("armor", getArmors(tagLookup)).slots(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
        EquipmentSlot.LEGS, EquipmentSlot.FEET).name("Armor").build()),
    TOOL(tagLookup -> ItemSet.buildByName("tool", getTools(tagLookup)).slots(EquipmentSlot.HAND).name("Tool").build()),
    SWORDS_AXES(tagLookup -> ItemSet.buildByName("swords_axes", getSwordsAxes(tagLookup)).slots(EquipmentSlot.HAND)
        .name("Weapon").build()),
    BOW_CROSSBOW(tagLookup -> ItemSet.buildByType("bow_crossbow", Lists.newSet(Material.BOW, Material.CROSSBOW)).slots(
        EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Bow / Crossbow").build()),
    CHESTPLATE_ELYTRA(tagLookup -> ItemSet.buildByName("chestplate_elytra", getTorso(tagLookup)).slots(
        EquipmentSlot.CHEST).name("Chestplate / Elytra").build()),
    ALL_WEAPON(tagLookup -> ItemSet.buildByName("weapon", getAllRangeWeapon(tagLookup)).slots(EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND).name("Weapon").build()),
    MINING_TOOLS(tagLookup -> ItemSet.buildByName("mining_tools", getMiningTools(tagLookup)).slots(EquipmentSlot.HAND)
        .name("Mining Tool").build()),
    TOOLS_WEAPONS(tagLookup -> ItemSet.buildByName("tools_weapons", getToolsAndWeapon(tagLookup)).slots(
        EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Tools & Weapons").build());

    public static void initializeAll(ItemTagLookup tagLookup) {
        stream().forEach(set -> set.initialize(tagLookup));
    }


    public static Stream<ItemSetDefaults> stream() {
        return Stream.of(values());
    }

    public static void clearAll() {
        for (ItemSetDefaults value : values()) {
            value.clear();
        }
    }


    private static Set<String> getArmors(ItemTagLookup tagLookup) {
        Set<String> materials = new HashSet<>();
        materials.addAll(tagLookup.getHelmets());
        materials.addAll(tagLookup.getChestplates());
        materials.addAll(tagLookup.getLeggings());
        materials.addAll(tagLookup.getBoots());
        return materials;
    }


    private static Set<String> getTools(ItemTagLookup tagLookup) {
        Set<String> materials = new HashSet<>();
        materials.addAll(tagLookup.getAxes());
        materials.addAll(tagLookup.getHoes());
        materials.addAll(tagLookup.getPickaxes());
        materials.addAll(tagLookup.getShovels());
        materials.add(Material.SHEARS.name());
        return materials;
    }


    private static Set<String> getMiningTools(ItemTagLookup tagLookup) {
        Set<String> materials = new HashSet<>();
        materials.addAll(tagLookup.getAxes());
        materials.addAll(tagLookup.getPickaxes());
        materials.addAll(tagLookup.getShovels());
        return materials;
    }


    private static Set<String> getToolsAndWeapon(ItemTagLookup tagLookup) {
        Set<String> materials = new HashSet<>();
        materials.addAll(getTools(tagLookup));
        materials.addAll(getAllRangeWeapon(tagLookup));
        return materials;
    }


    private static Set<String> getSwordsAxes(ItemTagLookup tagLookup) {
        Set<String> materials = new HashSet<>();
        materials.addAll(tagLookup.getAxes());
        materials.addAll(tagLookup.getSwords());
        return materials;
    }


    private static Set<String> getAllRangeWeapon(ItemTagLookup tagLookup) {
        Set<String> materials = new HashSet<>();
        materials.addAll(tagLookup.getAxes());
        materials.addAll(tagLookup.getSwords());
        materials.addAll(tagLookup.getSpears());
        materials.add(Material.BOW.name());
        materials.add(Material.CROSSBOW.name());
        materials.add(Material.TRIDENT.name());
        materials.add(Material.MACE.name());
        return materials;
    }


    private static Set<String> getTorso(ItemTagLookup tagLookup) {
        Set<String> materials = new HashSet<>(tagLookup.getChestplates());
        materials.add(Material.ELYTRA.name());
        return materials;
    }

    private Function<ItemTagLookup, ItemSet> initFunction;

    @Nullable
    private ItemSet itemSet;

    ItemSetDefaults(Function<ItemTagLookup, ItemSet> initFunction) {
        this.initFunction = initFunction;
    }

    public void clear() {
        this.itemSet = null;
    }

    public void initialize(ItemTagLookup tagLookup) {
        if (this.itemSet == null) {
            this.itemSet = this.initFunction.apply(tagLookup);
        }
    }


    public ItemSet getItemSet() {
        if (this.itemSet == null) {
            throw new IllegalStateException("ItemSet is not initialized!");
        }
        return this.itemSet;
    }
}
