package su.nightexpress.excellentenchants.tooltip.format;


import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

@NullMarked
public class ChargesFormat implements Writeable {

    private final int    threshold;
    private final String format;

    public ChargesFormat(int threshold, String format) {
        this.threshold = threshold;
        this.format = format;
    }


    public static ChargesFormat read(FileConfig config, String path) {
        int threshold = config.getInt(path + ".Threshold");
        String format = config.getString(path + ".Format", EnchantsPlaceholders.GENERIC_AMOUNT);

        return new ChargesFormat(threshold, format);
    }

    @Override
    public void write(FileConfig config, String path) {
        config.set(path + ".Threshold", this.threshold);
        config.set(path + ".Format", this.format);
    }


    public String getFormatted(int charges) {
        return this.format.replace(EnchantsPlaceholders.GENERIC_AMOUNT, String.valueOf(charges));
    }

    public boolean isAboveThreshold(int percent) {
        return percent >= this.threshold;
    }

    public boolean isUnderThreshold(int percent) {
        return percent < this.threshold;
    }

    public int getThreshold() {
        return this.threshold;
    }


    public String getFormat() {
        return this.format;
    }
}
