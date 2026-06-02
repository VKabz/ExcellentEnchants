package su.nightexpress.excellentenchants.bridge.spigot;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.bridge.ItemTagLookup;

@NullMarked
public class SpigotItemTagLookup implements ItemTagLookup {

    @Override
    public Set<String> getBreakable() {
        return fromTag(Tag.ITEMS_ENCHANTABLE_DURABILITY);
    }

    @Override
    public Set<String> getHelmets() {
        return fromTag(Tag.ITEMS_HEAD_ARMOR);
    }

    @Override
    public Set<String> getChestplates() {
        return fromTag(Tag.ITEMS_CHEST_ARMOR);
    }

    @Override
    public Set<String> getLeggings() {
        return fromTag(Tag.ITEMS_LEG_ARMOR);
    }

    @Override
    public Set<String> getBoots() {
        return fromTag(Tag.ITEMS_FOOT_ARMOR);
    }

    @Override
    public Set<String> getSwords() {
        return fromTag(Tag.ITEMS_SWORDS);
    }

    @Override
    public Set<String> getSpears() {
        return fromTag("ITEMS_SPEARS");
    }

    @Override
    public Set<String> getAxes() {
        return fromTag(Tag.ITEMS_AXES);
    }

    @Override
    public Set<String> getHoes() {
        return fromTag(Tag.ITEMS_HOES);
    }

    @Override
    public Set<String> getPickaxes() {
        return fromTag(Tag.ITEMS_PICKAXES);
    }

    @Override
    public Set<String> getShovels() {
        return fromTag(Tag.ITEMS_SHOVELS);
    }

    private Set<String> fromTag(Tag<Material> tag) {
        return tag.getValues().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")

    private static Set<String> fromTag(String fieldName) {
        try {
            Object tag = Tag.class.getField(fieldName).get(null);
            if (tag instanceof Tag<?> bukkitTag) {
                return ((Tag<Material>) bukkitTag).getValues().stream().map(Enum::name).collect(Collectors.toSet());
            }
        }
        catch (ReflectiveOperationException ignored) {
        }
        return Collections.emptySet();
    }
}
