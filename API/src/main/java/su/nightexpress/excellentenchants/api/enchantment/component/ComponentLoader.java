package su.nightexpress.excellentenchants.api.enchantment.component;


import org.jspecify.annotations.NullMarked;

import su.nightexpress.nightcore.config.FileConfig;

@NullMarked
public interface ComponentLoader<T> {

    T load(FileConfig config);
}
