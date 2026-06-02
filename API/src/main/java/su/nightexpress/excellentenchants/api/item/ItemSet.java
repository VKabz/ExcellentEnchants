package su.nightexpress.excellentenchants.api.item;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LowerCase;

@NullMarked
public class ItemSet implements Writeable {

    private final String          id;
    private final Set<String>     materials;
    private final EquipmentSlot[] slots;
    private final String          displayName;

    public ItemSet(String id, Set<String> materials, EquipmentSlot[] slots, String displayName) {
        this.id = id;
        this.materials = Lists.modify(materials, LowerCase.INTERNAL::apply);
        this.slots = slots;
        this.displayName = displayName;
    }


    public static ItemSet read(FileConfig config, String path, String id) {
        String name = config.getString(path + ".Name", "null");
        List<EquipmentSlot> slots = Lists.modify(config.getStringList(path + ".Slots"), raw -> Enums.get(raw,
            EquipmentSlot.class));
        slots.removeIf(Objects::isNull);

        Set<String> itemNames = config.getStringSet(path + ".Items");

        return new ItemSet(id, itemNames, slots.toArray(new EquipmentSlot[0]), name);
    }

    @Override
    public void write(FileConfig config, String path) {
        config.set(path + ".Name", this.displayName);
        config.set(path + ".Slots", Stream.of(this.slots).map(Enum::name).toList());
        config.set(path + ".Items", this.materials);
    }


    public static Builder buildByType(String id, Set<Material> materials) {
        return builder(id).materials(materials);
    }


    public static Builder buildByName(String id, Set<String> materials) {
        return builder(id).materialNames(materials);
    }


    public static Builder buildByType(String id, Material... materials) {
        return builder(id).materials(Lists.newSet(materials));
    }


    public static Builder builder(String id) {
        return new Builder(id);
    }


    public String getId() {
        return this.id;
    }


    public Set<String> getMaterials() {
        return this.materials;
    }

    public EquipmentSlot[] getSlots() {
        return this.slots;
    }


    public String getDisplayName() {
        return this.displayName;
    }

    public static class Builder {

        private final String id;

        private String          name;
        private Set<String>     materials;
        private EquipmentSlot[] slots;

        public Builder(String id) {
            this.id = id;
            this.name = "null";
            this.materials = new HashSet<>();
            this.slots = new EquipmentSlot[0];
        }


        public ItemSet build() {
            return new ItemSet(this.id, this.materials, this.slots, this.name);
        }


        public Builder name(String name) {
            this.name = name;
            return this;
        }


        public Builder materials(Set<Material> materials) {
            return this.materialNames(materials.stream().map(BukkitThing::getValue).collect(Collectors.toSet()));
        }


        public Builder materialNames(Set<String> materials) {
            this.materials = materials;
            return this;
        }


        public Builder slots(EquipmentSlot... slots) {
            this.slots = slots;
            return this;
        }
    }
}
