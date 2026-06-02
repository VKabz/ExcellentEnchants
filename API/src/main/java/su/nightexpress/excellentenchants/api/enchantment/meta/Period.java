package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.nightcore.util.TimeUtil;

@NullMarked
public class Period {

    private final long interval;

    public Period(long interval) {
        this.interval = interval;
    }

    public static Period ofSeconds(int seconds) {
        return new Period(seconds);
    }

    public long getInterval() {
        return this.interval;
    }

    public boolean isTriggerTime(LivingEntity entity) {
        int secondsLived = (int) TimeUtil.ticksToSeconds(entity.getTicksLived());

        return secondsLived % this.interval == 0;
    }
}
