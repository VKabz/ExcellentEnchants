package su.nightexpress.excellentenchants.bridge;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface DistributionSettings {

    boolean isEnchantingEnabled();

    boolean isTradingEnabled();

    boolean isMobEquipmentEnabled();

    boolean isTradeEquipmentEnabled();

    boolean isRandomLootEnabled();
}
