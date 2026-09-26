package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Персональный кулдаун зачарования: одна запись на сущность. Карта чистится лениво,
 * когда разрастается, — без отдельного таска.
 */
@NullMarked
public class Cooldown {

    private static final int PURGE_THRESHOLD = 2048;

    private final Map<UUID, Long> until = new HashMap<>();

    public boolean isReady(Entity entity) {
        Long time = this.until.get(entity.getUniqueId());
        return time == null || time <= System.currentTimeMillis();
    }

    public void start(Entity entity, double seconds) {
        long now = System.currentTimeMillis();
        if (this.until.size() > PURGE_THRESHOLD) {
            this.until.values().removeIf(time -> time <= now);
        }
        this.until.put(entity.getUniqueId(), now + (long) (seconds * 1000D));
    }

    public boolean tryUse(Entity entity, double seconds) {
        if (!this.isReady(entity)) return false;

        this.start(entity, seconds);
        return true;
    }
}
