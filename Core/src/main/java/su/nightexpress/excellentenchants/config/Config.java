package su.nightexpress.excellentenchants.config;

import org.bukkit.Material;

import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Set;

import static su.nightexpress.excellentenchants.EnchantsPlaceholders.URL_WIKI_ITEMS;
import static su.nightexpress.excellentenchants.EnchantsPlaceholders.WIKI_CHRAGES;

public class Config extends AbstractConfig {

    @Override
    public void load(FileConfig config) {
        if (config.contains("Description.Enabled")) {
            config.set("Modules.EnchantTooltip", config.getBoolean("Description.Enabled"));
            config.remove("Description.Enabled");
        }

        super.load(config);
    }

    private final ConfigProperty<Boolean> featuresEnchantTooltip = this.addProperty(ConfigTypes.BOOLEAN,
        "Modules.EnchantTooltip",
        true,
        "When 'true', adds the enchantment description to item lore under enchantment names."
    );

    public static final ConfigValue<Boolean> CHARGES_ENABLED = ConfigValue.create("Charges.Enabled",
        false,
        "Enables the Charges feature: " + WIKI_CHRAGES
    );

    public static final ConfigValue<Boolean> CHARGES_FUEL_IGNORE_META = ConfigValue.create("Charges.Fuel.Ignore_Meta",
        false,
        "Controls if item meta (such as display name, lore, model data, etc.) should be ignored when checks if item is valid fuel.",
        "[Default is false]"
    );

    public static final ConfigValue<NightItem> CHARGES_FUEL_ITEM = ConfigValue.create("Charges.Fuel.Item",
        NightItem.fromType(Material.LAPIS_LAZULI),
        "Default item used to fuel enchantments on anvils.",
        "Enchantments can have their own custom fuel items.",
        URL_WIKI_ITEMS
    );

    // gamai.ru: «случайная книга редкости X» — награды вместо Осколков Зари (вайп 2026-11).
    public static final ConfigValue<Set<String>> RANDOM_BOOK_EXCLUDE = ConfigValue.create("Random_Book.Exclude",
        Lists.newSet("dawn_strike", "second_wind", "daybreak", "crushing"),
        "Enchantment ids that '/eenchants randombook <player> <rarity>' never gives.",
        "Default: books that only come from the books case and events."
    );

    public static final ConfigValue<Boolean> RANDOM_BOOK_MAX_LEVEL = ConfigValue.create("Random_Book.Max_Level",
        true,
        "When 'true', a book given by rarity has the enchantment's max level, otherwise a random level."
    );

    public boolean isEnchantTooltipEnabled() {
        return this.featuresEnchantTooltip.get();
    }

    public static boolean isChargesEnabled() {
        return CHARGES_ENABLED.get();
    }
}
