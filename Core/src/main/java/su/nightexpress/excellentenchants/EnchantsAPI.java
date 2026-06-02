package su.nightexpress.excellentenchants;


import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.manager.EnchantManager;

@NullMarked
public class EnchantsAPI {

    private static EnchantsPlugin plugin;

    static void load(EnchantsPlugin plugin) {
        EnchantsAPI.plugin = plugin;
    }

    static void clear() {
        plugin = null;
    }


    public static EnchantsPlugin getPlugin() {
        if (plugin == null) throw new IllegalStateException("API is not initialized!");

        return plugin;
    }


    public static EnchantManager getEnchantManager() {
        return getPlugin().getEnchantManager();
    }

    //    
    //    public static EnchantNMS getInternals() {
    //        return getPlugin().getEnchantNMS();
    //    }
}
