package su.nightexpress.excellentenchants.bridge;

import java.util.Set;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ItemTagLookup {

    Set<String> getBreakable();

    Set<String> getHelmets();

    Set<String> getChestplates();

    Set<String> getLeggings();

    Set<String> getBoots();

    Set<String> getSwords();


    default Set<String> getSpears() {
        return Set.of();
    }

    Set<String> getAxes();

    Set<String> getHoes();

    Set<String> getPickaxes();

    Set<String> getShovels();
}
