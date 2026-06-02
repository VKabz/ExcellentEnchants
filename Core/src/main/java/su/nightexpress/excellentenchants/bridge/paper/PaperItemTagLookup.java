package su.nightexpress.excellentenchants.bridge.paper;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import net.kyori.adventure.key.Key;
import su.nightexpress.excellentenchants.bridge.ItemTagLookup;

@NullMarked
public class PaperItemTagLookup implements ItemTagLookup {

    private final PostFlattenTagRegistrar<ItemType> registrar;

    public PaperItemTagLookup(PostFlattenTagRegistrar<ItemType> registrar) {
        this.registrar = registrar;
    }

    @Override
    public Set<String> getBreakable() {
        return this.fromRegistry(ItemTypeTagKeys.ENCHANTABLE_DURABILITY);
    }

    @Override
    public Set<String> getHelmets() {
        return this.fromRegistry(ItemTypeTagKeys.HEAD_ARMOR);
    }

    @Override
    public Set<String> getChestplates() {
        return this.fromRegistry(ItemTypeTagKeys.CHEST_ARMOR);
    }

    @Override
    public Set<String> getLeggings() {
        return this.fromRegistry(ItemTypeTagKeys.LEG_ARMOR);
    }

    @Override
    public Set<String> getBoots() {
        return this.fromRegistry(ItemTypeTagKeys.FOOT_ARMOR);
    }

    @Override
    public Set<String> getSwords() {
        return this.fromRegistry(ItemTypeTagKeys.SWORDS);
    }

    @Override
    public Set<String> getSpears() {
        return this.fromRegistry(TagKey.create(RegistryKey.ITEM, Key.key(Key.MINECRAFT_NAMESPACE, "spears")));
    }

    @Override
    public Set<String> getAxes() {
        return this.fromRegistry(ItemTypeTagKeys.AXES);
    }

    @Override
    public Set<String> getHoes() {
        return this.fromRegistry(ItemTypeTagKeys.HOES);
    }

    @Override
    public Set<String> getPickaxes() {
        return this.fromRegistry(ItemTypeTagKeys.PICKAXES);
    }

    @Override
    public Set<String> getShovels() {
        return this.fromRegistry(ItemTypeTagKeys.SHOVELS);
    }

    private Set<String> fromRegistry(TagKey<ItemType> key) {
        if (!this.registrar.hasTag(key)) return Set.of();

        return this.registrar.getTag(key).stream().map(typedKey -> typedKey.key().value()).collect(Collectors.toSet());
    }
}
