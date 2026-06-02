package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.nightcore.util.bukkit.NightItem;

@NullMarked
public class Charges {

    private final Modifier  maxAmount;
    private final int       consumeAmount;
    private final int       rechargeAmount;
    private final boolean   customFuelEnabled;
    private final NightItem customFuelItem;

    public Charges(Modifier maxAmount, int consumeAmount, int rechargeAmount, boolean customFuelEnabled,
                   NightItem customFuelItem) {
        this.maxAmount = maxAmount;
        this.consumeAmount = consumeAmount;
        this.rechargeAmount = rechargeAmount;
        this.customFuelEnabled = customFuelEnabled;
        this.customFuelItem = customFuelItem;
    }


    public static Charges normal() {
        return new Charges(Modifier.addictive(100).perLevel(25).build(), 1, 25, false, NightItem.fromType(
            Material.LAPIS_LAZULI));
    }


    public static Charges custom(Modifier.Builder maxAmount, int consumeAmount, int rechargeAmount, NightItem fuel) {
        return custom(maxAmount.build(), consumeAmount, rechargeAmount, fuel);
    }


    public static Charges custom(Modifier maxAmount, int consumeAmount, int rechargeAmount, NightItem fuel) {
        return new Charges(maxAmount, consumeAmount, rechargeAmount, true, fuel);
    }

    public int getMaxAmount(int level) {
        return this.maxAmount.getIntValue(level);
    }

    public boolean isCustomFuelEnabled() {
        return this.customFuelEnabled;
    }


    public Modifier getMaxAmount() {
        return this.maxAmount;
    }


    public NightItem getCustomFuelItem() {
        return this.customFuelItem;
    }

    public int getConsumeAmount() {
        return this.consumeAmount;
    }

    public int getRechargeAmount() {
        return this.rechargeAmount;
    }
}
